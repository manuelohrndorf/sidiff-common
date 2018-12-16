package org.sidiff.common.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.sidiff.common.extension.IExtension.Description;
import org.sidiff.common.extension.internal.ExtensionComparator;
import org.sidiff.common.extension.internal.ExtensionsPlugin;

/**
 * <p>A generic manager for extensions.</p>
 * <p>The interface {@link IExtension} specifies a generic extension,
 * which is usually associated with an eclipse extension point.</p>
 * <p>If the extension implements {@link ITypedExtension}, a
 * {@link TypedExtensionManager} should be used instead,
 * as it provides additional functionality.</p>
 * <p>Parameter and return values should never be <code>null</code>.</p>
 * @param <T> the type of the extension, extending {@link IExtension}
 * @author Robert Müller
 */
public class ExtensionManager<T extends IExtension> {

	private final Map<String,T> extensions = new HashMap<>();
	private final Comparator<T> comparator = new ExtensionComparator<>();

	/**
	 * Creates a new, empty extension manager.
	 */
	public ExtensionManager() {
		// empty default constructor
	}

	/**
	 * Creates a new, empty extension manager and loads the extensions
	 * from the extension point specified by the description.
	 * @param description description of the extension point
	 */
	public ExtensionManager(final Description<T> description) {
		Assert.isNotNull(description);
		for(final IConfigurationElement element :
			RegistryFactory.getRegistry().getConfigurationElementsFor(description.getExtensionPointId())) {
			if(element.getName().equals(description.getElementName())) {
				try {
					final Object rawExtension = element.createExecutableExtension(description.getClassAttribute());
					final T extension = description.getExtensionClass().cast(rawExtension);
					addExtension(extension);
				} catch (Exception e) {
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
	 * @param extension the extension
	 */
	public final void addExtension(final T extension) {
		Assert.isNotNull(extension);
		synchronized (extensions) {
			extensions.put(extension.getKey(), extension);
		}
	}

	/**
	 * Removes the extension with the given ID from this manager.
	 * Does nothing if no extension with this ID exists.
	 * @param id the extension's ID
	 */
	public final void removeExtension(final String id) {
		Assert.isNotNull(id);
		synchronized (extensions) {
			extensions.remove(id);
		}
	}

	/**
	 * Returns all extensions of this manager.
	 * @return unmodifiable collection of all extensions of this manager
	 */
	public final Collection<T> getExtensions() {
		synchronized (extensions) {
			return Collections.unmodifiableCollection(extensions.values());
		}
	}

	/**
	 * <p>Returns a sorted list of all extensions of this manager,
	 * using the comparator provided by {@link #getComparator()}.</p>
	 * @return sorted list of all extensions of this manager
	 */
	public final List<T> getSortedExtensions() {
		final List<T> sortedExtensions = new ArrayList<>(getExtensions());
		sortedExtensions.sort(getComparator());
		return sortedExtensions;
	}

	/**
	 * Returns the extension with the given ID.
	 * The returned Optional is empty if no extension with this ID exists.
	 * @param id the extension's ID
	 * @return {@link Optional} containing the extension with the id, or empty Optional if none
	 */
	public final Optional<T> getExtension(final String id) {
		Assert.isNotNull(id);
		synchronized (extensions) {
			return Optional.ofNullable(extensions.get(id));
		}
	}

	/**
	 * Returns the first extension with the given class.
	 * The returned Optional is empty if no extension with this class exists.
	 * @param extensionClass the extension's class
	 * @return {@link Optional} containing the extension with the class, or empty Optional if none
	 */
	public final <S extends T> Optional<S> getExtension(final Class<S> extensionClass) {
		Assert.isNotNull(extensionClass);
		synchronized (extensions) {
			return extensions.values().stream()
					.filter(extensionClass::isInstance)
					.map(extensionClass::cast)
					.findFirst();
		}
	}

	/**
	 * <p>Returns the default extension of this manager.</p>
	 * <p>The default implementation returns any extension
	 * if this manager has any, else an empty optional is returned.</p>
	 * <p>Subclasses may override.</p>
	 * @return the default extension, or empty Optional if none
	 */
	public Optional<T> getDefaultExtension() {
		return getExtensions().stream().findAny();
	}

	/**
	 * <p>Returns a comparator for the extensions of this manager.</p>
	 * <p>The default comparator compares extensions lexicographically
	 * using {@link IExtension#getName()}.</p>
	 * <p>Subclasses may override.</p>
	 * @return comparator for extensions
	 */
	public Comparator<? super T> getComparator() {
		return comparator;
	}
}
