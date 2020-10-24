package org.sidiff.common.emf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.emf.exceptions.EPackageNotFoundException;
import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;

/**
 * Utility class which provides shortcut functions to several EMF related operations.
 *
 * @author wenzel, mrindt
 *
 */
public class EMFUtil {

	/**
	 * This method delivers all sub EPackages of an EPackage.
	 *
	 * @param ePackage
	 * @return
	 * @throws EPackageNotFoundException
	 */
	public static List<EPackage> getAllSubEPackages(EPackage ePackage) throws EPackageNotFoundException {
		if (ePackage == null) {
			throw new EPackageNotFoundException();
		}

		List<EPackage> list = new ArrayList<>();

		for (EPackage sub : ePackage.getESubpackages()) {
			if (!list.contains(sub)) {
				// add current sub package
				list.add(sub);
				// recursively add sub of sub packages...
				List<EPackage> subsOfSub = getAllSubEPackages(sub);
				subsOfSub.removeAll(list);
				list.addAll(subsOfSub);
			}
		}

		return list;
	}

	/**
	 * Checks whether the given EModelElement has the given marking annotation. In case of an EObject, its eClass is checked.
	 *
	 * @param object
	 * @param annotation
	 * @return
	 */
	public static boolean isAnnotatedWith(EObject object, String annotation) {
		if (object instanceof EModelElement) {
			return ((EModelElement) object).getEAnnotation(annotation) != null;
		}
		return object.eClass().getEAnnotation(annotation) != null;
	}

	/**
	 * Returns a list of objects that are reachable from the object with the given reference.
	 * @param object the object
	 * @param reference the reference
	 * @return list of all referenced objects, may be empty
	 */
	public static List<EObject> getReferenceTargets(EObject object, EReference reference) {
		return CollectionUtil.getValues(object.eGet(reference), EObject.class);
	}

	/**
	 * Returns a list of all values of the attribute of the given object.
	 * @param object the object
	 * @param attribute the attribute
	 * @return all values of the attribute of the object
	 */
	public static List<Object> getAttributeValues(EObject object, EAttribute attribute) {
		return CollectionUtil.getValues(object.eGet(attribute), Object.class);
	}

	/**
	 * Gets the qualified name of a classifier within its meta model.
	 */
	public static String getModelRelativeName(EClassifier type) {
		try {
			String name = type.getName();
			EPackage pkg = type.getEPackage();
			while (pkg != null && pkg.getESuperPackage() != null) {
				name = pkg.getName() + "." + name;
				pkg = pkg.getESuperPackage();
			}
			return name;
		} catch (Exception e) {
			throw new SiDiffRuntimeException("Unable to qualify name. ", e);
		}
	}

	/**
	 * Get the root package (i.e. the meta model) that contains the given classifier.
	 *
	 * @param type
	 * @return
	 */
	public static EPackage getRootPackage(EClassifier type) {
		EPackage pkg = type.getEPackage();
		while (pkg != null && pkg.getESuperPackage() != null) {
			pkg = pkg.getESuperPackage();
		}
		return pkg;
	}

	/**
	 * Get the ID (local URI fragment) of a specific instance of {@link EObject}.
	 *
	 * @param eobj
	 *            The instance of {@link EObject} whose ID to retrieve.
	 * @return The object's ID.
	 */
	public static String getEObjectID(EObject eobj) {
		if (eobj.eResource() != null) {
			return eobj.eResource().getURIFragment(eobj);
		}
		if (eobj instanceof BasicEObjectImpl) {
			URI uri = ((BasicEObjectImpl) eobj).eProxyURI();
			if (uri != null) {
				return uri.toString();
			}
		}
		LogUtil.log(LogEvent.DEBUG, "Unable to resolve URI fragment for ", eobj.toString(),
				" Reason: " + eobj + " is not contained in a resource.");
		return "Anonymous_" + EMFUtil.getModelRelativeName(eobj.eClass());
	}

