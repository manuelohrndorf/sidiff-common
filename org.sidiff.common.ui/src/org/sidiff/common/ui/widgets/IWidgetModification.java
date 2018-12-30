package org.sidiff.common.ui.widgets;

import java.util.List;

/**
 * Widgets implementing {@link IWidgetModification} manage a selection
 * of generic type as well as {@link ModificationListener}s,
 * which are notified when the value of the widget changes.
 * @author Robert Müller
 * @param <T> the type of value this widget broadcasts to its listeners
 */
public interface IWidgetModification<T> {

	/**
	 * Returns the selection of the widget.
	 * Note that this method should return all selected values
	 * and not aggregate them in any way. The list may also be
	 * empty or only have one element for widgets that only
	 * have a single value.
	 * @return the selection
	 */
	List<T> getSelection();

	/**
	 * Adds a {@link ModificationListener} to this widget.
	 * @param listener the listener
	 */
	void addModificationListener(ModificationListener<T> listener);

	/**
	 * Removes a {@link ModificationListener} from this widget.
	 * @param listener the listener
	 */
	void removeModificationListener(ModificationListener<T> listener);

	/**
	 * A modification listener is notified by a widget after its value changes.
	 * @author Robert Müller
	 * @param <T> the type of value
	 */
	interface ModificationListener<T> {

		/**
		 * Called when the value of the widget changes. Note that
		 * the arguments are lists but may be empty or only have a
		 * single element for some widgets.
		 * @param oldValues list of old values
		 * @param newValues list of new values
		 */
		void onModify(List<T> oldValues, List<T> newValues);
	}
}
