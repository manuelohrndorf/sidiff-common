package org.sidiff.common.logging;

import org.eclipse.core.runtime.IStatus;
import org.sidiff.common.exceptions.ExceptionUtil;

/**
 * <p>This utility class allows execution of arbitrary code without
 * catching exceptions. All exceptions are catched and returned as
 * an {@link IStatus} instead. Usage example:</p>
 * <pre>
 * IStatus status = StatusWrapper.run(() -> {
 *   // code that might throw an exception
 * }
 * </pre>
 * @author Robert M�ller
 */
public class StatusWrapper {

	/**
	 * Runnable interface with a run method that can throw
	 * arbitrary exceptions and can return an IStatus.
	 */
	@FunctionalInterface
	public interface IStatusRunnable {
		/**
		 * Executes the runnable and returns the result of 
		 * the operation.
		 * @return status of the execution
		 * @throws Exception if something went wrong
		 */
		IStatus run() throws Exception;
	}

	/**
	 * Executes the runnable and returns the status of
	 * the operation. If the runnable throws an exception,
	 * the exception is wrapped in a status an returned.
	 * Else, the status returned by the runnable is returned.
	 * @param code the runnable to execute
	 * @return status of the execution
	 */
	public static IStatus wrap(IStatusRunnable code) {
		try {
			return code.run();
		} catch(Exception e) {
			return ExceptionUtil.toStatus(e);
		}
	}
}
