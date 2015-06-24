package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.*;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.core.annotation.Annotator;

/**
 * Zeahlt die Elemente, die ueber einen gegebenen Pfad (Parameter 1) erreicht werden. 
 * @author wenzel
 *
 */
public class CountTargets extends Annotator {

	public CountTargets(EPackage documentType, String annotationKey, String parameter,
			EClass acceptedType, Collection<String> requiredAnnotations) {
		
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		return new Float(EMFModelAccess.evaluatePath(object, EMFMetaAccess.translatePath(object.eClass(), this.getParameter())).size());
	}
}