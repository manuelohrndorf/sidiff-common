package org.sidiff.common.ui.widgets;

import org.eclipse.swt.events.SelectionListener;

/**
 * @deprecated {@link IWidgetCallback} and {@link AbstractWidget}
 * should be used instead to request validation when necessary.
 * {@link IWidgetModification} and {@link AbstractModifiableWidget}
 * should be used to receive better widget value updates.
 */
public interface IWidgetSelection {

	/**
	 * Add a selection listener to the widget control(s).
	 *
	 * @param listener The new listener.
	 */
	void addSelectionListener(SelectionListener listener);

	/**
	 * Remove a selection listener from the widget control(s).
	 *
	 * @param listener The listener to remove.
	 */
	void removeSelectionListener(SelectionListener listener);
}
