package org.sidiff.common.extension;

/**
 * An {@link IExtension extension} that specifies a key for indexing,
 * and a human-readable name.
 * @author Robert Müller
 *
 */
public interface INamedExtension extends IExtension {

	/**
	 * Returns a key that uniquely identifies this extension.
	 * @return unique key / identifier
	 */
	String getKey();

	/**
	 * Returns a human-readable name for this extension.
	 * @return
	 */
	String getName();
}
