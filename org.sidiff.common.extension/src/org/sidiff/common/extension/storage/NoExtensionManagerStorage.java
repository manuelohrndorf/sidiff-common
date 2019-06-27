package org.sidiff.common.extension.storage;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.sidiff.common.extension.IExtension;
import org.sidiff.common.extension.IExtension.Description;

/**
 * An extension manager storage that does not store extensions and always recreates them based
 * on the extension description.
 * The storage cannot be modified, and the related methods throw UnsupportedOperationException.
 * @author Robert Müller
 * @param <T> type of stored extensions
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
