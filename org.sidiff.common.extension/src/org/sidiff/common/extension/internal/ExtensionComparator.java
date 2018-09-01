package org.sidiff.common.extension.internal;

import java.util.Comparator;

import org.sidiff.common.extension.IExtension;
import org.sidiff.common.extension.INamedExtension;

/**
 * Comparator for extensions, using the name for {@link INamedExtension}
 * and the class name as fallback for lexicographically string comparison.
 * @author Robert Müller
 *
 */
public class ExtensionComparator implements Comparator<IExtension> {

	@Override
	public int compare(final IExtension ext1, final IExtension ext2) {
		return getName(ext1).compareTo(getName(ext2));
	}

	private String getName(final IExtension ext) {
		if(ext instanceof INamedExtension) {
			return ((INamedExtension)ext).getName();
		}
		return ext.getClass().getSimpleName();
	}
}