package org.sidiff.common.settings.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SettingsPlugin implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.sidiff.common.settings"; //$NON-NLS-1$

	private static BundleContext bundleContext;

	@Override
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		bundleContext = null;
	}

	public static BundleContext getBundleContext() {
		return bundleContext;
	}
}
