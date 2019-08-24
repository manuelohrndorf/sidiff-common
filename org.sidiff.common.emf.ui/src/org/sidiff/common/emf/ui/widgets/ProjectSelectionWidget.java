package org.sidiff.common.emf.ui.widgets;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.sidiff.common.ui.util.UIUtil;

public class ProjectSelectionWidget extends AbstractResourceSelectionEditableListWidget<IProject> {

	public ProjectSelectionWidget() {
		setTitle("Project");
		setViewerFilters(Collections.singletonList(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return element instanceof IProject;
			}
		}));
		setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IProject project = (IProject)element;
				return project.getName();
			}
		});
	}

	@Override
	protected List<IProject> selectNewElements() {
		IContainer containers[] = WorkspaceResourceDialog.openFolderSelection(UIUtil.getActiveShell(), 
				"Add new project", "Select a new '" + getTitle() + "' to add to the selection.", true,
				getInitialSelection() == null ? null : new Object[] { getInitialSelection() }, getViewerFilters());
		return Stream.of(containers).filter(IProject.class::isInstance).map(IProject.class::cast).collect(Collectors.toList());
	}
}