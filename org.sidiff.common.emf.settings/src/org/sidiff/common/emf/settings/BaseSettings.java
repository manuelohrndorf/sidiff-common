package org.sidiff.common.emf.settings;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.sidiff.common.emf.access.Scope;
import org.sidiff.common.emf.settings.internal.SettingsPlugin;

/**
 * @see BaseSettingsItem
 */
public class BaseSettings extends AbstractSettings {

	/**
	 * The {@link Scope} of the comparison. (Default: {@link Scope#RESOURCE}.
	 */
	private Scope scope;

	/**
	 * Validation of the input models. (Default: False)
	 */
	private boolean validate;

	public BaseSettings() {
	}
	
	public BaseSettings(Scope scope, boolean validate) {
		this.scope = scope;
		this.validate = validate;
	}

	@Override
	public void initDefaults(Set<String> documentTypes) {
		if(scope == null) {
			scope = getDefaultScope(documentTypes);			
		}
		this.validate = getDefaultValidate(documentTypes);
	}

	protected Scope getDefaultScope(Set<String> documentTypes) {
		return Scope.RESOURCE;
	}

	protected boolean getDefaultValidate(Set<String> documentTypes) {
		return false;
	}

	@Override
	protected void validate(MultiStatus multiStatus) {
		if(scope == null) {
			multiStatus.add(new Status(IStatus.ERROR, SettingsPlugin.PLUGIN_ID, 0, "Scope is not set.", null));
		}
	}

	@Override
	public String toString() {
		return "BaseSettings[" + "Scope: " + getScope() + ", " + "Validate input models: " + isValidate() + "]";
	}

	// ---------- Getter and Setter Methods----------

	/**
	 * @return The {@link Scope} of the comparison.
	 * @see BaseSettingsItem#SCOPE
	 */
	public Scope getScope() {
		return scope;
	}

	/**
	 * Setup the new {@link Scope} of the comparison.
	 * 
	 * @param scope The new {@link Scope}.
	 * @see BaseSettingsItem#SCOPE
	 */
	public void setScope(Scope scope) {
		if (this.scope != scope) {
			this.scope = scope;
			notifyListeners(BaseSettingsItem.SCOPE);
		}
	}

	/**
	 * Get the validation of the input models. (Default: False)
	 * 
	 * @return <code>true</code> if the input models will be validated; <code>false</code> otherwise.
	 * @see BaseSettingsItem#VALIDATE
	 */
	public boolean isValidate() {
		return validate;
	}

	/**
	 * Set the validation of the input models. (Default: False)
	 * 
	 * @param validate <code>true</code> if the input models should be validated; <code>false</code> otherwise.
	 * @see BaseSettingsItem#VALIDATE
	 */
	public void setValidate(boolean validate) {
		if (this.validate != validate) {
			this.validate = validate;
			notifyListeners(BaseSettingsItem.VALIDATE);
		}
	}
}
