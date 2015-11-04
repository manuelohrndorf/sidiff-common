package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.*;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;
import org.sidiff.core.annotation.Annotator;

/**
 * Zaehlt die Anzahl Zeichen des Werts eines gegebenen Attributs.
 * @author wenzel
 *
 */
public class CountAttributeValueTextLength extends Annotator {

	public CountAttributeValueTextLength(EPackage documentType, String annotationKey, String parameter,
			EClass acceptedType, Collection<String> requiredAnnotations) {
		
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		EAttribute attribute = (EAttribute) object.eClass().getEStructuralFeature(this.getParameter());
		if (attribute == null) {
			assert(LogUtil.log(LogEvent.DEBUG, "No such attribute '",getParameter(),"' on type '",object.eClass().getName(),"'.",object));
			return new Float(0);
		}
		return new Float(String.valueOf(object.eGet(attribute)).length());
	}

}
