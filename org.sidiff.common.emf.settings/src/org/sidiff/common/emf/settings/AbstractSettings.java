package org.sidiff.common.emf.settings;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.sidiff.common.emf.settings.internal.SettingsPlugin;

/**
 * Abstract implementation of {@link ISettings} managing a list of
 * {@link ISettingsChangedListener listeners} that can be notified
 * by subclasses by calling {@link #notifyListeners}. Subclasses
 * must implement {@link #validate(MultiStatus)} to add validation
 * results to the multi status.
 */
public abstract class AbstractSettings implements ISettings {

	/**
	 * All listeners of this Setting-Object.
	 */
	private final ListenerList<ISettingsChangedListener> listeners = new ListenerList<>();

	/**
	 * Cached status describing the validity of this Settings-Object.
	 */
	private MultiStatus status;

	@Override
	public void addSettingsChangedListener(ISettingsChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeSettingsChangedListener(ISettingsChangedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Call this function every time when a setting was changed!
	 * 
	 * @param item The Enumeration item associated with the changed setting.
	 */
	protected void notifyListeners(ISettingsItem item) {
		// reset cached validation result
		status = null;

		for (ISettingsChangedListener listener : listeners) {
			try {
				listener.settingsChanged(item);
			} catch (Exception e) {
				Platform.getLog(Platform.getBundle(SettingsPlugin.PLUGIN_ID))
					.log(new Status(IStatus.WARNING, SettingsPlugin.PLUGIN_ID, "Settings listener threw exception", e));
			}
		}
	}

	@Override
	public IStatus validate() {
		if(status == null) {
			status = new MultiStatus(SettingsPlugin.PLUGIN_ID, 0, "The settings were validated.", null);
			validate(status);
		}
		return status;
	}

	/**
	 * Initializes all required, unset parameters to default values for the specific document types.
	 * The set of document types will be empty for the generic document type.
	 * @param documentTypes the document types, empty if generic
	 */
	public abstract void initDefaults(Set<String> documentTypes);

	/**
	 * Initializes all required, unset parameters to default values using generic extensions.
	 * Convenience method equivalent to <code>initDefaults(Collections.emptySet())</code>.
	 */
	public void initDefaults() {
		initDefaults(Collections.emptySet());
	}

	/**
	 * Validates the settings. Adds {@link IStatus} to the {@link MultiStatus}.<br>
	 * <b>Super must be called: <pre>super.validateSettings(multiStatus);</pre></b>
	 * @param multiStatus the multi status
	 */
	protected abstract void validate(MultiStatus multiStatus);
}
