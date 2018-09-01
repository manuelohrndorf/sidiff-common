package org.sidiff.common.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.sidiff.common.extension.IExtension.Description;
import org.sidiff.common.extension.internal.ExtensionComparator;
import org.sidiff.common.extension.internal.ExtensionsPlugin;

/**
 * <p>A generic manager for extensions.</p>
 * <p>The interface {@link IExtension} specifies a generic extension,
 * which is usually associated with an eclipse extension point.</p>
 * <p>If the extension implements {@link INamedExtension}, the
 * extension's custom key and name will be used for indexing and sorting,
 * else key and name will be derived from the extension's class object.</p>
 * <p>If the extension implements {@link ITypedExtension}, a
 * {@link TypedExtensionManager} should be used instead,
 * as it provides additional functionality.</p>
 * @author Robert Müller
 *
 * @param <T> the type of the extension, extending {@link IExtension}
 */
public class ExtensionManager<T extends IExtension> {

	private final Map<String,T> extensions = new HashMap<String,T>();
	private final Comparator<IExtension> comparator = new ExtensionComparator();

	/**
	 * Creates a new, empty extension manager.
	 */
	public ExtensionManager() {
		// empty default constructor
	}

	/**
	 * Creates a new, empty extension manager and Loads the extensions
	 * from the extension point specified by the description.
	 * @param description description of the extension point
	 */
	public ExtensionManager(final Description<T> description) {
		Assert.isNotNull(description);
		for(final IConfigurationElement element :
			RegistryFactory.getRegistry().getConfigurationElementsFor(description.getExtensionPointId())) {
			if(element.getName().equals(description.getElementName())) {
				try {
					final T extension = description.getExtensionClass().cast(element.createExecutableExtension(description.getClassAttribute()));
					addExtension(extension);
				} catch (CoreException e) {
					ExtensionsPlugin.logError("Failed to create executable extension contributed by "
							+ element.getDeclaringExtension().getContributor().getName()
							+ " for extension point " + description.getExtensionPointId(), e);
				}
			}
		}
	}

	/**
	 * Adds the given extension to this manager.
	 * Existing extensions with the same ID are replaced.
	 * @param extension the extension, must not be <code>null</code>
	 */
	public void addExtension(final T extension) {
		Assert.isNotNull(extension);
		extensions.put(getInternalExtensionId(extension), extension);
	}

	/**
	 * Returns all extensions of this manager.
	 * @return collection of all extensions of this manager
	 */
	public Collection<T> getExtensions() {
		return new ArrayList<>(extensions.values());
	}

	/**
	 * <p>Returns a sorted list of all extensions of this manager.</p>
	 * <p>If the extensions implement {@link INamedExtension}, the
	 * extension's name is used for sorting. Else, the simple name
	 * of its class is used.</p>
	 * @return sorted list of all extensions of this manager
	 */
	public List<T> getSortedExtensions() {
		final List<T> sortedExtensions = new ArrayList<>(getExtensions());
		Collections.sort(sortedExtensions, getComparator());
		return sortedExtensions;
	}

	/**
	 * Returns the extension with the given ID.
	 * The returned Optional is empty if no extension with this ID exists.
	 * @param id the extension's ID, must not be <code>null</code>
	 * @return {@link Optional} containing the extension with the id, or empty Optional if none
	 */
	public Optional<T> getExtension(final String id) {
		Assert.isNotNull(id);
		final T extension = extensions.get(id);
		if(extension == null) {
			return Optional.empty();
		}
		return Optional.of(extension);
	}

	/**
	 * Returns a comparator for extensions, using the name for {@link INamedExtension}
	 * and the simple class name as fallback for lexicographically string comparison.
	 * @return comparator for extensions
	 */
	public Comparator<IExtension> getComparator() {
		return comparator;
	}

	private String getInternalExtensionId(final IExtension extension) {
		if(extension instanceof INamedExtension) {
			return Objects.requireNonNull(((INamedExtension)extension).getKey());
		}
		return extension.getClass().getName();
	}
}
