package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.*;
import org.sidiff.common.collections.FilterUtil;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.common.emf.collections.EMFSelectors;
import org.sidiff.core.annotation.Annotator;

/**
 * Zaehlt die Elemente, die ueber eine bestimmte Referenz (Parameter 1) erreicht werden
 * und einen bestimmten Typ (Parameter 2) haben.
 * @author wenzel
 *
 */
public class CountReferencesByType extends Annotator {

	public CountReferencesByType(EPackage documentType, String annotationKey, String parameter,
			EClass acceptedType, Collection<String> requiredAnnotations) {
		
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		EClass cls =  (EClass) EMFMetaAccess.getMetaObjectByName(getDocumentType().getNsURI(), this.getParameter().split(",")[1]);
		if (cls==null)
			return 0f;
		
		return new Float(
				FilterUtil.filter(EMFModelAccess.getNodeNeighbors(object, EMFMetaAccess.getReferencesByNames(object.eClass(), this.getParameter().split(",")[0]).toArray(new EReference[] {})), true, EMFSelectors.byInstance(cls)).size());
	}
}