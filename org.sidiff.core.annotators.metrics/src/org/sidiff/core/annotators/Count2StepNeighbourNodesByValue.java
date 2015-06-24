package org.sidiff.core.annotators;

import java.util.*;

import org.eclipse.emf.ecore.*;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.core.annotation.Annotator;

/**
 * Zaehlt Elemente, die ueber das aufeinanderfolgende Traversieren zweier Referenzen (Parameter 1+2) erreicht werden,
 * solange das gegebene Attribut (Parameter 3) der erreichten Elemente einen bestimmten Wert (Parameter 4) hat. 
 * @author wenzel
 *
 */
public class Count2StepNeighbourNodesByValue extends Annotator {

	private String firstReferenceString;
	private String secondReferenceString;
	private String attributeString;
	private String value = null;

	public Count2StepNeighbourNodesByValue(EPackage documentType, String annotationKey, String parameter,
			EClass acceptedType, Collection<String> requiredAnnotations) {
		
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);
		
		StringTokenizer st = new StringTokenizer(parameter, ",");
		firstReferenceString = st.nextToken();
		secondReferenceString = st.nextToken();
		attributeString = st.nextToken();
		value = st.nextToken();
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		float answer = 0f;
		EReference firstReference = (EReference) object.eClass().getEStructuralFeature(firstReferenceString);
		List<EObject> firstStep = EMFModelAccess.getNodeNeighbors(object,firstReference);
		for (EObject obj : firstStep) {
			EReference secondReference = (EReference) obj.eClass().getEStructuralFeature(secondReferenceString);
			List<EObject> secondStep = EMFModelAccess.getNodeNeighbors(obj,secondReference);
			for (EObject obj2 : secondStep) {
				EAttribute attribute = (EAttribute) obj2.eClass().getEStructuralFeature(attributeString);
				if (value.equals(obj2.eGet(attribute).toString()))
					answer++;
			}
		}
		return new Float(answer);
	}

}
