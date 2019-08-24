package org.sidiff.common.emf.ui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.sidiff.common.exceptions.ExceptionUtil;
import org.sidiff.common.ui.util.UIUtil;

public class FileSelectionWidget<T> extends AbstractResourceSelectionEditableListWidget<T> {

	private IModelLoader<T> modelLoader;

	public FileSelectionWidget(IModelLoader<T> modelLoader) {
		this.modelLoader = Objects.requireNonNull(modelLoader);
	}

	@Override
	protected List<T> selectNewElements() {
		IFile files[] = WorkspaceResourceDialog.openFileSelection(UIUtil.getActiveShell(), 
				"Add new file", "Select a new '" + getTitle() + "' to add to the selection.", true,
				getInitialSelection() == null ? null : new Object[] { getInitialSelection() }, getViewerFilters());
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
				IFile file = (IFile)element;
				return file.getFullPath().toString();
			};
		});
		return widget;
	}

	public interface IModelLoader<T> {
		T load(IFile file) throws Exception;
	}
}