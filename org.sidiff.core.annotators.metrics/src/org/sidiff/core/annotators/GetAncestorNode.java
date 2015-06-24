package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.*;
import org.sidiff.common.emf.EMFUtil;
import org.sidiff.core.annotation.Annotator;

/**
 * Dieser Annotator durchlaeuft die Container eines Elements solange bis ein Element eines bestimmten Typs 
 * (Parameter 1) gefunden wird und setzt dieses Element als Annotation.
 * @author wenzel
 *
 */
public class GetAncestorNode extends Annotator {

	public GetAncestorNode(EPackage documentType, String annotationKey, String parameter,
			EClass acceptedType, Collection<String> requiredAnnotations) {
		
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		while (object != null && !EMFUtil.getModelRelativeName(object.eClass()).equals(this.getParameter())) {
			object = object.eContainer();
		}
		if (object != null && EMFUtil.getModelRelativeName(object.eClass()).equals(this.getParameter())) {
			return object;
		}
		return null;
	}
}
