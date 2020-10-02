package org.sidiff.common.extension.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.Assert;
import org.sidiff.common.extension.IExtension;
import org.sidiff.common.extension.IExtension.Description;
import org.sidiff.common.extension.internal.ExtensionsPlugin;

/**
 * <p>An extension manager storage that caches the registered extensions, such that extensions
 * are only created once when the cache is created.</p>
 * <p>This storage implementation ensures that each extension is a singleton.
 * This storage must not be used when extensions have stored state, else different
 * processes may interfere with each other by accessing the same extension instance.</p>
 * <p>The contents of the cache may be modified programmatically.</p>
 * @author rmueller
 * @param <T> type of stored extensions
 * @see NoExtensionManagerStorage NoExtensionManagerStorage: to be used when extensions have stored state
 */
public class CachingExtensionManagerStorage<T extends IExtension> implements IExtensionManagerStorage<T> {

	private final Map<String,T> extensions = new HashMap<>();

	public CachingExtensionManagerStorage() {
		// Empty caching storage
	}

	public CachingExtensionManagerStorage(final Description<? extends T> description) {
		Assert.isNotNull(description);
		ExtensionsPlugin.logInfo("Initializing " + description.getExtensionClass().getSimpleName()
				+ " extensions of extension point " + description.getExtensionPointId());
		description.createRegisteredExtensions().forEach(this::addExtension);
	}

	@Override
	public void addExtension(T extension) {
		extensions.put(extension.getKey(), extension);
	}

	@Override
	public void removeExtension(String id) {
		extensions.remove(id);
	}

	@Override
	public void clearExtensions() {
		extensions.clear();
	}

	@Override
	public Collection<T> getExtensions() {
		return Collections.unmodifiableCollection(extensions.values());
	}

	@Override
	public Optional<? extends T> getExtension(String id) {
		return Optional.ofNullable(extensions.get(id));
	}
}
