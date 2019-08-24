package org.sidiff.common.emf.ui.widgets;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.sidiff.common.ui.util.UIUtil;

public class FolderSelectionWidget extends AbstractResourceSelectionEditableListWidget<IFolder> {

	public FolderSelectionWidget() {
		setTitle("Folder");
		setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IFolder folder = (IFolder)element;
				return folder.getFullPath().toString();
			}
		});
	}

	@Override
	protected List<IFolder> selectNewElements() {
		IContainer containers[] = WorkspaceResourceDialog.openFolderSelection(UIUtil.getActiveShell(), 
				"Add new folder", "Select a new '" + getTitle() + "' to add to the selection.", true,
				getInitialSelection() == null ? null : new Object[] { getInitialSelection() }, getViewerFilters());
		return Stream.of(containers).filter(IFolder.class::isInstance).map(IFolder.class::cast).collect(Collectors.toList());
	}
}