package org.sidiff.common.extension.internal;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * Plugin activator for the extension management plugin.
 * Contains utility functions for logging.
 * @author Robert Müller
 *
 */
public class ExtensionsPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.sidiff.common.extension";

	private static ExtensionsPlugin instance;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		instance = null;
	}

	public static ExtensionsPlugin getDefault() {
		return instance;
	}


	// logging utility functions
	//

	public static void log(int severity, String message, Throwable throwable) {
		getDefault().getLog().log(new Status(severity, PLUGIN_ID, message, throwable));
	}

	public static void logInfo(String message) {
		log(Status.INFO, message, null);
	}
	
	public static void logError(String message, Throwable throwable) {
		log(Status.ERROR, message, throwable);
	}

	public static void logWarning(String message, Throwable throwable) {
		log(Status.WARNING, message, throwable);
	}

	public static void logWarning(String message) {
		logWarning(message, null);
	}
}
