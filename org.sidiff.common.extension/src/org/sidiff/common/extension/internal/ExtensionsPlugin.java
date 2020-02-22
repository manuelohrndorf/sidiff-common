package org.sidiff.common.extension.internal;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * <p>Plugin activator for the extension management plugin.
 * Contains utility functions for logging.</p>
 * <p>The VM system property {@value #PROP_EXTENSION_MANAGER_LOGGING} controls the
 * minimum logging level ({@value #LOGGING_LEVEL_ERROR}, {@value #LOGGING_LEVEL_WARNING},
 * {@value #LOGGING_LEVEL_INFO}) for console log output (default is {@value #LOGGING_LEVEL_ERROR}).</p>
 * @author rmueller
 */
public class ExtensionsPlugin extends Plugin {

	public static final String ID = "org.sidiff.common.extension";

	public static final String PROP_EXTENSION_MANAGER_LOGGING = "extensionManagerLogging";
	public static final String LOGGING_LEVEL_ERROR = "error";
	public static final String LOGGING_LEVEL_WARNING = "warning";
	public static final String LOGGING_LEVEL_INFO = "info";

	private static ExtensionsPlugin instance;

	private int loggingLevel;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		this.loggingLevel = parseLoggingLevel(System.getProperty(PROP_EXTENSION_MANAGER_LOGGING));
	}

	private static int parseLoggingLevel(String property) {
		if(property == null || property.isEmpty() || property.equalsIgnoreCase(LOGGING_LEVEL_ERROR)) {
			return Status.ERROR;
		}
		if(property.equalsIgnoreCase(LOGGING_LEVEL_WARNING)) {
			return Status.WARNING;
		}
		if(property.equalsIgnoreCase(LOGGING_LEVEL_INFO)) {
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
			instance.getLog().log(new Status(severity, ID, message, throwable));			
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
