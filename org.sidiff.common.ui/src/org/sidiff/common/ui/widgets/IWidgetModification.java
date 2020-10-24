package org.sidiff.common.ui.widgets;

import java.util.Collections;
import java.util.List;

/**
 * Widgets implementing {@link IWidgetModification} manage a selection
 * of generic type as well as {@link ModificationListener}s,
 * which are notified when the value of the widget changes.
 * @author rmueller
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
	 * <p>Returns the first element of the selection of this widget, or <code>null</code> if the selection is empty.</p>
	 * <p>This method may only be used when there can only be 0 or 1 selected element.</p>
	 * @return selected element, or <code>null</code> if none
	 */
	default T getSingleSelection() {
		List<T> selection = getSelection();
		switch(selection.size()) {
			case 0: return null;
			case 1: return selection.get(0);
			default: throw new IllegalStateException("Selection may not contain multiple elements when using this method");
		}
	}

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
	 * Convenience method for single-valued selection.
	 * {@link #setSelection(List) Sets the selection} to only the given element,
	 * or empty if <code>null</code>.
	 * @param selection the element to select, <code>null</code> for empty selection
	 */
	default void setSelection(T selection) {
		if(selection == null) {
			setSelection(Collections.emptyList());
		} else {
			setSelection(Collections.singletonList(selection));
		}
	}

	/**
	 * <p>Returns all values that can be selected in this widget.</p>
	 * <p>Returns <code>null</code> if all values assignable to T are allowed, e.g. for primitive types.</p>
	 * @return all selectable values, <code>null</code> to allow all assignable
	 */
	List<T> getSelectableValues();

	/**
	 * Adds a {@link ModificationListener} to this widget.
	 * @param listener the listener
	 */
	void addModificationListener(ModificationListener<? super T> listener);

	/**
	 * Removes a {@link ModificationListener} from this widget.
	 * @param listener the listener
	 */
	void removeModificationListener(ModificationListener<? super T> listener);

	/**
	 * A modification listener is notified by a widget after its value changes.
	 * @author rmueller
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
		void onModify(List<? extends T> oldValues, List<? extends T> newValues);
	}
}
