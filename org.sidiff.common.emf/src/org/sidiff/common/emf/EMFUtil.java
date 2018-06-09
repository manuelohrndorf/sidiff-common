package org.sidiff.common.emf;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.eclipse.emf.common.util.*;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.*;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.sidiff.common.emf.exceptions.EPackageNotFoundException;
import org.sidiff.common.emf.exceptions.UnknownAttributeException;
import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.io.ResourceUtil;
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

		ArrayList<EPackage> list = new ArrayList<EPackage>();

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
	 * Returns an Iterable that iterates over all elements contained in the given element (eAllContents()). It allows to iterate over the content in for-each loops.
	 * 
	 * @param element
	 * @return
	 */
	public static Iterable<EObject> getEAllContentAsIterable(final EObject element) {
		return new Iterable<EObject>() {
			@Override
			public Iterator<EObject> iterator() {
				return new Iterator<EObject>() {
					TreeIterator<EObject> iterator = element.eAllContents();

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

					@Override
					public EObject next() {
						return iterator.next();
					}

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}
				};
			}
		};
	}

	/**
	 * Returns an Iterable that iterates over all elements contained in the given resource (getAllContents()). It allows to iterate over the content in for-each loops.
	 * 
	 * @param element
	 * @return
	 */
	public static Iterable<EObject> getAllContentAsIterable(final Resource resource) {
		return new Iterable<EObject>() {
			@Override
			public Iterator<EObject> iterator() {
				return new Iterator<EObject>() {
					TreeIterator<EObject> iterator = resource.getAllContents();

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

					@Override
					public EObject next() {
						return iterator.next();
					}

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}
				};
			}
		};
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
		} else {
			return object.eClass().getEAnnotation(annotation) != null;
		}
	}

	/**
	 * Loads an Ecore model from a resource and registers the contained EPackage at the EPackage.Registry.INSTANCE.
	 * 
	 * @param ecoreModelName
	 *            Name of the Ecore-File to be loaded
	 */
	public static void loadEcoreModelFromResource(String ecoreModelName) {
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource ecoreResource = resourceSet.createResource(URI.createFileURI("./tmp"));
		try {
			ecoreResource.load(ResourceUtil.getInputStreamByResourceName(ecoreModelName), null);
		} catch (IOException e) {
			throw new SiDiffRuntimeException("Unable to load ecore model.", e);
		}
		EPackage ePackage = (EPackage) ecoreResource.getContents().get(0);
		registerEPackage(ePackage);
	}

	/**
	 * Registers a single EPackage at the EPackage registry of EMF.
	 * 
	 * @param ePackage
	 *            the Package to be registered
	 */
	private static void registerEPackage(EPackage ePackage) {
		EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
		for (EPackage subPackage : ePackage.getESubpackages()) {
			registerEPackage(subPackage);
		}
	}

	/**
	 * Returns a list of objects with is reachable from object with the given reference.
	 * 
	 * @param object
	 * @param reference
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<EObject> getObjectListFromReference(EObject object, EReference reference) {

		List<EObject> result = null;
		if (object.eGet(reference) == null) {
			return Collections.emptyList();
		} else if (reference.isMany()) {
			result = (EList<EObject>) object.eGet(reference);
		} else {
			result = Collections.singletonList((EObject) object.eGet(reference));
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Works similar to {@link getObjectListFromReference(EObject object, EReference reference)} but writes the result in the given list.
	 * 
	 * @param result
	 * @param object
	 * @param reference
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<EObject> fillObjectListFromReference(List<EObject> result, EObject object, EReference reference) {

		if (reference.isMany()) {
			result.addAll((List<EObject>) object.eGet(reference));
		} else {
			EObject ref = (EObject) object.eGet(reference);
			if (ref != null)
				result.add(ref);
		}
		return result;
	}

	/**
	 * Returns the complete contents of an EObject as list.
	 * 
	 * @param object
	 * @return
	 */
	public static List<EObject> createListFromEAllContents(EObject object) {
		List<EObject> result = new LinkedList<EObject>();
		TreeIterator<EObject> iterator = object.eAllContents();
		while (iterator.hasNext())
			result.add(iterator.next());
		return Collections.unmodifiableList(result);
	}

	/**
	 * Returns the size of the complete contents of an EObject.
	 * 
	 * @param object
	 * @return
	 */
	public static int computeEAllContentsSize(EObject object) {
		int result = 0;
		TreeIterator<EObject> iterator = object.eAllContents();
		while (iterator.hasNext()) {
			result++;
			iterator.next();
		}
		return result;
	}

	/**
	 * Returns the complete contents of a Resource as list.
	 * 
	 * @param resource
	 * @return
	 */
	public static List<EObject> createListFromEAllContents(Resource resource) {
		List<EObject> result = new LinkedList<EObject>();
		TreeIterator<EObject> iterator = resource.getAllContents();
		while (iterator.hasNext())
			result.add(iterator.next());
		return Collections.unmodifiableList(result);
	}

	/**
	 * Returns the size of the complete contents of a Resource.
	 * 
	 * @param resource
	 * @return
	 */
	public static int computeEAllContentsSize(Resource resource) {
		int result = 0;
		TreeIterator<EObject> iterator = resource.getAllContents();
		while (iterator.hasNext()) {
			result++;
			iterator.next();
		}
		return result;
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
		} else {
			if (eobj instanceof BasicEObjectImpl) {
				URI uri = ((BasicEObjectImpl) eobj).eProxyURI();
				if (uri != null)
					return "" + uri;
			}
			LogUtil.log(LogEvent.DEBUG, "Unable to resolve URI fragment for ", eobj.toString(), " Reason: " + eobj + " is not contained in a resource.");
			return "Anonymous_" + EMFUtil.getModelRelativeName(eobj.eClass());
		}
	}
	/**
	 * Get the value of the "name" feature iff present, otherwise null.
	 * @param eobj
	 * @return name of given object otherwise null is returned
	 */
	public static String getEObjectName(EObject eobj){
		EStructuralFeature nameFeature = eobj.eClass().getEStructuralFeature("name");
		
		if (nameFeature != null) {
			Object nameValue = eobj.eGet(nameFeature);
			
			if ((nameValue != null) && (nameValue instanceof String)) {
				return (String) nameValue;
			}
		}
		return null;
	}

	/**
	 * Get the URI of a specific instance of {@link EObject}.
	 * 
	 * @param eobj
	 *            The instance of {@link EObject} whose URI to retrieve.
	 * @return The object's URI.
	 */
	public static String getEObjectURI(EObject eobj) {
		try {
			return eobj.eResource().getURI() + "#" + eobj.eResource().getURIFragment(eobj);
		} catch (NullPointerException e) {
			if (eobj instanceof BasicEObjectImpl) {
				URI uri = ((BasicEObjectImpl) eobj).eProxyURI();
				if (uri != null)
					return "" + uri;
			}
			if (eobj instanceof EFactory) {
				EFactory ef = (EFactory) eobj;
				return ef.getEPackage().getNsURI();
			}
			assert (LogUtil.log(LogEvent.DEBUG, "Unable to resolve URI for ", eobj.toString(), " Reason: ", e));
		} catch (Exception e) {
			assert (LogUtil.log(LogEvent.DEBUG, "Unable to resolve URI for ", eobj.toString(), " Reason: ", e));
		}
		return getEObjectID(eobj);
	}

	/**
	 * Get the attribute value of a specific {@link EAttribute} of an {@link EObject}.
	 * 
	 * @param eobj
	 *            The instance of {@link EObject} whose attribute value to retrieve.
	 * @param attributeName
	 *            The name of the attribute to retrieve.
	 * @return The object's attribute value.
	 */
	public static Object getEObjectsAttribute(EObject eobj, String attributeName) {
		EStructuralFeature sf = eobj.eClass().getEStructuralFeature(attributeName);
		if (sf == null)
			throw new UnknownAttributeException("No such attribute '", attributeName, "' for " + eobj.eClass());
		if (!(sf instanceof EAttribute)) {
			throw new UnknownAttributeException("Feature is not an attribute: '", attributeName, "' for " + eobj.eClass());
		}
		return eobj.eGet(sf);
	}

	
	/**
	 * This method returns all EObjects in a Resource (optionally filtered by EClass type).
	 * Set type to null if no filtering is required.
	 * 
	 * @param 
	 * 		type (optional EClass Type)
	 * @param 
	 * 		resource
	 * @return
	 * 		list of EObjects
	 */
	public static List<EObject> getAllEObjectsByType(EClass type, Resource resource) {
		List<EObject> list = new ArrayList<EObject>();
		
		for(EObject eObject: getAllContentAsIterable(resource))  {		
			if(type==null || eObject.eClass().equals(type)) {
				list.add(eObject);
			}			
		}		
		return list;
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
		} else {
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
	}
	
	public static Map<EObject, EObject> copyAll(Collection<? extends EObject> eObjects){
		Copier copier = new Copier();
		copier.copyAll(eObjects);
		copier.copyReferences();
		
	
		return copier;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<EObject, EObject> copySubModel(Set<EObject> eObjects) {
		Map<EObject,EObject> copies = new HashMap<EObject, EObject>();
		Set<EObject> remaining = eObjects;
		while(!remaining.isEmpty()){
			Set<EObject> dependencies = new HashSet<EObject>();
			for (Iterator<EObject> iterator = remaining.iterator(); iterator.hasNext();) {
				EObject eObject = iterator.next();
				copies.put(eObject, copyWithoutReferences(eObject));
				for(EReference eReference : eObject.eClass().getEAllReferences()){
					if(!eReference.isDerived() && eReference.getLowerBound() > 0){
						dependencies.addAll(getObjectListFromReference(eObject, eReference));
					}
				}
				iterator.remove();
			}
			dependencies.removeAll(copies.keySet());
			remaining.addAll(dependencies);
		}

		for(EObject eObject : copies.keySet()){
			for(EReference eReference : eObject.eClass().getEAllReferences()){
				if(eObject.eGet(eReference) != null && !eReference.isDerived() && eReference.isChangeable()){
					if(eReference.isMany()){
						for(EObject tgt : (Collection<EObject>) eObject.eGet(eReference)){
							if(copies.containsKey(tgt)){
								((Collection<EObject>) copies.get(eObject).eGet(eReference)).add(copies.get(tgt));
							}
						}
					}else{
						EObject tgt = (EObject) eObject.eGet(eReference);
						if(copies.containsKey(tgt)){
							copies.get(eObject).eSet(eReference, copies.get(tgt));
						}
					}
				}
			}
		}
		Map<EObject,EObject> submodel = new HashMap<EObject,EObject>();
		for(EObject eObject : copies.keySet()){
			if(copies.get(eObject).eContainer() == null){
				submodel.put(eObject, copies.get(eObject));
			}
		}
		return submodel;
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
	 * Compares two EObjects
	 * 
	 * @param obj1
	 *            the first compare object
	 * @param obj2
	 *            the second compare object
	 * @return true if the objects are equal and false if they are not
	 */
	public static boolean equalsEObject(EObject obj1, EObject obj2) {
		if (obj1.eIsProxy() && obj2.eIsProxy()) {
			return EcoreUtil.getURI(obj1).equals(EcoreUtil.getURI(obj2));
		} else if (!obj1.eIsProxy() && !obj2.eIsProxy()) {
			return obj1.equals(obj2);
		} else {
			return false;
		}
	}

	/**
	 * Returns the xmi:id attribute value for the given eObject as a <tt>String</tt>. Returns <b>null</b> in case there's no containing resource or the eObject simply didn't have a xmi:id attribute.
	 */
	public static String getXmiId(EObject eObject) {
		String objectID = null;
		if (eObject != null && eObject.eResource() instanceof XMIResource) {
			objectID = ((XMIResource) eObject.eResource()).getID(eObject);
		}
		return objectID;
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
		if (eObject.eResource() instanceof XMIResource) {
			((XMIResource) eObject.eResource()).setID(eObject, id);
		}
	}
	
}
