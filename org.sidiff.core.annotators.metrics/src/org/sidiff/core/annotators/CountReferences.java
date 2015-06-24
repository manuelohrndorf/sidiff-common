package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.*;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.core.annotation.Annotator;

/**
 * Zaehlt die Elemente, die ueber eine bestimmte Referenz (Parameter 1) erreicht werden.
 * @author wenzel
 *
 */
public class CountReferences extends Annotator {

	public CountReferences(EPackage documentType, String annotationKey, String parameter,
			EClass acceptedType, Collection<String> requiredAnnotations) {
		
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		return new Float(EMFModelAccess.getNodeNeighbors(object, EMFMetaAccess.getReferencesByNames(object.eClass(), this.getParameter()).toArray(new EReference[] {})).size());
	}
}