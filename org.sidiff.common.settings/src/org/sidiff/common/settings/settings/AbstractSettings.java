package org.sidiff.common.settings.settings;

import java.util.ArrayList;

public abstract class AbstractSettings {
	/**
	 * All listeners of this Setting-Object.
	 */
	private final ArrayList<ISettingsChangedListener> listeners = new ArrayList<ISettingsChangedListener>();

	/**
	 * Adds a new listener to this Settings-Object.
	 * 
	 * @param listener
	 *            The listener.
	 */
	public void addSettingsChangedListener(ISettingsChangedListener listener) {
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}

	/**
	 * Removes a new listener to this Settings-Object.
	 * 
	 * @param listener
	 *            The listener.
	 */
	public void removeSettingsChangedListener(ISettingsChangedListener listener) {
		if (this.listeners.contains(listener)) {
			this.listeners.remove(listener);
		}
	}

	/**
	 * Call this function every time when a setting was changed!
	 * 
	 * @param item
	 *            The Enumeration associated with the changed setting.
	 */
	protected void notifyListeners(Enum<?> item) {
		if (listeners != null) {
			for (ISettingsChangedListener listener : listeners) {
				try {
					listener.settingsChanged(item);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public abstract boolean validateSettings();

}
