package org.sidiff.common.emf.collections;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.emf.access.EMFMetaAccess;

/**
 * Different selectors/predicates to be used with {@link Stream#filter(Predicate)} / {@link CollectionUtil}.
 * @author wenzel
 *
 */
public class EMFSelectors {
	
	/**
	 * Filters EObjects by a given EClass.
	 * @param eClass
	 * @return
	 */
	public static final Predicate<EObject> byClass(final EClass eClass) {
		return item -> eClass.isSuperTypeOf(item.eClass());
	}
	
	/**
	 * Filters EObjects that are an instance of the given EClass or subclasses.
	 * @param eClass
	 * @return
	 */
	public static final Predicate<EObject> byInstance(final EClass eClass) {
		return item -> eClass.isInstance(item);
	}

	/**
	 * Filters elements by the given resource.
	 * @param resource
	 * @return
	 */
	public static final Predicate<EObject> byResource(final Resource resource) {
		return item -> Objects.equals(item.eResource(), resource);
	}
	
	/**
	 * Filters elements by the given metamodel.
	 * @param metamodelPackage
	 * @return
	 */
	public static final Predicate<EObject> byMetaModel(EPackage metamodelPackage) {
		final List<EClassifier> classes = EMFMetaAccess.getAllMetaClassesForPackage(metamodelPackage);
		return item -> classes.contains(item.eClass());
	}
}
