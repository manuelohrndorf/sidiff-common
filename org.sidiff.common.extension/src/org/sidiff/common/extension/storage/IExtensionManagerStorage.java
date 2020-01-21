package org.sidiff.common.extension.storage;

import java.util.Collection;
import java.util.Optional;

import org.sidiff.common.extension.IExtension;

/**
 * The extension storage is used by extension managers to access and/or cache
 * the registered extensions.
 * @author rmueller
 * @param <T> type of stored extensions
 */
public interface IExtensionManagerStorage<T extends IExtension> {

	Collection<T> getExtensions();
	void addExtension(T extension);
	void removeExtension(String id);
	void clearExtensions();
	Optional<? extends T> getExtension(String id);
}
