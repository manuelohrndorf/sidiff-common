package org.sidiff.common.emf.ui.widgets;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.sidiff.common.ui.util.UIUtil;

/**
 * Generic widget to select a project in the workspace.
 * @author rmueller
 */
public class ProjectSelectionWidget extends AbstractResourceSelectionEditableListWidget<IProject> {

	public ProjectSelectionWidget() {
		setTitle("Project");
		getViewerFilters().add(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return element instanceof IProject;
			}
		});
		setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IProject)element).getName();
			}
			@Override
			public Image getImage(Object element) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
			}
		});
	}

	@Override
	protected List<IProject> selectNewElements() {
		IContainer containers[] = WorkspaceResourceDialog.openFolderSelection(UIUtil.getActiveShell(),
				"Add new project", "Select a new '" + getTitle() + "' to add to the selection.", true,
				getInitialSelection().toArray(), getViewerFilters());
		return Stream.of(containers).filter(IProject.class::isInstance).map(IProject.class::cast).collect(Collectors.toList());
	}
}