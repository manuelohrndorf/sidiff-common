package org.sidiff.common.extension.configuration;

import org.sidiff.common.extension.IExtension;

/**
 * A configurable extension is a {@link IExtension} that
 * has a {@link IExtensionConfiguration}.
 * @author Robert Müller
 *
 */
public interface IConfigurableExtension extends IExtension {

	/**
	 * Returns the configuration of this extension.
	 * @return the configuration
	 */
	IExtensionConfiguration getConfiguration();
}
