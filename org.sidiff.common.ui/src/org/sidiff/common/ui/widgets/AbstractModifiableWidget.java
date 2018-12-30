package org.sidiff.common.ui.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>Abstract widget with type argument to also implement {@link IWidgetModification}.</p>
 * <p>Implements a collection of modification listeners and provides the functions
 * {@link #propagateValueChange(List, List)} and {@link #propagateValueChange(Object, Object)}
 * which should be called after the value of the widget changes.</p>
 * @author Robert Müller
 */
public abstract class AbstractModifiableWidget<T> extends AbstractWidget implements IWidgetModification<T>  {

	private Collection<ModificationListener<T>> modificationListeners = new ArrayList<>();

	@Override
	public void addModificationListener(ModificationListener<T> listener) {
		modificationListeners.add(listener);
	}

	@Override
	public void removeModificationListener(ModificationListener<T> listener) {
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
}
