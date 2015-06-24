package org.sidiff.common.services;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.sidiff.common.services.debug.SiDiffDebugger;

public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		context.registerService(CommandProvider.class.getName(), SiDiffDebugger.getInstance(), null);
	}

	public void stop(BundleContext context) throws Exception {
	}

}
