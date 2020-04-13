package org.sidiff.common.emf.input.ui.internal;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EmfInputUiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.sidiff.common.emf.input.ui"; //$NON-NLS-1$
	
	public static final String IMAGE_ARROW_UP = "arrowup";
	public static final String IMAGE_ARROW_DOWN = "arrowdown";

	// The shared instance
	private static EmfInputUiPlugin plugin;
	
	/**
	 * The constructor
	 */
	public EmfInputUiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
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
	public static EmfInputUiPlugin getDefault() {
		return plugin;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put(IMAGE_ARROW_UP, imageDescriptorFromPlugin(PLUGIN_ID, "icons/arrow_up.png"));
		reg.put(IMAGE_ARROW_DOWN, imageDescriptorFromPlugin(PLUGIN_ID, "icons/arrow_down.png"));
	}
	
	public static Image getImage(String key) {
		return getDefault().getImageRegistry().getDescriptor(key).createImage();
	}
}
