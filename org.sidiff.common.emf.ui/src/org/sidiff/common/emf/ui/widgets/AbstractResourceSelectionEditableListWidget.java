package org.sidiff.common.emf.ui.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ViewerFilter;
import org.sidiff.common.ui.widgets.AbstractEditableListWidget;

public abstract class AbstractResourceSelectionEditableListWidget<T> extends AbstractEditableListWidget<T> {

	/**
	 * The initially selected resource in the selection dialog, <code>null</code> if none.
	 */
	private IResource initialSelection;

	private List<ViewerFilter> viewerFilters;

	public AbstractResourceSelectionEditableListWidget() {
	}

	public void setInitialSelection(IResource initialSelection) {
		this.initialSelection = initialSelection;
	}

	public IResource getInitialSelection() {
		return initialSelection;
	}

	public void setViewerFilters(List<ViewerFilter> viewerFilters) {
		if(viewerFilters == null || viewerFilters.isEmpty()) {
			this.viewerFilters = null;
		} else {
			this.viewerFilters = new ArrayList<>(viewerFilters);			
		}
	}

	public List<ViewerFilter> getViewerFilters() {
		return viewerFilters == null ? null : Collections.unmodifiableList(viewerFilters);
	}
}