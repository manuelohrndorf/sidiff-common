package org.sidiff.eclipse.integrator;

import org.eclipse.ui.IStartup;

public class SidiffShotgun implements IStartup {

	@Override
	public void earlyStartup() {
		BundleHandler.getInstance().startBundles();
	}

}
