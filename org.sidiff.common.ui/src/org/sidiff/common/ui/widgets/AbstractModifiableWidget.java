package org.sidiff.common.ui.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * <p>Abstract widget with type argument to also implement {@link IWidgetModification}.</p>
 * <p>Implements a list of selected values, a collection of modification listeners, and
 * functions to propagate changes to the listeners. {@link #setSelection(List)} is
 * implemented to propagate changes. Override {@link #hookSetSelection()} to implement
 * custom handling for selections.</p>
 * <p>Has a label provider which is used by subclasses to get label and icon for
 * the values of the type T. The default label provider is <code>null</code>,
 * which uses {@link Object#toString()}. </p>
 * @param <T> the type of the input elements
 * @author Robert Müller
 */
public abstract class AbstractModifiableWidget<T> extends AbstractContainerWidget implements IWidgetModification<T>  {

	private List<T> selection = Collections.emptyList();
	private Collection<ModificationListener<? super T>> modificationListeners = new ArrayList<>();
	private ILabelProvider labelProvider;

	@Override
	public void setSelection(List<T> selection) {
		// remove all values which cannot be selected
		List<T> newSelection = new ArrayList<>(selection);
		List<T> selectable = getSelectableValues();
		if(selectable != null) {
			newSelection.retainAll(selectable);
		}

		if(!Objects.equals(newSelection, this.selection)) {
			List<T> oldSelection = this.selection;
			this.selection = newSelection;
			hookSetSelection();
			propagateValueChange(oldSelection, this.selection);
		}
	}

	/**
	 * <p>Hook method called by {@link #setSelection(List)} after the selection
	 * has been applied and before the new value has been propagated.
	 * Use {@link #getSelection()} to get the current selection.
	 * <u>Do not</u> call {@link #setSelection(List)} from this method.<p>
	 * <p>Override to implement custom handling for new selections.
	 * The default implementation does nothing.</p>
	 */
	protected void hookSetSelection() {
		// default implementation does nothing
	}

	@Override
	public List<T> getSelection() {
		return Collections.unmodifiableList(selection);
	}
	
	@Override
	public void addModificationListener(ModificationListener<? super T> listener) {
		modificationListeners.add(listener);
	}

	@Override
	public void removeModificationListener(ModificationListener<? super T> listener) {
		modificationListeners.remove(listener);
	}

	/**
	 * Propagates a changed value (possibly multiple values) to all registered {@link ModificationListener}s.
	 * @param oldValues the list of old values
	 * @param newValues the list of new values
	 */
	protected void propagateValueChange(List<T> oldValues, List<T> newValues) {
		if(!Objects.equals(oldValues, newValues)) {
			modificationListeners.forEach(listener -> listener.onModify(oldValues, newValues));
		}
	}

	/**
	 * Propagates a changed value (single value to single value) to all registered {@link ModificationListener}s.
	 * If a value is <code>null</code>, an empty list is propagated instead.
	 * @param oldValue the old value
	 * @param newValue the new value
	 */
	protected void propagateValueChange(T oldValue, T newValue) {
		propagateValueChange(
				oldValue == null ? Collections.emptyList() : Collections.singletonList(oldValue),
				newValue == null ? Collections.emptyList() : Collections.singletonList(newValue));
	}

	public ILabelProvider getLabelProvider() {
		if(labelProvider == null) {
			labelProvider = new ColumnLabelProvider(); // initialize to default which uses toString
		}
		return labelProvider;
	}

	public void setLabelProvider(ILabelProvider labelProvider) {
		this.labelProvider = Objects.requireNonNull(labelProvider);
	}
}
