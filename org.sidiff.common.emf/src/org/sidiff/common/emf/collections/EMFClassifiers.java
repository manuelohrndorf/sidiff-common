package org.sidiff.common.emf.collections;

import java.util.function.Function;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.sidiff.common.collections.CollectionUtil;

/**
 * Classifiers to be used with {@link CollectionUtil}.
 */
public class EMFClassifiers {

	/**
	 * Classifies EObjects by their EClass
	 */
	public final static Function<EObject, EClass> ELEMENT_BY_CLASS = item -> item.eClass();
}
