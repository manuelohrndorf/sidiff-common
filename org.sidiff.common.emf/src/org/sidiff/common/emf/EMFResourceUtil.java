package org.sidiff.common.emf;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.access.EObjectLocation;

public class EMFResourceUtil {

	public static EObjectLocation locate(Resource model, EObject eObject) {
		// RESOURCE_INTERNAL..?
		if (eObject.eResource() == model) {
			return EObjectLocation.RESOURCE_INTERNAL;
		}

		// RESOURCE_SET_INTERNAL..?
		for (Resource r : model.getResourceSet().getResources()) {
			if (r == eObject.eResource()) { // r != model
				return EObjectLocation.RESOURCE_SET_INTERNAL;
			}
		}

		// Must be found in PACKAGE_REGISTRY
		assert (EPackage.Registry.INSTANCE.containsValue(eObject.eClass().getEPackage())) : "" + eObject;

		return EObjectLocation.PACKAGE_REGISTRY;
	}
}
