package org.sidiff.common.exceptions;

import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

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
			return new Status(IStatus.ERROR, getPluginId(e), ((SiDiffException)e).getShortMessage(), e);
		} else if(e instanceof SiDiffRuntimeException) {
			return new Status(IStatus.ERROR, getPluginId(e), ((SiDiffRuntimeException)e).getShortMessage(), e);
		} else if(e instanceof OperationCanceledException) {
			return Status.CANCEL_STATUS;
		} else if(e instanceof CoreException) {
			// Note that we do not return e.getStatus directly, as this hides the original stack trace
			return new Status(((CoreException)e).getStatus().getSeverity(), getPluginId(e), "An exception occurred", e);
		}
		return new Status(IStatus.ERROR, getPluginId(e), "An exception occurred", e);
	}

	private static String getPluginId(Throwable e) {
		if(e instanceof CoreException) {
			return ((CoreException)e).getStatus().getPlugin();
		}
		final String UNKNOWN_ID = "unknown";
		try {
			Bundle bundle = FrameworkUtil.getBundle(Class.forName(e.getStackTrace()[0].getClassName()));
			return bundle == null ? UNKNOWN_ID : bundle.getSymbolicName();
		} catch (ClassNotFoundException e2) {
			return UNKNOWN_ID;
		}
	}

	/**
	 * Wraps the given throwable in a {@link CoreException} using
	 * {@link #toStatus(Throwable)}. Returns the exception itself,
	 * if it already is a CoreException.
	 * @param e the throwable
	 * @return wrapped throwable
	 */
	public static CoreException asCoreException(Throwable e) {
		if(e instanceof Error) {
			// errors should be thrown as they are
			throw (Error)e;
		} else if(e instanceof CoreException) {
			return (CoreException)e;
		}
		return new CoreException(ExceptionUtil.toStatus(e));
	}

	/**
	 * Throws the given exception without having to declare or catch it,
	 * even if it is not a {@link RuntimeException}.
	 * @param exception the throwable to throw
	 * @throws E the exception
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Throwable> void sneakyThrow(Throwable exception) throws E {
		throw (E)exception;
	}

	/**
	 * Wraps the given consumer, which can throw an exception,
	 * so that the exception is suppressed and the consumer can be
	 * used as a lambda.
	 * @param consumer the consumer, which may throw exceptions
	 * @return a consumer that sneakily re-throws exceptions
	 */
	public static <T> Consumer<T> wrap(UnsafeConsumer<T> consumer) {
		return t -> {
			try {
				consumer.accept(t);
			} catch(Exception e) {
				sneakyThrow(e);
			}
		};
	}

	@FunctionalInterface
	public static interface UnsafeConsumer<T> {
		void accept(T t) throws Exception;
	}
}
