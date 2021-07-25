package org.sidiff.common.emf.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ViewerFilter;
import org.sidiff.common.ui.widgets.AbstractEditableListWidget;

/**
 * @author rmueller
 * @param <T> the type of input elements
 */
public abstract class AbstractResourceSelectionEditableListWidget<T> extends AbstractEditableListWidget<T> {

	private final List<IResource> initialSelection = new ArrayList<>();
	private final List<ViewerFilter> viewerFilters = new ArrayList<>();

	/**
	 * @return Modifiable list of the initially selected resources in the selection dialog, empty if none. Initially empty.
	 */
	public List<IResource> getInitialSelection() {
		return initialSelection;
	}

	/**
	 * @return Modifiable list of viewer filters, empty if none. Initially empty.
	 */
	public List<ViewerFilter> getViewerFilters() {
		return viewerFilters;
	}
}