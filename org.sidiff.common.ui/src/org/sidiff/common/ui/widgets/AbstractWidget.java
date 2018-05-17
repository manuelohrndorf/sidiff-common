package org.sidiff.common.ui.widgets;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.widgets.Composite;

/**
 * <p>Abstract widget class with common widget functionality.<p>
 * <p>Implements {@link IWidget} and {@link IWidgetDependence} and provides
 * default implementations for widgets dependency management.</p>
 * @author Robert M�ller
 *
 */
public abstract class AbstractWidget implements IWidget, IWidgetDependence {

	private boolean enabled;

	private List<IWidgetDependence> dependents;

	private ControlEnableState enableState;

	public AbstractWidget() {
		this.enabled = true;
		this.dependents = new LinkedList<IWidgetDependence>();
	}

	//
	// IWidget

	/**
	 * <p>Sets the widget's layout data.</p>
	 * <p>The default implementation provided by this abstract class sets the layout data
	 * of the widget returned by {@link IWidget#getWidget()}.</p>
	 * <p>Subclasses may override to customize this behavior.</p>
	 */
	@Override
	public void setLayoutData(Object layoutData) {
		getWidget().setLayoutData(layoutData);
	}

	//
	// IWidgetDependency

	@Override
	public void addDependent(IWidgetDependence dependent) {
		dependents.add(dependent);
	}

	/**
	 * <p>Enables/disables this widget and propagates the state to all dependent widgets.</p>
	 * <p>The default implementation stores the enabled-state and enables/disables
	 * the widget returned by {@link IWidget#getWidget()} and all of its children.</p>
	 * <p>Subclasses may override {@link #updateWidgetEnabledState(boolean)} to supply
	 * custom logic to disable the widget's control.</p>
	 * <p>When overriding this method, subclasses should call {@link #propagateEnabledState()}
	 * to update the enabled-state of dependents according to the result of {@link #areDependentsEnabled()}.</p>
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;

		// enable/disable this widgets control
		updateWidgetEnabledState(enabled);

		// propagate the state to all dependent widgets
		propagateEnabledState();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * <p>Returns whether the widgets depending on this widget are enabled.</p>
	 * <p>The default implementation returns whether this widget {@link #isEnabled() is enabled}.</p>
	 * <p>Subclasses may override, but should generally also call the implementation of the superclass.</p>
	 */
	@Override
	public boolean areDependentsEnabled() {
		return enabled;
	}

	/**
	 * <p>Enables/disables this widget's control.</p>
	 * <p>The default implementation disables the widget's control and all of its children.</p>
	 * <p>Subclasses may override.</p>
	 * @param enabled <code>true</code> to enable this widget's control, <code>false</code> to disable them
	 */
	protected void updateWidgetEnabledState(boolean enabled) {
		Composite composite = getWidget();
		if(composite != null) {
			if(enabled) {
				if(enableState != null) {
					enableState.restore();
					enableState = null;
				}
			} else {
				if(enableState == null) {
					enableState = ControlEnableState.disable(composite);
				}
			}
		}
	}

	/**
	 * <p>Propagates the enables-state returned by {@link #areDependentsEnabled()} to all dependent widgets.</p>
	 * <p>Should be called when the state returned by {@link #areDependentsEnabled()} changes.</p>
	 */
	public void propagateEnabledState() {
		final boolean enabled = areDependentsEnabled();
		for(IWidgetDependence client : dependents) {
			client.setEnabled(enabled);
		}
	}
}
