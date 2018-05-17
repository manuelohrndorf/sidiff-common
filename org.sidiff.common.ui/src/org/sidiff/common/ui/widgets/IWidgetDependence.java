package org.sidiff.common.ui.widgets;

/**
 * The {@link IWidgetDependence} interface can be implemented by classes implementing {@link IWidget}.
 * Widgets implementing this interface can have dependent widgets, can be enabled/disabled and must
 * specify whether dependent widgets are enabled.
 * 
 * @author Robert Müller
 *
 */
public interface IWidgetDependence {

	/**
	 * Adds the given widget to the list of widgets that depend on this widget.
	 * @param dependent the widget that depends on this
	 */
	void addDependent(IWidgetDependence dependent);

	/**
	 * Enables/disables this widget and propagates the state to all dependent widgets.
	 * @param enabled <code>true</code> to enable this widget, <code>false</code> to disable it
	 */
	void setEnabled(boolean enabled);

	/**
	 * Returns whether this widget is enabled.
	 * @return <code>true</code> if this widget is enabled, <code>false</code> otherwise
	 */
	boolean isEnabled();

	/**
	 * Returns whether the widgets depending on this widgets are enabled.
	 * @return <code>true</code> if dependents are enabled, <code>false</code> otherwise
	 */
	boolean areDependentsEnabled();
}
