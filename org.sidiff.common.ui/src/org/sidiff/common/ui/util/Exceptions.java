package org.sidiff.common.ui.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.statushandlers.StatusManager;
import org.sidiff.common.exceptions.SiDiffException;
import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.logging.StatusWrapper;
import org.sidiff.common.logging.StatusWrapper.IStatusRunnable;

/**
 * <p>Utility class for convenient exception handling.
 * This class allows execution of {@link IStatusRunnable arbitrary code} while
 * automatically catching all exceptions and handling
 * them appropriately (logging, error dialogs).</p>
 * <p>Exceptions are converted to {@link IStatus} using
 * {@link StatusWrapper} and then handled by the {@link StatusManager}.</p>
 * <p>For own exception types, it is recommended to subclass {@link SiDiffException}
 * or {@link SiDiffRuntimeException}, to allow for specification of a custom status message.</p>
 * <p>Example usage: 
 * <pre>
 * // Shows an error dialog if the operation failed
 * Exceptions.show(() -> {
 *     // ... some operation that can throw an exception ...
 *     return Status.OK_STATUS;
 * });
 * </pre></p>
 * <p><pre>
 * // Logs the status of the operation
 * Exceptions.log(() -> {
 *     // Can also be used with try-with-resource inside
 *     try(FileOutputStream out = new FileOutputStream(...)) {
 *         out.write(...);
 *     }
 *     // Return Info-status to log
 *     return new Status(IStatus.INFO, "&lt;plugin id>", "File written to ...");
 * });
 * </pre></p>
 * @author Robert Müller
 */
public final class Exceptions {

	private Exceptions() {
		throw new AssertionError();
	}

	/**
	 * <p>Executes the runnable and logs its returned status / thrown exception.</p>
	 * <p>If the status is <i>OK</i> or <i>Cancel</i>, it is ignored.</p>
	 * @param code the runnable to execute
	 */
	public static void log(IStatusRunnable code) {
		handle(code, StatusManager.LOG);
	}

	/**
	 * <p>Executes the runnable and shows a dialog for its
	 * returned status / thrown exception, also logging it.</p>
	 * <p>If the status is <i>OK</i> or <i>Cancel</i>, it is ignored.</p>
	 * @param code the runnable to execute
	 */
	public static void show(IStatusRunnable code) {
		handle(code, StatusManager.LOG|StatusManager.SHOW);
	}

	private static void handle(IStatusRunnable code, int style) {
		IStatus status = StatusWrapper.wrap(code);
		if(!status.isOK() && status.getSeverity() != IStatus.CANCEL) {
			StatusManager.getManager().handle(status, style);
		}
	}
}
