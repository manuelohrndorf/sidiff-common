package org.sidiff.integration.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.sidiff.integration.eclipse.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getStore();
		// store.setDefault(PreferenceConstants.P_BOOLEAN, true);
		// store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
		// store.setDefault(PreferenceConstants.P_STRING,
		// "Default value");
	}

}
