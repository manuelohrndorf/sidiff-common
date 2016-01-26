package org.sidiff.common.emf;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.eclipse.emf.common.util.*;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
import org.eclipse.emf.ecore.resource.*;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.*;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
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
			result = (EList) object.eGet(reference);
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
			result.addAll((List) object.eGet(reference));
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
		if (eObject.eIsSet(eAttribute)) {
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
	
	/**
	 * This method loads a resource which contains so called <b>cross document references</b>
	 * to other imported resources which are referenced by <b>platform:/resource/ </b>
	 * or <b>platform:/plugin</b> URIs.<br/>
	 * Usually, imported resources are referenced by relative URI paths
	 * (e.i, without leading plugin/platform/file schema prefix).
	 * However, in some cases one has platform:/resource instead which will result in
	 * unresolvable eProxyURIs when the main resource is loaded. In such cases this method can help.
	 * Just make sure you initialize your meta model package beforehand with
	 * <br/>MyMetaModelPackage.eINSTANCE.eClass();
	 * 
	 * <br/>Note: This method will take in <b>absolute file:/ paths to the referenced resources</b>.
	 * <br/>Note: This method will <b>assume that the imported resource files lie in the same folder</b>
	 * as the main resource. If you have differing folders then use a different method, see below
	 *  
	 * @param platformResourceOrPluginURI
	 * 				the platform:/resource or plattform:/plugin URI of the resource to load
	 * @param fileSchemaURI
	 * 				the absolute file:/ URI of the resource to load
	 * @return the resourceSet with the loaded main resource and it's referenced
	 * 		   resources
	 * @throws Exception 
	 * @see #loadResourceWithCrossDocumentReferences(ResourceSet, URI, URI, EPackage, Set)
	 */
	public static ResourceSet loadResourceWithCrossDocumentReferences(
			URI platformResourceOrPluginURI,
			URI fileSchemaURI) throws Exception {
				
		// Register arbitrary file extensions in the resource factory.
		// Extensions could also be defined separately (e.g.:
		// asymmetric, symmetric, ecore, uml) It is however necessary
		// for later resolution that one defines them all .
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("*", new XMIResourceFactoryImpl());

		// Obtain a new resource set, its URIConverter and URIMap
		ResourceSet resSet = new ResourceSetImpl();
		URIConverter uriConverter = resSet.getURIConverter();
		Map<URI,URI> uriMap = uriConverter.getURIMap();

		// Map platform resource/plugin URIs to absolute file:/-URIs
		// Firstly, the main resource:
		uriMap.put(platformResourceOrPluginURI, fileSchemaURI);
		
		// Create the resource (to find out all eProxyURIs)
		Resource resource = resSet.getResource(platformResourceOrPluginURI, true);
		
		// Find all eProxyURIS therein and create
		// URIMap entries to the respective absolute file:/ paths.
		// Here: all resources are assumed to lie in the same folder.
		String fileDirectoryPath = fileSchemaURI.trimSegments(1).toString() + System.getProperty("file.separator");
		boolean inDepthEProxySearchFinished = false;
		while(inDepthEProxySearchFinished==false) {

			for(URI proxyURI : findEProxyModelURIs(resource)) {
				boolean resolvable = false;
				if(proxyURI.isPlatformResource() || proxyURI.isPlatformPlugin()) {		
					String fileName = proxyURI.trimFragment().lastSegment();								
					String filePath = fileDirectoryPath + fileName;
					File f = new File(filePath.replace("file:/", ""));
					if(f.exists()) {
						uriMap.put(proxyURI,URI.createURI(filePath));
						resolvable = true;
					}
				}

				if(resolvable==false) {
					throw new Exception("Unresovable URI: " + proxyURI +
						  "\nOne cross referenced document platform:/ URI "
						+ "can't be mapped and resolved onto an absolute file:/ path."
						+ "Either add the document to the same directory as the main resource"
						+ "or activate the containing plugin during runtime.");
				}
				
			}

			// Previously recognized resources must be cleared in order to
			// load them again now with necessary URI mappings
			resSet.getResources().clear();
			resource = resSet.getResource(platformResourceOrPluginURI, true);

			// manually demand resolution of all proxyURIs
			EcoreUtil.resolveAll(resSet);

			if(findEProxyModelURIs(resource).isEmpty()) {
				inDepthEProxySearchFinished = true;
			}
			
		}
		
		return resSet;

	}
	
	
	/**
	 * This method loads a resource which contains so called <b>cross document references</b>
	 * to other imported resources which are referenced by <b>platform:/resource/ </b>
	 * or <b>platform:/plugin</b> URIs.<br/>
	 * Usually, imported resources are referenced by relative URI paths
	 * (e.i, without leading plugin/platform/file schema prefix).
	 * However, in some cases one has platform:/resource instead which will result in
	 * unresolvable eProxyURIs when the main resource is loaded. In such cases this method can help.
	 * Just make sure you initialize your meta model package beforehand with
	 * <br/>MyMetaModelPackage.eINSTANCE.eClass();
	 * 
	 * <br/>Note: This method will take in <b>absolute file:/ paths to the referenced resources</b>.
	 * If all cross referenced resources lie in the same folder as the main resource you can
	 * also use another method, see below.
	 *  
	 * @param platformOrPluginResourceURI
	 * 				the platform:/resource URI of the resource to load
	 * @param fileSchemaURI
	 * 				the absolute file:/ URI of the resource to load
	 * @param crossReferencedFileURIs
	 * 				the set of absolute file:/ URIs of the imported resources
	 * @return the resourceSet with the loaded main resource and it's referenced
	 * 		   resources
	 * @throws Exception 
	 * @see #loadResourceWithCrossDocumentReferences(URI, URI)
	 */
	public static ResourceSet loadResourceWithCrossDocumentReferences(
			URI platformResourceOrPluginURI,
			URI fileSchemaURI,
			Set<URI> crossReferencedFileURIs) throws Exception {

		// Register arbitrary file extensions in the resource factory.
		// Extensions could also be defined separately (e.g. here:
		// asymmetric, symmetric, ecore) It is however necessary
		// for later resolution that one defines them all .
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("*", new XMIResourceFactoryImpl());

		// Obtain a new resource set, its URIConverter and URIMap
		ResourceSet resSet = new ResourceSetImpl();
		URIConverter uriConverter = resSet.getURIConverter();
		Map<URI,URI> uriMap = uriConverter.getURIMap();

		// Map platform resource/plugin-URIs to absolute file:/-URIs
		// Firstly, the main resource:
		uriMap.put(platformResourceOrPluginURI,fileSchemaURI);

		// Create the resource (to find out all eProxyURIs)
		Resource resource = resSet.getResource(platformResourceOrPluginURI, true);		

		// find all platform resource/plugin URIs referenced inside the
		// resource and map them onto the respective file:/ URIs
		boolean inDepthEProxySearchFinished = false;
		while(inDepthEProxySearchFinished==false) {

			Set<URI> eProxieURIs = findEProxyModelURIs(resource);
			for(URI proxyURI: eProxieURIs) {
				boolean resolvable = false;
				String representativeSegmentsOfEProxyURI = null;

				// setup file-Scheme equivalent for platform:/resource URIs
				if(proxyURI.isPlatformResource()) {
					representativeSegmentsOfEProxyURI = proxyURI.toString().replaceFirst("platform:/resource","");
					representativeSegmentsOfEProxyURI = representativeSegmentsOfEProxyURI.replaceAll("\\|/", System.getProperty("file.separator"));
				}
				// setup file-Scheme equivalent for platform:/plugin URIs
				else if(proxyURI.isPlatformPlugin()) {
					representativeSegmentsOfEProxyURI = proxyURI.lastSegment();
				}
				
				// map
				for(URI crossReferencedFileURI: crossReferencedFileURIs) {
					if(crossReferencedFileURI.path().toString().contains(representativeSegmentsOfEProxyURI)) {
						uriMap.put(proxyURI,crossReferencedFileURI);
						resolvable = true;
						break;
					}
				}
				if(resolvable==false) {
					throw new Exception("Unresovable URI: " + proxyURI +
						  "\nThe list of input cross referenced document file URIs "
						+ "requires an existing absolute file:/ path onto which this proxy URI can be resolved.");
				}
			}
		
		// Previously recognized resources must be cleared in order to
		// load them again now with necessary URI mappings
		resSet.getResources().clear();
		resource = resSet.getResource(platformResourceOrPluginURI, true);

		// manually demand resolution of all proxyURIs mapped so far
		EcoreUtil.resolveAll(resSet);	
		
		// check if after the recent resolution there are still old or new eProxies left
		// (inside encapsulated resources of the main resource)
		if(findEProxyModelURIs(resource).isEmpty()) {
			inDepthEProxySearchFinished = true;			
		}		

	}
		
	return resSet;

	}

	/**
	 * This method finds EProxy URIs inside a resource and returns a list
	 * of unresolvable model URIs.
	 * 
	 * <br/><b>Note:</b> It is only possible to find EProxyURIs inside already resolved contents.
	 * In case of a deeper containment hierarchy of multiple encapsulated resources you need
	 * to make sure that the containing resources are resolved top down in order to find
	 * remaining eProxyURIs with this method.
	 * 
	 * @param resource
	 * @return set of eProxyURIs
	 */
	public static Set<URI> findEProxyModelURIs(Resource resource) {
		
		Set<URI> eProxies = new HashSet<URI>();		
		
		// iterate over all resources and encapsulated resources inside the ResourceSet
		// in order to find any proxy URIs
		TreeIterator<Object> oIt = EcoreUtil.getAllContents(resource.getResourceSet(), true);
		while(oIt.hasNext()) {
			
			// in case tree object is a resource
			Object object = oIt.next();
			if(object instanceof Resource) {
			
				Resource res = (Resource) object;				
				for(EObject eObject: getAllContentAsIterable(res)) {
					
					for(EObject crossReferencedEObject: eObject.eCrossReferences()) {
						if(crossReferencedEObject.eIsProxy()) {
							eProxies.add(((InternalEObject) crossReferencedEObject).eProxyURI().trimFragment());
						}
					}				
				}				
			}
			// in case tree object is an EObject
			else if(object instanceof EObject) {
				

				for(EObject crossReferencedEObject: ((EObject)object).eCrossReferences()) {
					if(crossReferencedEObject.eIsProxy()) {
						eProxies.add(((InternalEObject) crossReferencedEObject).eProxyURI().trimFragment());
					}
				}
			}
		
		}
					
		return eProxies;
		
	}

}
