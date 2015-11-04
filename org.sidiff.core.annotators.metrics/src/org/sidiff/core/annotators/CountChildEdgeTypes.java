package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.*;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.core.annotation.Annotator;

/**
 * Zaehlt die Kind-Elemente, die ueber eine bestimmte Referenz erreicht werden. 
 * @author wenzel
 *
 */
public class CountChildEdgeTypes extends Annotator {

	public CountChildEdgeTypes(EPackage documentType, String annotationKey, String parameter,
			EClass acceptedType, Collection<String> requiredAnnotations) {
		
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		EReference ref = (EReference) object.eClass().getEStructuralFeature(this.getParameter());
		return new Float(EMFModelAccess.getChildren(object, ref).size());
	}

}
