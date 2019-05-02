package org.sidiff.common.emf.metrics.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MetricsUiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String ID = "org.sidiff.common.emf.metrics.ui"; //$NON-NLS-1$

	// The shared instance
	private static MetricsUiPlugin plugin;
	
	/**
	 * The constructor
	 */
	public MetricsUiPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MetricsUiPlugin getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String filename) {
		return imageDescriptorFromPlugin(ID, "/icons/" + filename);
	}
}
