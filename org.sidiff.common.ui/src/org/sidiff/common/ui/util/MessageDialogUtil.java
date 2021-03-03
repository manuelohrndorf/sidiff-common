package org.sidiff.common.ui.util;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.ui.statushandlers.StatusManager;
import org.sidiff.common.exceptions.ExceptionUtil;

/**
 * A class that provides static methods to create {@link MessageDialog}s for
 * errors, messages and runnables with progress.
 * @author Adrian Bingener
 * @author rmueller
 */
public final class MessageDialogUtil {

	private MessageDialogUtil() {
		throw new AssertionError();
	}

	public static void showErrorDialog(String title, String message) {
		MessageDialog.open(MessageDialog.ERROR, UIUtil.getActiveShell(), title, message, SWT.NONE);
	}

	public static void showMessageDialog(String title, String message) {
		MessageDialog.open(MessageDialog.INFORMATION, UIUtil.getActiveShell(), title, message, SWT.NONE);
	}

	public static void showProgressDialog(IRunnableWithProgress runnable) {
		try {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(UIUtil.getActiveShell());
			dialog.run(false, false, runnable);
		} catch (InvocationTargetException e) {
			StatusManager.getManager().handle(
					ExceptionUtil.toStatus(e.getTargetException()),
					StatusManager.LOG|StatusManager.SHOW);
		} catch (InterruptedException e) {
			// operation was canceled
		}
	}
}