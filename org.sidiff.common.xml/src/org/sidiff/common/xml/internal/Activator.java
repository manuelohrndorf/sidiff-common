package org.sidiff.common.xml.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.sidiff.common.io.ResourceUtil;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		ResourceUtil.registerClassLoader(getClass().getClassLoader());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ResourceUtil.unregisterClassLoader(getClass().getClassLoader());
	}
}
