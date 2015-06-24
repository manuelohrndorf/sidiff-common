package org.sidiff.common.services;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;

/**
 * This BundleActivator waits until the required bundle(s) specified by the
 * Manifest are activated. It also disables the Bundels functionality when a 
 * required Bundel is not longer available.
 * 
 */
public abstract class DependentBundleActivator implements BundleActivator, BundleListener {

	private boolean enabled = false;

	private Set<Bundle> requiredBundels = null;
	private BundleContext context = null;

	/**
	 * This methods contains the code to be executed, when the specified bundle
	 * becomes activated.
	 * 
	 * @param context
	 * @param reference
	 */
	public void start(BundleContext context) {

		// Init fields
		this.requiredBundels = new HashSet<Bundle>();
		this.context = context;

		// Init required Bundels
		Dictionary<String, String> headers = (Dictionary<String, String>) context.getBundle().getHeaders();
		initRequirements(headers.get(Constants.REQUIRE_BUNDLE));

		// Register as listener regarding bundel changes
		context.addBundleListener(this);

		// immediately activation when requirements complies.
		if (checkRequirements()) {
			this.startOnlyAfterRequired(context);
			this.enabled = true;
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {

		// unregister listener first!
		this.context.removeBundleListener(this);

		// stop when still activated, cannot infer to events anymore
		if (enabled) {
			stopWithoutRequired(context);
			enabled = false;
		}

		// Cleanup
		this.context = null;
		this.requiredBundels = null;
	}

	@Override
	public void bundleChanged(BundleEvent event) {

		if (this.requiredBundels.contains(event.getBundle())) {
			if (!this.enabled && checkRequirements()) {
				// Activation
				this.startOnlyAfterRequired(this.context);
				this.enabled = true;
			} else if (this.enabled) {
				// Deactivation
				this.stopWithoutRequired(this.context);
				this.enabled = false;
			}
		}
	}

	private void initRequirements(String string) {
		
		String bundelStrings[] = string.split(",");
		for(String requiredBundelString : bundelStrings){
			String requiredSymbolicName = requiredBundelString.split(";")[0];
			Bundle bundels[] = this.context.getBundles();
			for(Bundle bundel : bundels){
				if(bundel.getSymbolicName().equals(requiredSymbolicName)){
					this.requiredBundels.add(bundel);
					break;
				}
			}
		}

	}

	/**
	 * Checks whether the specified bundle is active.
	 * 
	 * @param context
	 * @param bundleName
	 * @return
	 */
	private boolean checkRequirements() {

		boolean allActive = true;
		for (Bundle bundle : this.requiredBundels) {
			allActive &= bundle.getState() == Bundle.ACTIVE;
		}
		return allActive;
	}

	/**
	 * This methods contains the code to be executed, when all required bundles
	 * becomes activated.
	 * 
	 * @param context
	 */
	public abstract void startOnlyAfterRequired(BundleContext context);

	public abstract void stopWithoutRequired(BundleContext context);

}
