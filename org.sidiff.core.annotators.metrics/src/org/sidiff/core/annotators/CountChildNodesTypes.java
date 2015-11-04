package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.*;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.core.annotation.Annotator;

/**
 * Zaehlt Kind-Elemente eines bestimmten Typs (Parameter 1).
 * @author wenzel
 *
 */
public class CountChildNodesTypes extends Annotator {

	public CountChildNodesTypes(EPackage documentType, String annotationKey, String parameter,
			EClass acceptedType, Collection<String> requiredAnnotations) {
		
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		EClass cls =  (EClass) EMFMetaAccess.getMetaObjectByName(getDocumentType().getNsURI(), this.getParameter());
		if (cls==null)
			return 0f;
		return new Float(EMFModelAccess.getChildren(object, cls).size());
	}

}
