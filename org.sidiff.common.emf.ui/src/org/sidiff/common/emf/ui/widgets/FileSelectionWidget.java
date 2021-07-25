package org.sidiff.common.emf.ui.widgets;

import java.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.sidiff.common.exceptions.ExceptionUtil;
import org.sidiff.common.ui.util.UIUtil;

/**
 * <p>Generic widget to select files in the workspace.</p>
 * <p>A model loader interface must be specified to convert the selected IFile
 * into the generic type T of this class.</p>
 * <p>Use {@link #createBasicFileSelectionWidget()} to create a basic widget,
 * which does not convert the file and simply allows file selection in itself.</p>
 * @author rmueller
 * @param <T> the type of input elements
 */
public class FileSelectionWidget<T> extends AbstractResourceSelectionEditableListWidget<T> {

	private IModelLoader<T> modelLoader;

	public FileSelectionWidget(IModelLoader<T> modelLoader) {
		this.modelLoader = Objects.requireNonNull(modelLoader);
	}

	@Override
	protected List<T> selectNewElements() {
		IFile files[] = WorkspaceResourceDialog.openFileSelection(UIUtil.getActiveShell(),
				"Add new file", "Select a new '" + getTitle() + "' to add to the selection.", true,
				getInitialSelection().toArray(), getViewerFilters());
		List<T> addedFiles = new ArrayList<>();
		for(IFile file : files) {
			try {
				T model = modelLoader.load(file);
				if(!addedFiles.contains(model)) {
					addedFiles.add(model);
				}
			} catch (Exception e) {
				ErrorDialog.openError(UIUtil.getActiveShell(), "Invalid file selected",
					"The file " + file.getFullPath() + " could not be loaded as '" + getTitle() + "'",
					ExceptionUtil.toStatus(e));
			}
		}
		return addedFiles;
	}

	public static FileSelectionWidget<IFile> createBasicFileSelectionWidget() {
		FileSelectionWidget<IFile> widget = new FileSelectionWidget<>(file -> file);
		widget.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IFile)element).getFullPath().toString();
			}
			@Override
			public Image getImage(Object element) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			}
		});
		return widget;
	}

	public interface IModelLoader<T> {
		T load(IFile file) throws Exception;
	}
}