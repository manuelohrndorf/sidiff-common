package org.sidiff.core.annotators;

import java.util.Collection;
import java.util.StringTokenizer;

import org.eclipse.emf.ecore.*;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.core.annotation.Annotator;

/**
 * Zaehlt Kind-Elemente eines bestimmten Typs (Parameter 1), 
 * deren Attribut (Parameter 2) einen bestimmten Wert (Parameter 3) hat.
 * @author wenzel
 *
 */
public class CountChildNodesByValue extends Annotator {

	private String classString = null;
	private String attributeString = null;
	private String value = null;

	public CountChildNodesByValue(EPackage documentType, String annotationKey, String parameter,
			EClass acceptedType, Collection<String> requiredAnnotations) {
		
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);
		
		StringTokenizer st = new StringTokenizer(parameter, ",");
		classString = st.nextToken();
		attributeString = st.nextToken();
		value = st.nextToken();
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		float answer = 0f;
		EClass cls = (EClass) EMFMetaAccess.getMetaObjectByName(getDocumentType().getNsURI(), classString);
		if (cls==null)
			return 0f;
		for (EObject child : EMFModelAccess.getChildren(object, cls)) {
			EAttribute attribute = (EAttribute) child.eClass().getEStructuralFeature(attributeString);
			if (attribute != null && value.equals(child.eGet(attribute).toString()))
				answer++;
		}
		return new Float(answer);
	}

}
