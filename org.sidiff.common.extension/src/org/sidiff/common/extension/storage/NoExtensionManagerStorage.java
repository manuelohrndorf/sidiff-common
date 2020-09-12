package org.sidiff.common.extension.storage;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.sidiff.common.extension.IExtension;
import org.sidiff.common.extension.IExtension.Description;

/**
 * <p>An extension manager storage that does not store extensions and always recreates them based
 * on the extension description.</p>
 * <p>This storage implementation must be used if any of the extensions can have any kind of
 * stored state. This ensures that extension instances are used exclusively without any interference.</p>
 * <p>The storage cannot be modified and the related methods throw runtime exceptions.</p>
 * @author rmueller
 * @param <T> type of stored extensions
 * @see CachingExtensionManagerStorage CachingExtensionManagerStorage: to be used for singleton extensions without stored state
 */
public class NoExtensionManagerStorage<T extends IExtension> implements IExtensionManagerStorage<T> {

	private final Description<? extends T> description;

	public NoExtensionManagerStorage(Description<? extends T> description) {
		this.description = Objects.requireNonNull(description);
	}

	@Override
	public void addExtension(T extension) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeExtension(String id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearExtensions() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<T> getExtensions() {
		return description.createRegisteredExtensions().collect(Collectors.toList());
	}

	@Override
	public Optional<? extends T> getExtension(String id) {
		return description.createRegisteredExtensions().filter(ext -> ext.getKey().equals(id)).findFirst();
	}
}
