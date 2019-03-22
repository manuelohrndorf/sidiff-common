package org.sidiff.common.emf.settings;

import org.eclipse.core.runtime.IStatus;

public interface ISettings {

	/**
	 * Adds a listener to this Settings-Object.
	 * 
	 * @param listener The listener.
	 */
	void addSettingsChangedListener(ISettingsChangedListener listener);

	/**
	 * Removes a listener from this Settings-Object.
	 * 
	 * @param listener The listener.
	 */
	void removeSettingsChangedListener(ISettingsChangedListener listener);

	/**
	 * Returns an {@link IStatus} describing the validity of this Settings-Object.
	 * @return status the status
	 */
	IStatus validate();
}