	/**
	 * Get the value of the "name" feature iff present, otherwise <code>null</code>.
	 * @param eObject the object
	 * @return name of given object, otherwise <code>null</code> is returned
	 */
	public static String getEObjectName(EObject eObject) {
		if(eObject == null) {
			return null;
		}
		EStructuralFeature nameFeature = eObject.eClass().getEStructuralFeature("name");
		if (nameFeature instanceof EAttribute) {
			Object nameValue = eObject.eGet(nameFeature);
			if(nameValue instanceof String) {
				return (String)nameValue;
			}
		}
		return null;
	}

	/**
	 * Computes a hash value for the given resource.
	 *
	 * @param resource
	 * @return
	 */
	public static String computeHashForResource(Resource resource) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");

			TreeIterator<EObject> iterator = resource.getAllContents();
			while (iterator.hasNext()) {
				EObject obj = iterator.next();
				md.update(obj.eClass().getName().getBytes());
				md.update(EMFUtil.getEObjectID(obj).getBytes());
			}

			// get hash value
			StringBuffer buf = new StringBuffer();
			byte[] digest = md.digest();
			for (byte element : digest) {
				buf.append(Integer.toHexString(element & 0xff));
			}
			return buf.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new SiDiffRuntimeException(e);
		}

	}

	/**
	 * Returns a copy of the given eObject but not copies the EReferences.
	 *
	 * @param eObject
	 *            the object to copy.
	 * @return the copy.
	 */
	public static EObject copyWithoutReferences(EObject eObject) {
		if (eObject == null) {
			return null;
		}

		EObject copyEObject = EcoreUtil.create(eObject.eClass());
		EClass eClass = eObject.eClass();
		for (int i = 0, size = eClass.getFeatureCount(); i < size; ++i) {
			EStructuralFeature eStructuralFeature = eClass.getEStructuralFeature(i);
			if (eStructuralFeature.isChangeable() && !eStructuralFeature.isDerived()) {
				if (eStructuralFeature instanceof EAttribute) {
					copyAttribute((EAttribute) eStructuralFeature, eObject, copyEObject);
				}
			}
		}

		copyProxyURI(eObject, copyEObject);

		return copyEObject;
	}

	/**
	 * Called to handle the copying of an attribute; this adds a list of values or sets a single value as appropriate for the multiplicity.
	 *
	 * @param eAttribute
	 *            the attribute to copy.
	 * @param eObject
	 *            the object from which to copy.
	 * @param copyEObject
	 *            the object to copy to.
	 */
	public static void copyAttribute(EAttribute eAttribute, EObject eObject, EObject copyEObject) {
		// Standard value changed?
		if (eObject.eIsSet(eAttribute) && eAttribute.isChangeable() && !eAttribute.isDerived() && !eAttribute.isTransient()) {
			if (FeatureMapUtil.isFeatureMap(eAttribute)) {
				FeatureMap featureMap = (FeatureMap) eObject.eGet(eAttribute);
				for (int i = 0, size = featureMap.size(); i < size; ++i) {
					EStructuralFeature feature = featureMap.getEStructuralFeature(i);

					if (feature instanceof EReference && ((EReference) feature).isContainment()) {
						Object value = featureMap.getValue(i);

						if (value != null) {
							EcoreUtil.copy((EObject) value);
						}
					}
				}
			} else if (eAttribute.isMany()) {
				List<?> source = (List<?>) eObject.eGet(eAttribute);
				@SuppressWarnings("unchecked")
				List<Object> target = (List<Object>) copyEObject.eGet(eAttribute);
				if (source.isEmpty()) {
					target.clear();
				} else {
					target.addAll(source);
				}
			} else {
				copyEObject.eSet(eAttribute, eObject.eGet(eAttribute));
			}
		}
	}

	/**
	 * Copies the proxy URI from the original to the copy, if present.
	 *
	 * @param eObject
	 *            the object being copied.
	 * @param copyEObject
	 *            the copy being initialized.
	 */
	public static void copyProxyURI(EObject eObject, EObject copyEObject) {
		if (eObject.eIsProxy()) {
			((InternalEObject) copyEObject).eSetProxyURI(((InternalEObject) eObject).eProxyURI());
		}
	}

	/**
	 * Returns the xmi:id attribute value for the given eObject as a <tt>String</tt>. Returns <b>null</b> in case there's no containing resource or the eObject simply didn't have a xmi:id attribute.
	 */
	public static String getXmiId(EObject eObject) {
		if (eObject != null) {
			Resource resource = eObject.eResource();
			if (resource instanceof XMIResource) {
				return ((XMIResource)resource).getID(eObject);
			}
		}
		return null;
	}

	/**
	 * Sets the xmi:id attribute value for the given eObject
	 *
	 * @param eObject
	 * 				the object for that the id is set
	 * @param id
	 * 				the id that is set
	 */
	public static void setXmiId(EObject eObject, String id){
		if (eObject != null) {
			Resource resource = eObject.eResource();
			if (resource instanceof XMIResource) {
				((XMIResource)resource).setID(eObject, id);
			}
		}
	}

	/**
	 * Returns the signature name for the {@link EObject}.
	 * This is the object's name attribute, if present.
	 * For some other commonly used objects, a different signature is provided.
	 * As a last resort, the name of the object's class is returned.
	 * If the object is <code>null</code>, the string "null" is returned.
	 * @param eObject the object
	 * @return signature name
	 */
	public static String getEObjectSignatureName(EObject eObject) {
		if(eObject == null) {
			return "null";
		} else if(eObject instanceof EGenericType) {
			return getEGenericTypeSignature((EGenericType)eObject);
		} else if(eObject instanceof EAnnotation) {
			return getEAnnotationSignature((EAnnotation)eObject);
		} else if(eObject instanceof BasicEMap.Entry<?,?>) {
			return getEMapEntrySignature((BasicEMap.Entry<?,?>)eObject);
		}

		// try "signature" feature
		EStructuralFeature signatureFeature = eObject.eClass().getEStructuralFeature("signature");
		if(signatureFeature instanceof EAttribute
				&& !signatureFeature.isMany()
				&& signatureFeature.getEType() == EcorePackage.Literals.ESTRING) {
			String signature = (String)eObject.eGet(signatureFeature);
			if(signature != null && !signature.isEmpty()) {
				return signature;
			}
		}
		// try "name" feature
		String name = EMFUtil.getEObjectName(eObject);
		if(name != null && !name.isEmpty()) {
			return name;
		}
		return "[" + eObject.eClass().getName() + "]";
	}

	/**
	 * Returns the signature name for the {@link EGenericType}.
	 * This is analogous to the Java representation of generic types:
	 * <pre>T extends Foo&lt;? super S, U extends Bar&lt;T, ?>></pre>
	 * @param eGenericType the generic type, not <code>null</code>
	 * @return signature name
	 */
	public static String getEGenericTypeSignature(EGenericType eGenericType) {
		ETypeParameter eTypeParameter = eGenericType.getETypeParameter();
		if (eTypeParameter != null) {
			return getEObjectSignatureName(eTypeParameter);
		}

		EClassifier eClassifier = eGenericType.getEClassifier();
		if (eClassifier != null) {
			StringBuilder result = new StringBuilder();
			result.append(getEObjectSignatureName(eClassifier));
			List<EGenericType> eTypeArguments = eGenericType.getETypeArguments();
			if(!eTypeArguments.isEmpty()) {
				result.append(eTypeArguments.stream().map(EMFUtil::getEGenericTypeSignature).collect(Collectors.joining(", ", "<", ">")));
			}
			return result.toString();
		}

		EGenericType eUpperBound = eGenericType.getEUpperBound();
		if (eUpperBound != null) {
			return "? extends " + getEGenericTypeSignature(eUpperBound);
		}
		EGenericType eLowerBound = eGenericType.getELowerBound();
		if (eLowerBound != null) {
			return "? super " + getEGenericTypeSignature(eLowerBound);
		}
		return "?";
	}

	/**
	 * Returns the signature name for the {@link EAnnotation}
	 * using the annotation's source attribute.
	 * @param eAnnotation the annotation, not <code>null</code>
	 * @return signature name
	 */
	public static String getEAnnotationSignature(EAnnotation eAnnotation) {
		return "[" + eAnnotation.getSource() + "]";
	}

	/**
	 * Returns the signature name for the {@link BasicEMap.Entry}
	 * using the entry's key.
	 * @param mapEntry the map entry, not <code>null</code>
	 * @return signature name
	 */
	public static String getEMapEntrySignature(BasicEMap.Entry<?,?> mapEntry) {
		Object key = mapEntry.getKey();
		if(key == null) {
			return "[null]";
		} if(key instanceof String) {
			return (String)key;
		} else if(key instanceof EObject) {
			return getEObjectSignatureName((EObject)key);
		}
		return "[" + key.toString() + "]";
	}

	/**
	 * Check if the element is created dynamically, i.e. the containment
	 * reference is transient, derived or non-changeable, or whether is a synthetically
	 * inserted EGenericType.
	 * @param element The element to test.
	 * @return <code>true</code> if the element is created dynamically;
	 * <code>false</code> otherwise
	 */
	public static boolean isDynamic(EObject element) {
		if(element instanceof EGenericType) {
			return isDynamic((EGenericType)element);
		}
		EReference containment = element.eContainmentFeature();
		if (containment != null) {
			return isDynamic(element.eContainer(), containment);
		}
		return false;
	}

	/**
	 * TODO doc
	 * @param eReference
	 * @return
	 */
	public static boolean isDynamic(EReference eReference) {
		return eReference.isDerived() || eReference.isTransient() || !eReference.isChangeable();
	}

	/**
	 * TODO doc
	 * @param element
	 * @param eReference
	 * @return
	 */
	public static boolean isDynamic(EObject element, EReference eReference) {
		return isDynamic(eReference)
			 || eReference.getEType() == EcorePackage.eINSTANCE.getEGenericType() // special case for references referencing generic types
			 		&& eReference.getEContainingClass().isSuperTypeOf(element.eClass()) // prevent exceptions because of undefined features
			 		&& getReferenceTargets(element, eReference).stream().allMatch(EMFUtil::isDynamic); // dynamic if all targets are dynamic
	}

	/**
	 * TODO doc
	 * @param eAttribute
	 * @return
	 */
	public static boolean isDynamic(EAttribute eAttribute) {
		return eAttribute.isDerived() || eAttribute.isTransient() || !eAttribute.isChangeable();
	}

	/**
	 * TODO doc
	 * @param element
	 * @param eAttribute
	 * @return
	 */
	public static boolean isDynamic(EObject element, EAttribute eAttribute) {
		return isDynamic(eAttribute);
	}

	/**
	 * Returns whether the given {@link EGenericType} is dynamic, i.e. synthetically derived based on another feature,
	 * and therefore irrelevant for serialization.
	 * @param genericType the generic type
	 * @return <code>true</code> if dynamic, <code>false</code> otherwise
	 */
	public static boolean isDynamic(EGenericType genericType) {
		if(genericType.getETypeParameter() != null) {
			// Generic types with type parameter are not dynamic,
			// they might occur as generic return / exception types
			// for operations.
			return false;
		}
		if(!genericType.getETypeArguments().isEmpty()) {
			// Generic types with type arguments are not dynamic,
			// because they denote complex types.
			return false;
		}

		if(genericType.eContainer() instanceof EGenericType) {
			// This generic type is contained in another one
			// and not dynamic, unless the parent does not use this one.
			EGenericType container = (EGenericType)genericType.eContainer();
			return !(container.getETypeArguments().contains(genericType)
					|| genericType.equals(container.getELowerBound())
					|| genericType.equals(container.getEUpperBound()));
		} else if(genericType.eContainer() instanceof ETypeParameter) {
			// This generic type is contained in a type parameter
			// and not dynamic, unless the type parameter does not use this one.
			return !((ETypeParameter)genericType.eContainer()).getEBounds().contains(genericType);
		}

		// The generic type is dynamic, as it only specifies a classifier,
		// but no type arguments or parameters, and it is not part
		// of another type that needs it.
		return true;
	}
}
