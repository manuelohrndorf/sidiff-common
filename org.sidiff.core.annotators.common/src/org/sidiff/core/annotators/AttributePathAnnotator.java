package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;

/**
 * A specific path annotator that returns the value of the specified EAttribute
 * (given as parameter) for the actual EObject. If the attribute value is null,
 * to situations are distinguished: If the EAttribute is required, an assertion
 * error should be thrown. If the attribute is not required, "<null>" is
 * returned as path segment.
 * 
 * @author kehrer
 */
public class AttributePathAnnotator extends AbstractPathAnnotator {

	private EAttribute eAttribute = null;

	public AttributePathAnnotator(EPackage documentType, String annotationKey, String parameter, EClass acceptedType,
			Collection<String> requiredAnnotations) {
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations);

		EStructuralFeature feature = acceptedType.getEStructuralFeature(getParameter());
		if (feature != null && feature instanceof EAttribute) {
			this.eAttribute = (EAttribute) feature;
		} else {
			throw new IllegalAccessError(acceptedType.getName() + " does not contain a Attribute named '" + getParameter() + "'");
		}
	}

	@Override
	protected String getPathSegment(EObject object) {
		Object attrValue = object.eGet(eAttribute);

		if (attrValue != null) {
			return attrValue.toString();
		} else {
			if (eAttribute.isRequired()) {
				throw new IllegalAccessError("Value of required attribute " + eAttribute.getName() + " is null for object " + object);
			} else {
				LogUtil.log(LogEvent.WARNING, "Value of attribute " + eAttribute.getName() + " is null for object " + object
						+ ". Returning <null> for path segment");

				return "<null>";
			}
		}
	}

}
