package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * This annotator computes the path of an object. 
 * The path is computed by concatenation of the types of the objects (from root) to the annotated object. 
 */
public class TypePathAnnotator extends AbstractPathAnnotator {

	public TypePathAnnotator(EPackage documentType, String annotationKey, String parameter, EClass acceptedType, Collection<String> requiredAnnotations) {
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations);
		if(getParameter()!=null) {
			throw new IllegalArgumentException("Not parameter expected! ("+getParameter()+")");
		}
	}

	@Override
	protected String getPathSegment(EObject object) {

		return object.eClass().getName();
	}

}
