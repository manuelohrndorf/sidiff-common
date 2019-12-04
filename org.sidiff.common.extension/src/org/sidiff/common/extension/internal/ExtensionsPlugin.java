package org.sidiff.common.extension.internal;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * <p>Plugin activator for the extension management plugin.
 * Contains utility functions for logging.</p>
 * <p>The VM system property <code>extensionManagerLogging</code> controls the
 * minimum logging level ("info", "warning", "error") for console log output (default is "error").</p>
 * @author rmueller
 */
public class ExtensionsPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.sidiff.common.extension";

	private static ExtensionsPlugin instance;

	private int loggingLevel;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		
		this.loggingLevel = parseLoggingLevel(System.getProperty("extensionManagerLogging"));
	}

	private static int parseLoggingLevel(String property) {
		if(property == null || property.isEmpty() || property.equalsIgnoreCase("error")) {
			return Status.ERROR;
		}
		if(property.equalsIgnoreCase("warning")) {
			return Status.WARNING;
		}
		if(property.equalsIgnoreCase("info")) {
			return Status.INFO;
		}
		return Status.ERROR;
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
		if(severity >= instance.loggingLevel) {
			instance.getLog().log(new Status(severity, PLUGIN_ID, message, throwable));			
		}
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
