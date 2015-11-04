package org.sidiff.core.annotators;

import java.util.Collection;
import java.util.StringTokenizer;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.*;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.core.annotation.Annotator;

/**
 * Zaehlt Kind-Elemente und Kindes-Kind-Elemente (im gesamten Teilbaum) eines bestimmten Typs (Parameter 1), 
 * deren Attribut (Parameter 2) einen bestimmten Wert (Parameter 3) hat.
 * @author wenzel
 *
 */
public class CountSubtreeNodesByValue extends Annotator {

	private String classString = null;
	private String attributeString = null;
	private String value = null;

	public CountSubtreeNodesByValue(EPackage documentType, String annotationKey, String parameter,
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
		
		EAttribute attribute = (EAttribute) cls.getEStructuralFeature(attributeString);
		if (attribute == null)
			return 0f;
		
		TreeIterator<EObject> iterator = object.eAllContents();
		while (iterator.hasNext()) {
			EObject obj = iterator.next();
			if (cls.isSuperTypeOf(obj.eClass())){
				Object ref_value = obj.eGet(attribute);	
				if (ref_value != null && value.equals(ref_value.toString()))
					answer++;
			}	
		}
		
		return new Float(answer);
	}

}
