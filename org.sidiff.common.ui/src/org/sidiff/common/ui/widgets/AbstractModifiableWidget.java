package org.sidiff.common.ui.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

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

	private static final BiPredicate<Object,Object> DEFAULT_EQUALITY = Objects::equals;

	private List<T> selection = Collections.emptyList();
	private Collection<ModificationListener<? super T>> modificationListeners = new ArrayList<>();
	private ILabelProvider labelProvider;
	private BiPredicate<T,T> equalityDelegate;

	@Override
	public void setSelection(List<T> selection) {
		BiPredicate<T,T> equality = getEqualityDelegate();

		// remove all values which cannot be selected
		List<T> newSelection = new ArrayList<>(selection);
		List<T> selectable = getSelectableValues();
		if(selectable != null) {
			if(equality != DEFAULT_EQUALITY && !selectable.getClass().getSimpleName().contains("Unmodifiable")) {
				try {
					Set<T> unmatchedNewElements = new HashSet<>(newSelection);
					unmatchedNewElements.removeAll(this.selection);
					// Replace selectable values by equal selected values
					selectable.replaceAll(selItem -> {
						for(Iterator<T> it = unmatchedNewElements.iterator(); it.hasNext(); ) {
							T newItem = it.next();
							if(newItem == selItem || equality.test(newItem, selItem)) {
								it.remove();
								return newItem;
							}
						}
						return selItem;
					});
				} catch(UnsupportedOperationException e) {
					// compatibility to widgets with otherwise unmodifiable getSelectableValues()
				}
			}
			newSelection.removeIf(newItem -> selectable.stream().noneMatch(selItem -> equality.test(newItem, selItem)));
		}

		// Check whether new selection is different from current one (using equality delegate)
		if(newSelection.size() != this.selection.size()
				|| IntStream.range(0, this.selection.size()).anyMatch(i -> !equality.test(newSelection.get(i), this.selection.get(i)))) {
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
	 * The default implementation updates the enabled state of dependent widgets.</p>
	 */
	protected void hookSetSelection() {
		propagateEnabledState();
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

	public BiPredicate<T, T> getEqualityDelegate() {
		if(equalityDelegate == null) {
			equalityDelegate = Objects::equals;
		}
		return equalityDelegate;
	}
	
	public void setEqualityDelegate(BiPredicate<T, T> equalityDelegate) {
		this.equalityDelegate = Objects.requireNonNull(equalityDelegate);
	}

	@Override
	public boolean areDependentsEnabled() {
		return super.areDependentsEnabled() && !getSelection().isEmpty();
	}
}
