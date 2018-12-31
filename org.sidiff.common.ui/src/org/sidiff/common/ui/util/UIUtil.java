package org.sidiff.common.ui.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;

public class UIUtil {

	/**
	 * Returns the active shell.
	 * @return active shell, <code>null</code> if none
	 */
	public static Shell getActiveShell() {
		IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return win != null ? win.getShell() : null;
	}

	/**
	 * Execute Eclipse command programmatically.
	 *
	 * @param commandID The command ID.
	 */
	public static void callCommand(String commandID, Map<String, String> paramters) {
		IServiceLocator serviceLocator = PlatformUI.getWorkbench();
		ICommandService commandService = serviceLocator.getService(ICommandService.class);
		IEvaluationService evaluationService = serviceLocator.getService(IEvaluationService.class);

		if (paramters == null) {
			paramters = new HashMap<String, String>();
		}

		try {
			Command theCommand = commandService.getCommand(commandID);
			ExecutionEvent event = new ExecutionEvent(
					theCommand,
					paramters,
					null,
					evaluationService.getCurrentState());

			theCommand.executeWithChecks(event);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open a file with the associated editor.
	 *
	 * @param path
	 *            The path on the file system.
	 * @throws FileNotFoundException
	 * @throws PartInitException 
	 */
	public static void openEditor(String path) throws FileNotFoundException, PartInitException {
		File osFile = new File(path);
		if(!osFile.exists() || !osFile.isFile()) {
			throw new FileNotFoundException("File could not be found: " + osFile);
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = Path.fromOSString(osFile.getAbsolutePath());
		IFile file = workspace.getRoot().getFileForLocation(location);

		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if(window == null) throw new IllegalStateException("No active workbench window available, or not called from UI thread");
		IWorkbenchPage page = window.getActivePage();
		if(page == null) throw new IllegalStateException("The active workbench window has no active page");

		if (file != null) {
			// Open from workspace:
			IDE.openEditor(page, file);
		} else {
			// Open from file system:
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(osFile.toURI());
			IDE.openEditorOnFileStore(page, fileStore);
		}
	}
	
	public static void showMessage(final String message) {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
				MessageDialog.openInformation(getActiveShell(), 
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getTitle(),
						message);
		    }
		});
	}
	
	public static void showError(final String message) {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
				MessageDialog.openError(getActiveShell(), 
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getTitle(),
						message);
		    }
		});
	}
	
	
	/**
	 * This class validates a String.
	 */
	public static class NotEmptyValidator implements IInputValidator {

	  public String isValid(String newText) {
	    int len = newText.length();

	    // Determine if input is empty
	    if (len < 1) return "Empty inputs are not allowed!";

	    // Input must be OK
	    return null;
	  }
	}
}
