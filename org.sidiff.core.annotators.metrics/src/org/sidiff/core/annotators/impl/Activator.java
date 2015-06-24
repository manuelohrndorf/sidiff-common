package org.sidiff.core.annotators.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.sidiff.common.io.ResourceUtil;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		ResourceUtil.registerClassLoader(this.getClass().getClassLoader());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ResourceUtil.unregisterClassLoader(this.getClass().getClassLoader());
	}

}
