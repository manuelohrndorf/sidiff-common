package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.sidiff.common.emf.EMFUtil;
import org.sidiff.core.annotation.Annotator;

/**
 * Dieser Annotator setzt die persistente XMI ID als derived ID. Key ist also
 * normalerweise "DERIVED_ID" (damit der ID-basierte Matcher damit arbeiten
 * kann); value die persistente ID.
 * 
 * Die Existenz einer persistenten ID wird per Assertion überprüft.<br>
 * Dieser Annotator hat keine Parameter und erfordert keine anderen Annotations.
 * 
 * @author kehrer
 * 
 */
public class PersistentIDAnnotator extends Annotator {

	public PersistentIDAnnotator(EPackage documentType, String annotationKey,
			String parameter, EClass acceptedType,
			Collection<String> requiredAnnotations) {

		super(documentType, annotationKey, null, acceptedType,
				requiredAnnotations, ExecutionOrder.PRE);
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		String id = EMFUtil.getXmiId(object);
		assert (id != null) : "Could not find persistent identifier (XMI ID) for object "
				+ object;

		return id;
	}

}
