package org.sidiff.common.emf;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.sidiff.common.emf.access.EMFMetaAccess;

public class SubTypeIndex {

	/**
	 * Type index form each EClass to its sub-types.
	 */
	private Map<EClass, Set<EClass>> subTypes;

	public SubTypeIndex(Set<EPackage> ePackages) {
		this.subTypes = getSubtypeIndex(ePackages);
	}

	/**
	 * Returns all sub-types of the given EClass.
	 * 
	 * @param referenceType
	 *            The super-type.
	 * @return All sub-types of the given EClass.
	 */
	public Set<EClass> getSubTypes(EClass referenceType) {
		return subTypes.getOrDefault(referenceType, Collections.emptySet());
	}

	/**
	 * Creates a map form each class in the package to its corresponding sub-types (in the package).
	 * 
	 * @param ePackage
	 *            The package containing the sub- and super-classes.
	 * @return A map EClass -> Set of EClass sup-types.
	 */
	private static Map<EClass, Set<EClass>> getSubtypeIndex(Set<EPackage> ePackages) {
		// Class (A) -> [Sub classes (X, Y, Z)]
		Map<EClass, Set<EClass>> subTypes = new HashMap<>();
		for (EClass eSubClass : EMFMetaAccess.getAllEClasses(ePackages)) {
			subTypes.putIfAbsent(eSubClass, new HashSet<>());

			// Lookup the super types (X,Y,Z) of class (A) and add
			// class (A) as sub type to the classes (X, Y, Z)
			for (EClass eSuperClass : eSubClass.getEAllSuperTypes()) {
				subTypes.computeIfAbsent(eSuperClass, unused -> new HashSet<>()).add(eSubClass);
			}
		}
		return subTypes;
	}
}
