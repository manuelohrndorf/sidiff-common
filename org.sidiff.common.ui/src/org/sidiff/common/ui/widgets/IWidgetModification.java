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
	 * <p>Returns the selection of the widget.</p>
	 * <p>Note that this method should return all selected values
	 * directly and not return aggregates (e.g. incremental matchers etc.).</p>
	 * <p>The list may also be empty or only have one element for widgets
	 * that only have a single value.</p>
	 * @return the selection
	 */
	List<T> getSelection();

	/**
	 * <p>Sets the selection of the widget.</p>
	 * <p>The selection should contain the values directly and not
	 * contain aggregate values (e.g. incremental matchers etc.).</p>
	 * <p>The list may also be empty or only have one element for widgets
	 * that only have a single value.</p>
	 * @param selection
	 * @throws IllegalArgumentException if the selection is not legal
	 */
	void setSelection(List<T> selection);

	/**
	 * <p>Returns all values that can be selected in this widget.</p>
	 * <p>Returns an empty list if it would not make sense to
	 * list all values, e.g. for primitive types.</p>
	 * @return all selectable values
	 */
	List<T> getSelectableValues();
	
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
