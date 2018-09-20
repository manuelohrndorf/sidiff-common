package org.sidiff.common.extension.internal;

import java.util.Comparator;

import org.sidiff.common.extension.IExtension;

/**
 * Comparator for extensions, using {@link IExtension#getName()}
 * for lexicographically string comparison.
 * @author Robert Müller
 */
public class ExtensionComparator<T extends IExtension> implements Comparator<T> {

	@Override
	public int compare(final T ext1, final T ext2) {
		return ext1.getName().compareTo(ext2.getName());
	}
}