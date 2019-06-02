package org.sidiff.common.ui.widgets;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * <p>Abstract widget class with common widget functionality.<p>
 * <p>Implements {@link IWidget}, {@link IWidgetDependence}, {@link IWidgetCallback} and {@link IWidgetValidation}.
 * and provides default implementations for widgets dependency management and widget callbacks.</p>
 * <p>Implements widget validation, storing the validation message provided by {@link #doValidate()}.
 * The widget is responsible for requesting validation, when its value changes, using the callbacks.</p>
 * @author Robert Müller
 */
public abstract class AbstractWidget implements IWidget, IWidgetDependence, IWidgetCallback, IWidgetValidation {

	private boolean enabled;

	private IWidgetDependence dependency;
	private List<IWidgetDependence> dependents;

	private ControlEnableState enableState;

	/**
	 * The widget container callback.
	 * Initially a Null-Implementation for compatibility with widget pages that do not set the callbacks.
	 */
	private IWidgetCallback.Callback callback = IWidgetCallback.Callback.NULL;
	
	/**
	 * The cached validation message result of {@link #doValidate()}.
	 */
	private ValidationMessage validationMessage = ValidationMessage.OK;

	public AbstractWidget() {
		this.enabled = true;
		this.dependents = new LinkedList<>();
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
		if(!dependents.contains(dependent)) {
			dependents.add(dependent);
			dependent.setDependency(this);
		}
	}

	@Override
	public void setDependency(IWidgetDependence dependency) {
		if(this.dependency != dependency) {
			this.dependency = dependency;
			dependency.addDependent(this);
			setEnabled(dependency.areDependentsEnabled());
		}
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
		boolean old = this.enabled;
		this.enabled = enabled;

		// enable/disable this widgets control
		updateWidgetEnabledState(enabled);

		// propagate the state to all dependent widgets
		if(old != enabled) {
			propagateEnabledState();
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the visibility of this widget. An invisible widget takes up no space.
	 * Has no effect if the widget has not been created yet.
	 * @param visible <code>true</code> to make visible, <code>false</code> to make invisible
	 */
	public void setVisible(boolean visible) {
		Composite composite = getWidget();
		if(composite != null) {
			composite.setVisible(visible);
			((GridData)composite.getLayoutData()).exclude = !visible;
			composite.requestLayout();
			getWidgetCallback().requestLayout();
		}
	}

	/**
	 * Returns whether this widget is currently visible. An invisible widget takes up no space.
	 * @return <code>true</code> if visible, <code>false</code> if invisible or not yet created
	 */
	public boolean isVisible() {
		return getWidget() != null && getWidget().isVisible();
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

	@Override
	public final void setWidgetCallback(IWidgetCallback.Callback callback) {
		this.callback = callback;
	}

	/**
	 * Returns this widget's {@link Callback}.
	 * If no callback was set, this method does <u>not</u> return <code>null</code>,
	 * but a {@link IWidgetCallback.Callback#NULL null-implementation}.
	 * @return the callback
	 */
	protected final IWidgetCallback.Callback getWidgetCallback() {
		return callback;
	}

	@Override
	public final ValidationMessage validate() {
		validationMessage = Objects.requireNonNull(doValidate(), "doValidate must not return null");
		return validationMessage;
	}

	@Override
	public final ValidationMessage getValidationMessage() {
		return validationMessage;
	}

	protected ValidationMessage doValidate() {
		return ValidationMessage.OK;
	}
}
