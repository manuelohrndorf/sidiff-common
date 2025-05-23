package org.sidiff.common.extension;

import java.util.*;
import java.util.function.BiPredicate;

import org.eclipse.core.runtime.Assert;
import org.sidiff.common.extension.IExtension.Description;
import org.sidiff.common.extension.internal.ExtensionComparator;
import org.sidiff.common.extension.internal.ExtensionEquality;
import org.sidiff.common.extension.storage.CachingExtensionManagerStorage;
import org.sidiff.common.extension.storage.IExtensionManagerStorage;

/**
 * <p>A generic manager for extensions.</p>
 * <p>The interface {@link IExtension} specifies a generic extension,
 * which is usually associated with an eclipse extension point.</p>
 * <p>If the extension implements {@link ITypedExtension}, a
 * {@link TypedExtensionManager} should be used instead,
 * as it provides additional functionality.</p>
 * <p>Parameter and return values should never be <code>null</code>.</p>
 * @param <T> the type of the extension, extending {@link IExtension}
 * @author rmueller
 */
public class ExtensionManager<T extends IExtension> {

	private final IExtensionManagerStorage<T> storage;

	/**
	 * Creates a new, empty extension manager, using a {@link CachingExtensionManagerStorage}.
	 */
	public ExtensionManager() {
		this(new CachingExtensionManagerStorage<>());
	}

	/**
	 * Creates a new, empty extension manager and loads the extensions
	 * from the extension point specified by the description, using
	 * a {@link CachingExtensionManagerStorage}.
	 * @param description description of the extension point
	 */
	public ExtensionManager(final Description<? extends T> description) {
		this(new CachingExtensionManagerStorage<>(description));
	}

	/**
	 * Creates a new extension manager that uses the given storage implementation.
	 * @param storage the storage implementation
	 */
	public ExtensionManager(final IExtensionManagerStorage<T> storage) {
		this.storage = Objects.requireNonNull(storage, "extension manager storage is null");
	}

	/**
	 * Adds the given extension to this manager.
	 * Existing extensions with the same ID are replaced.
	 * @param extension the extension
	 */
	public final void addExtension(final T extension) {
		Assert.isNotNull(extension);
		synchronized (storage) {
			storage.addExtension(extension);
		}
	}

	/**
	 * Removes the extension with the given ID from this manager.
	 * Does nothing if no extension with this ID exists.
	 * @param id the extension's ID
	 */
	public final void removeExtension(final String id) {
		Assert.isNotNull(id);
		synchronized (storage) {
			storage.removeExtension(id);
		}
	}

	/**
	 * Removes all extensions from this manager.
	 */
	public final void clearExtensions() {
		synchronized (storage) {
			storage.clearExtensions();
		}
	}

	/**
	 * Returns all extensions of this manager.
	 * @return unmodifiable collection of all extensions of this manager
	 */
	public final Collection<T> getExtensions() {
		synchronized (storage) {
			return storage.getExtensions();
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
	 * <p>Returns the extension with the given ID.</p>
	 * <p>The returned Optional is empty if no extension with this ID exists.</p>
	 * @param id the extension's ID
	 * @return {@link Optional} containing the extension with the id, or empty Optional if none
	 */
	public final Optional<? extends T> getExtension(final String id) {
		Assert.isNotNull(id);
		synchronized (storage) {
			return storage.getExtension(id);
		}
	}

	/**
	 * <p>Returns the extension with the given ID.</p>
	 * <p>Throws a runtime exception if no extension with that ID is found.</p>
	 * @param id the extension's ID
	 * @return the extension with the id, never <code>null</code>
	 * @implSpec delegates to {@link #getExtension(String)}
	 */
	public final T requireExtension(final String id) {
		return getExtension(id)
				.orElseThrow(() -> new NoSuchElementException("No extension with key '" + id + "' was found"));
	}

	/**
	 * <p>Returns the first extension with the given class.</p>
	 * <p>The returned Optional is empty if no extension with this class exists.</p>
	 * @param extensionClass the extension's class
	 * @return {@link Optional} containing the extension with the class, or empty Optional if none
	 */
	public final <S extends T> Optional<S> getExtension(final Class<S> extensionClass) {
		Assert.isNotNull(extensionClass);
		return getExtensions().stream()
				.filter(extensionClass::isInstance)
				.map(extensionClass::cast)
				.findFirst();
	}

	/**
	 * <p>Returns the first extension with the given class.</p>
	 * <p>Throws a runtime exception if no extension with that class is found.</p>
	 * @param extensionClass the extension's class
	 * @return the extension with the class, never <code>null</code>
	 * @implSpec delegates to {@link #getExtension(Class)}
	 */
	public final <S extends T> S requireExtension(final Class<S> extensionClass) {
		return getExtension(extensionClass)
				.orElseThrow(() -> new NoSuchElementException("No extension with type '" + extensionClass.getName() + "' was found"));
	}

	/**
	 * <p>Returns the default extension of this manager.</p>
	 * <p>The default implementation of ExtensionManager returns any extension
	 * if this manager has any, else an empty optional is returned.</p>
	 * <p>Subclasses may override.</p>
	 * @return the default extension, or empty Optional if none
	 */
	public Optional<? extends T> getDefaultExtension() {
		return getExtensions().stream().findAny();
	}

	/**
	 * <p>Returns the default extension of this manager.</p>
	 * <p>Throws a runtime exception if no default extension is available.</p>
	 * @return the default extension, never <code>null</code>
	 * @implSpec delegates to {@link #getDefaultExtension()}
	 */
	public final T requireDefaultExtension() {
		return getDefaultExtension()
				.orElseThrow(() -> new NoSuchElementException("No default extension is available"));
	}

	/**
	 * <p>Returns a comparator for the extensions of this manager.</p>
	 * <p>The default comparator compares extensions lexicographically
	 * using {@link IExtension#getName()}.</p>
	 * <p>Subclasses may override.</p>
	 * @return comparator for extensions
	 */
	public Comparator<? super T> getComparator() {
		return ExtensionComparator.getInstance();
	}

	/**
	 * <p>Returns a {@link BiPredicate} to compare two extensions of this manager to determine equality.</p>
	 * <p>The default equality is based on {@link IExtension#getKey()}.</p>
	 * @return predicate to check equality of extensions of this manager
	 */
	public BiPredicate<T,T> getEquality() {
		return ExtensionEquality.getInstance();
	}
}
