package org.sidiff.common.emf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.common.emf.access.EObjectLocation;
import org.sidiff.common.emf.access.ExternalManyReference;
import org.sidiff.common.emf.access.ExternalReference;
import org.sidiff.common.emf.access.ExternalReferenceContainer;
import org.sidiff.common.emf.access.Scope;

/**
 * Calculates all external references from the viewpoint of the given model and
 * with respect to the selected resource scope.
 * 
 * Note that we apply two filters when iterating over all EReferences of the
 * meta-model:
 * <ul>
 * <li>EReferences that are unchangeable or derived (see
 * {@link EMFMetaAccessEx#isUnconsideredStructualFeature(EStructuralFeature)})</li>
 * <li>Containment EReferences (because we assume that containments can only
 * point to a target object within the same resource)</li>
 * </ul>
 * 
 * @author kehrer
 */
public class ExternalReferenceCalculator {

	private List<ExternalReference> registryReferences;
	private List<ExternalReference> resourceSetReferences;
	private Scope scope;

	/**
	 * Calculates all external references from the viewpoint of the given model
	 * and with respect to the selected resource scope.
	 * 
	 * @param model
	 * @param scope
	 *            
	 * @return
	 */
	public ExternalReferenceContainer calculate(Resource model, Scope scope) {
		this.registryReferences = new ArrayList<>();
		this.resourceSetReferences = new ArrayList<>();
		this.scope = scope;

		// Find external references, i.e.
		// RESOURCE -> PACKAGE_REGISTRY, and
		// RESOURCE -> RESOURCE_SET
		calculate(model);

		// Also add external References from RESOURCE_SET to
		// PACKAGE_REGISTRY (when modus = COMPLETE RESOURCE SET)
		if (scope == Scope.RESOURCE_SET) {
			for (Resource r : model.getResourceSet().getResources()) {
				if (r == model) {
					continue;
				}
				calculate(r);
			}
		}

		return new ExternalReferenceContainer(registryReferences, resourceSetReferences);
	}

	private void calculate(Resource model) {
		for (EObject eObject : CollectionUtil.asIterable(model.getAllContents())) {
			// Check all class features (also inherited)
			for (EStructuralFeature eStructuralFeature : eObject.eClass().getEAllStructuralFeatures()) {
				// Check only changeable and not derived features
				if (EMFMetaAccess.isUnconsideredStructualFeature(eStructuralFeature)
						|| !(eStructuralFeature instanceof EReference)) {
					continue;
				}
				// Check references but do not check the containments
				EReference eReference = (EReference)eStructuralFeature;
				if(eReference.isContainment()) {
					continue;
				}

				// Check the objects reference targets for imports
				if (eReference.isMany()) {
					@SuppressWarnings("unchecked")
					List<EObject> targets = (List<EObject>) eObject.eGet(eReference);
					for (int i = 0; i < targets.size(); i++) {
						EObject target = targets.get(i);
						EObjectLocation location = EMFResourceUtil.locate(model, target);
						if (location == EObjectLocation.PACKAGE_REGISTRY) {
							registryReferences.add(new ExternalManyReference(eObject, eReference, target, i));
						} else if (location == EObjectLocation.RESOURCE_SET_INTERNAL && scope == Scope.RESOURCE) {
							resourceSetReferences.add(new ExternalManyReference(eObject, eReference, target, i));
						}
					}
				} else {
					EObject target = (EObject) eObject.eGet(eReference);
					if (target != null) {
						EObjectLocation location = EMFResourceUtil.locate(model, target);
						if (location == EObjectLocation.PACKAGE_REGISTRY) {
							registryReferences.add(new ExternalReference(eObject, eReference, target));
						} else if (location == EObjectLocation.RESOURCE_SET_INTERNAL && scope == Scope.RESOURCE) {
							resourceSetReferences.add(new ExternalReference(eObject, eReference, target));
						}
					}
				}
			}
		}
	}
}
