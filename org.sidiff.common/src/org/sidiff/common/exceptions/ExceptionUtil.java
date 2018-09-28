package org.sidiff.common.exceptions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.sidiff.common.CommonPlugin;

public final class ExceptionUtil {

	private ExceptionUtil() {
		throw new AssertionError();
	}

	/**
	 * <p>Returns a status wrapping the exception. The message of the
	 * status is determined depending on the type of the exception.</p>
	 * <ul>
	 * <li>If the exception is <code>null</code>, an {@link Status#OK_STATUS OK-status} is assumed.</li>
	 * <li>If the exception is a {@link SiDiffException} or {@link SiDiffRuntimeException},
	 * the short message it provides is used for the status.</li>
	 * <li>If the exception is an {@link OperationCanceledException},
	 * the {@link Status#CANCEL_STATUS Cancel-status} is returned.</li>
	 * <li>Else, the message will be a generic error message. If the exception
	 * is a {@link CoreException}, it's severity will be used, else the severity 
	 * is always {@link IStatus#ERROR}</li>
	 * </ul>
	 * @param e the exception
	 * @return status wrapping the exception
	 */
	public static IStatus toStatus(Throwable e) {
		if(e == null) {
			return Status.OK_STATUS;
		} else if(e instanceof SiDiffException) {
			return new Status(IStatus.ERROR, CommonPlugin.ID, ((SiDiffException)e).getShortMessage(), e);
		} else if(e instanceof SiDiffRuntimeException) {
			return new Status(IStatus.ERROR, CommonPlugin.ID, ((SiDiffRuntimeException)e).getShortMessage(), e);
		} else if(e instanceof OperationCanceledException) {
			return Status.CANCEL_STATUS;
		} else if(e instanceof CoreException) {
			// Note that we do not return e.getStatus directly, as this hides the original stack trace
			return new Status(((CoreException)e).getStatus().getSeverity(), CommonPlugin.ID, "An exception occurred", e);
		}
		return new Status(IStatus.ERROR, CommonPlugin.ID, "An exception occurred", e);
	}
}
