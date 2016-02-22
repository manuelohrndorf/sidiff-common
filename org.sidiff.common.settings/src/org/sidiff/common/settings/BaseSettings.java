package org.sidiff.common.settings;

import org.sidiff.common.emf.access.Scope;

public class BaseSettings extends AbstractSettings {
	/**
	 * The {@link Scope} of the comparison. (Default: {@link Scope#RESOURCE}.
	 */
	private Scope scope = Scope.RESOURCE;

	/**
	 * Validation of the input models. (Default: False)
	 */
	private boolean validate = false;
	
	public BaseSettings() {
		super();
	}


	public BaseSettings(Scope scope, boolean validate) {
		this.scope = scope;
		this.validate = validate;
	}
	
	@Override
	public boolean validateSettings() {
		return scope!=null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(getScope() != null ? "Scope: " + getScope() + "\n" : "");
		result.append("Validate input models: " + isValidate());
		
		return result.toString();
	}
	
	// ---------- Getter and Setter Methods----------
	
	/**
	 * @return The {@link Scope} of the comparison.
	 */
	public Scope getScope() {
		return scope;
	}
	
	/**
	 * Setup the new {@link Scope} of the comparison.
	 * 
	 * @param scope
	 *            The new {@link Scope}.
	 */
	public void setScope(Scope scope) {
		if (this.scope == null || !this.scope.equals(scope)) {
			this.scope = scope;
			this.notifyListeners(BaseSettingsItem.SCOPE);
		}
	}
	
	/**
	 * Get the validation of the input models. (Default: False)
	 * 
	 * @return <code>true</code> if the input models will be validated;
	 *         <code>false</code> otherwise.
	 */
	public boolean isValidate() {
		return validate;
	}
	
	/**
	 * Set the validation of the input models. (Default: False)
	 * 
	 * @param validate
	 *            <code>true</code> if the input models should be validated;
	 *            <code>false</code> otherwise.
	 */
	public void setValidate(boolean validate) {
		if (this.validate != validate) {
			this.validate = validate;
			this.notifyListeners(BaseSettingsItem.VALIDATE);
		}
	}
}
