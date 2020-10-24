package org.sidiff.common.ui.widgets;

/**
 * The IWidgetDisposable should be implemented by classes implementing
 * {@link IWidget}, that need to be disposed after no longer being needed.
 * A widget can only be disposed once and cannot be reused.
 * @author rmueller
 */
public interface IWidgetDisposable {

	/**
	 * Disposes of this widget. Frees all resources and detaches all listeners.
	 * Disposing {@link Control}s created by {@link IWidget#createControl(Composite)} is not required.
	 * This method will only be called once.
	 */
	void dispose();
}
