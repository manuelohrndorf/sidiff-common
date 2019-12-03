package org.sidiff.common.emf.collections;

import java.util.Comparator;

import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.sidiff.common.emf.EMFAdapter;
import org.sidiff.common.emf.annotation.AnnotateableElement;

/**
 * Different comparators to be used for EMF objects.
 * @author wenzel / reuling
 *
 */
public class EMFComparators {

	/**
	 * Compares {@link ENamedElement}s by their names. 
	 */
	public static final Comparator<ENamedElement> NAMED_ELEMENT_BY_NAME = Comparator.comparing(ENamedElement::getName);
	
	/**
	 * Compares EObjects by their annotation value stored under the given key.
	 * @param annotationKey
	 * @return
	 */
	public static final Comparator<EObject> createObjectByAnnotationComparator(final String annotationKey) {
		return Comparator.comparing(
				obj -> EMFAdapter.INSTANCE.adapt(obj, AnnotateableElement.class).getAnnotation(annotationKey, String.class),
				Comparator.nullsLast(String::compareTo));
	}
	
	/**
	 * Compares EObjects by their types. 
	 */
	public static final Comparator<EObject> EOBJECT_BY_TYPE = Comparator.comparing(obj -> obj.eClass().getName());
}
