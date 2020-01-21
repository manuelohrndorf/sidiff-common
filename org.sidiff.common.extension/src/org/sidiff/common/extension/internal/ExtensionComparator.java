package org.sidiff.common.extension.internal;

import java.util.Comparator;

import org.sidiff.common.extension.IExtension;

/**
 * Comparator for extensions, using {@link IExtension#getName()}
 * for lexicographically string comparison.
 * @author rmueller
 */
public class ExtensionComparator<T extends IExtension> implements Comparator<T> {

	private static final Comparator<? extends IExtension> INSTANCE = new ExtensionComparator<>();

	// type safe wrapper around singleton instance
	@SuppressWarnings("unchecked")
	public static <T extends IExtension> ExtensionComparator<T> getInstance() {
		return (ExtensionComparator<T>)INSTANCE;
	}

	@Override
	public int compare(final T ext1, final T ext2) {
		return ext1.getName().compareTo(ext2.getName());
	}
}