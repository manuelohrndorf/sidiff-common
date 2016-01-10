package org.sidiff.common.settings.settings;

import org.sidiff.common.emf.access.Scope;

public class SiDiffSettings extends AbstractSettings {
	/**
	 * The {@link Scope} of the comparison. (Default: {@link Scope#RESOURCE}.
	 */
	private Scope scope = Scope.RESOURCE;

	
	public SiDiffSettings() {
		this(Scope.RESOURCE_SET);
	}


	public SiDiffSettings(Scope scope) {
		this.scope=scope;
	}
	
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
			this.notifyListeners(SiDiffSettingsItem.SCOPE);
		}
	}

}
