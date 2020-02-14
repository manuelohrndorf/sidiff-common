package org.sidiff.common.emf.access;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.sidiff.common.emf.access.impl.EMFIndexedAccessorImpl;
import org.sidiff.common.emf.access.impl.EMFModelAccessor;
import org.sidiff.common.emf.access.impl.EMFModelAccessorImpl;
import org.sidiff.common.emf.access.path.EMFPath;
import org.sidiff.common.emf.access.path.EMFPathAccessor;
import org.sidiff.common.emf.access.path.PathEvaluationStrategy;
import org.sidiff.common.emf.access.path.TargetEvaluationStrategy;
import org.sidiff.common.emf.access.path.impl.EMFPathAccessorImpl;
import org.sidiff.common.emf.access.tree.TreeVisitor;
import org.sidiff.common.emf.access.value.RemoteAttribute;
import org.sidiff.common.emf.access.value.RemoteAttributeAccessor;
import org.sidiff.common.emf.access.value.impl.RemoteAttributeAccessorImpl;

/**
 * This class supports easy access to model instances.
 * 
 * @author wenzel
 * 
 */
public class EMFModelAccess {

	static EMFModelAccessor modelAccessor = new EMFModelAccessorImpl();
	static EMFPathAccessor pathAccessor = new EMFPathAccessorImpl();
	static RemoteAttributeAccessor remoteValueAccessor = new RemoteAttributeAccessorImpl();
	static EMFReverseAccessor reverseAccessor = new EMFIndexedAccessorImpl();
	
	/**
	 * Returns the index position of the given object within its container.
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static int getIndexOf(EObject object) {
		try {
			if (object.eContainmentFeature() == null || !object.eContainmentFeature().isMany())
				return 0;
			return ((EList<EObject>) object.eContainer().eGet(object.eContainmentFeature())).indexOf(object);
		} catch (ClassCastException e) {
			return -1;
		}
	}

	/**
	 * Computes the number of siblings of an element.
	 * 
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static int getNumberOfSiblings(EObject object) {
		try {
			if (!object.eContainmentFeature().isMany())
				return 0;
			return ((EList<EObject>) object.eContainer().eGet(object.eContainmentFeature())).size() - 1;
		} catch (ClassCastException e) {
			return 0;
		}
	}

	/**
	 * Selects the most proper document type out of a set of document types.
	 * 
	 * @param documentTypes
	 * @return
	 */
	public static String selectDocumentType(Set<String> documentTypes){
		return modelAccessor.getDocumentType(documentTypes);
	}
	
	/**
	 * Returns the document type (i.e. the package namespace URI) of the model of the given element.
	 * 
	 * @param eObject
	 * @return
	 */
	public static String getDocumentType(EObject eObject) {
		return modelAccessor.getDocumentType(eObject);
	}

	/**
	 * Returns the document type (i.e. the package namespace URI) of the given model. I.e. the URI of the root package of the meta model of the given object. The method navigates to the meta class of the given object, from there it navigates upwards
	 * through the package hierarchy until the root package is reached. The URI of this package is seen as the document type, and returned.
	 * 
	 * @param model
	 * @return
	 */
	public static String getDocumentType(Resource model) {
		return modelAccessor.getDocumentType(model);
	}

	/**
	 * Returns all document types between notifiers. This is
	 * the union of all resource/resource set/EObject document types.
	 */
	public static Set<String> getDocumentTypes(Collection<? extends Notifier> notifiers) {
		final Set<String> documentTypes = new HashSet<>();
		TreeVisitor visitor = new TreeVisitor() {
			@Override
			public boolean preExecute(EObject object) {
				documentTypes.add(getDocumentType(object));
				//Visit all Objects
				return true;
			}
			@Override
			public void postExecute(EObject object) {
				//Nothing to do
			}
		};
		for (Notifier notifier : notifiers) {
			if(notifier instanceof ResourceSet) {
				documentTypes.addAll(getDocumentTypes(notifier));
			} else if(notifier instanceof Resource) {
				EMFModelAccess.traverse((Resource)notifier, visitor);
			} else if(notifier instanceof EObject) {
				EMFModelAccess.traverse((EObject)notifier, visitor);
			}
		}
		return documentTypes;
	}

	/**
	 * Method returns a list of all children connected by a specific {@link EReference} type.
	 * 
	 * @param type
	 *            {@link EReference}
	 * @return returns a list of all children, connected by a specific {@link EReference} type.
	 */
	public static List<EObject> getChildren(EObject eObject, EReference type) {
		return modelAccessor.getChildren(eObject, type);
	}

	/**
	 * Method returns a list of all children with a specific {@link EClass} type.
	 * 
	 * @param type
	 *            {@link EClass}
	 * @return returns a list of all children with a specific {@link EClass} type.
	 */
	public static List<EObject> getChildren(EObject eObject, EClass type) {
		return modelAccessor.getChildren(eObject, type);
	}

	/**
	 * Method returns a list of all types of children
	 * 
	 * @return returns a list of all {@link EClass} types of children
	 */
	public static List<EClass> getChildrenTypes(EObject eObject) {
		return modelAccessor.getChildrenTypes(eObject);
	}

	/**
	 * Traverses over the model using a TreeVisitor.
	 * 
	 * @param visitor
	 *            The TreeVisitor to be used for traversal.
	 */
	public static void traverse(Resource model, TreeVisitor visitor){
		modelAccessor.traverse(model, visitor);
	}

	/**
	 * Traverses over the tree below the object using a TreeVisitor.
	 * 
	 * @param visitor
	 *            The TreeVisitor to be used for traversal.
	 */
	public static void traverse(EObject root, TreeVisitor visitor) {
		modelAccessor.traverse(root, visitor);
	}

	/**
	 * Returns all referenced objects which are neither children nor parent.
	 * 
	 * @return A list of referenced objects which are neither children nor parent.
	 */
	public static List<EObject> getReferencedObjects(EObject eObject) {
		return modelAccessor.getReferencedObjects(eObject);
	}

	/**
	 * Returns all referenced objects of the given type which are neither children nor parent.
	 * 
	 * @param type
	 *            {@link EClass}
	 * @return A list of referenced objects of the given type which are neither children nor parent.
	 */
	public static List<EObject> getReferencedObjects(EObject eObject, EClass type) {
		return modelAccessor.getReferencedObjects(eObject, type);
	}

	/**
	 * Returns all stereotypes of an object according to UML Profiles. The result is 
	 * based on the naming convention of the reference to the object (= eObject parameter)
	 * starting with "base_*". Additionally only stereotypes are returned, which
	 * are required by the base class defined via the lower bound of such reference. 
	 * @param eObject 
	 * @return A list of all required stereotypes
	 */
	public static List<EObject> getRequiredStereoTypes(EObject eObject){
		return modelAccessor.getRequiredStereoTypes(eObject);
	}
	
	/**
	 * Returns all stereotypes of an object according to UML Profiles. The result is 
	 * based on the naming convention of the reference to the object (= eObject parameter)
	 * starting with "base_*".	 * 
	 * @param eObject 
	 * @return A list of all stereotypes
	 */
	public static List<EObject> getStereoTypes(EObject eObject){
		return modelAccessor.getStereoTypes(eObject);
	}
	
	/**
	 * Returns all siblings of an object. I.e. all other objects that are stored in the same containment.
	 * 
	 * @return A list of all siblings.
	 */
	public static List<EObject> getSiblings(EObject eObject) {
		return modelAccessor.getSiblings(eObject);
	}

	/**
	 * Returns the left sibling of an object. I.e. the object which is stored in the same containment having an index n-1 with n being the index of the adapted object. If the adapted object is the first element in the containment list, null is returned.
	 * 
	 * @return The left sibling, or null, if no such exists.
	 */
	public static EObject getLeftSibling(EObject eObject) {
		return modelAccessor.getLeftSibling(eObject);
	}

	/**
	 * Returns the right sibling of an object. I.e. the object which is stored in the same containment having an index n+1 with n being the index of the adapted object. If the adapted object is the last element in the containment list, null is returned.
	 * 
	 * @return The right sibling, or null, if no such exists.
	 */
	public static EObject getRightSibling(EObject eObject) {
		return modelAccessor.getRightSibling(eObject);
	}

	/**
	 * Returns all neighbored nodes, i.e. they are connected by any reference (including container/containment-references)
	 * 
	 * @param object
	 * @return A list of neighbored nodes
	 */
	public static List<EObject> getNodeNeighbors(EObject object) {
		return modelAccessor.getNodeNeighbors(object);
	}

	/**
	 * Returns all neighbored nodes, which are connected by a reference (including container/containment-references) of the given type.
	 * 
	 * @param object
	 * @param types
	 * @return A list of neighbored nodes, which are connected by a reference (including container/containment-references) of the given type.
	 */
	public static List<EObject> getNodeNeighbors(EObject object, EReference... types) {
		return modelAccessor.getNodeNeighbors(object, types);
	}

	/**
	 * Returns all neighbored nodes, i.e. they are connected by any reference (including container/containment-references), which have the given type.
	 * 
	 * @param object
	 * @param types
	 * @return A list of neighbored nodes, i.e. they are connected by any reference (including container/containment-references), which have the given type.
	 */
	public static List<EObject> getNodeNeighbors(EObject object, EClass... types) {
		return modelAccessor.getNodeNeighbors(object, types);
	}

	/**
	 * 
	 * @param object
	 * @return
	 */
	public static List<EObject> getMandatoryNodeNeighbors(EObject object) {
		return modelAccessor.getMandatoryNodeNeighbors(object);
	}

	/**
	 * Returns those elements which are addresses by the given path and the default strategy.
	 * 
	 * @param start The EMF start node/object
	 * @param path The EMF-Path to be evaluated.
	 * 
	 * @return The EMF-Objects adressed by the given Path and the default strategy. 
	 */
	public static Collection<EObject> evaluatePath(EObject start, EMFPath path){
		return pathAccessor.evaluatePath(start, path, TargetEvaluationStrategy.class);
	}
	
	/**
	 * Returns a !element! which are addressed by the given path.
	 * 
	 * @param start The EMF start node/object
	 * @param path The EMF-Path to be evaluated.
	 * @param existence whether or not a object has to be addressed.
	 * 
	 * @return The EMF-Objects addressed by the given Path (target)
	 * @throws IllegalArgumentException if more then one Object is addressed!
	 */
	public static EObject evaluatePath(EObject start, EMFPath path,boolean existence){
		
		EObject result = null;
		Collection<EObject> resultSet = pathAccessor.evaluatePath(start, path, TargetEvaluationStrategy.class);
		Iterator<EObject> resultSetIterator = resultSet.iterator();
		
		if(resultSetIterator.hasNext()){
			result = resultSetIterator.next();
			if(resultSetIterator.hasNext()){
				throw new IllegalArgumentException("More the one target adressed by Object/Path");
			} 
		} else if(existence){
			throw new IllegalArgumentException("Missing adressed Object by given Object/Path");
		}
			
		return result;
	}
	
	/**
	 * Returns those elements which are addresses by the given path and the default strategy.
	 * 
	 * @param start The EMF start node/object
	 * @param path The EMF-Path to be evaluated.
	 * @param strategy The strategy to be used for evaluation. 
	 * 
	 * @return The EMF-Objects adressed by the given Path and the given strategy.  
	 */
	public static <T> T evaluatePath(EObject start, EMFPath path, Class<? extends PathEvaluationStrategy<T>> strategy){
		return pathAccessor.evaluatePath(start, path, strategy);
	}

	/**
	 * Evaluates the RemoteAttribute on a given Node/Object.
	 * 
	 * @param context The context node the evaluation starts.
	 * @param remoteAttribute Handle to a translated expression.
	 * 
	 * @return A result, regarding the EAttribute-Type (if a EAttribute was adressed)
	 */
	public static <T> T computeRemoteAttributeValue(EObject context,RemoteAttribute remoteAttribute){
		return remoteValueAccessor.computeRemoteAttributeValue(context, remoteAttribute);
	}
	
	/**
	 * Returns all EObjects that refer to the given target with a reference of the given type. 
	 * @param target
	 * @param reference
	 * @return
	 */
	public static Collection<EObject> getRefers(EObject target,EReference reference){
		return reverseAccessor.getRefers(target, reference);
	}
	
	/**
	 * Returns all EObjects that refer to the given target with a reference of the given type. 
	 * @param target
	 * @param reference
	 * @return
	 */
	public static EObject getRefer(EObject target,EReference reference){
		
		Iterator<EObject> refers = reverseAccessor.getRefers(target, reference).iterator();
		
		EObject result = null;
		if(refers.hasNext()){
			result = refers.next();
			if(refers.hasNext()) {
				throw new IllegalStateException("Target has more then one refer");
			}
		}
		return result;	
	}

	/**
	 * Constant that represents a "generic document type". If functions that are
	 * usually designed for a specific document type (e.g., matchers or
	 * technical difference builders) are generic in the sense that they can
	 * handle any document type, the y can indicate this genericity by using
	 * this constant as supported document type.
	 */
	public static final String GENERIC_DOCUMENT_TYPE = "generic";

	/**
	 * Selects, given a set of document types, the most characteristic document
	 * type. Most often, docTypes will contain only one element. However, there
	 * may be cases with multiple document types. In case of UML profiled
	 * models, for example, there are two document types; representing the
	 * reference metamodel and the profile definition. In such a case, the
	 * document type of the profile definition is to be considered
	 * characteristic. If there are even more than two document types, the first
	 * is returned.
	 * 
	 * @param docTypes
	 * @return The characteristic document type. Note: If docTypes is empty,
	 *         <code>null</code> will be returned.
	 */
	public static String getCharacteristicDocumentType(Set<String> docTypes) {
		if (docTypes.isEmpty()) {
			return null;
		}

		List<String> documentTypes = new LinkedList<String>();
		documentTypes.addAll(docTypes);

		if (documentTypes.size() == 1) {
			// documentType is nonambiguous
			return documentTypes.get(0);
		}

		// Remove irrelevant docTypes
		for (Iterator<String> iterator = documentTypes.iterator(); iterator.hasNext();) {
			String docType = iterator.next();
			if (docType.contains("UML") || docType.contains("Henshin/Trace")) {
				iterator.remove();
			}
		}

		if (documentTypes.size() == 1) {
			return documentTypes.get(0);
		} else {
			return docTypes.iterator().next();
		}
	}

	/**
	 * First, all document types are extracted from the given model resource.
	 * Afterwards, the most characteristic one is selected and returned.
	 * 
	 * In other words: Given a set of document types, we try to select the most
	 * characteristic document type. Most often, docTypes will contain only one
	 * element. However, there may be cases with multiple document types. Two
	 * examples are currently supported, feel free to add (and implement) more
	 * of them:
	 * 
	 * (a) In case of UML profiled models, there are two document types; one
	 * representing the reference meta-model and the other one representing the
	 * profile definition. In such a case, the document type of the profile
	 * definition is to be considered characteristic. If there are even more
	 * than two document types, the first one is returned.
	 * 
	 * (b) In case of multi-view models that are connected via Henshin trace
	 * links, we have at least two documents types; the Henshin/Trace document
	 * type and the docType of the model that holds the trace links.
	 * 
	 * @param model
	 * @return The characteristic document type. Note: If model is empty,
	 *         <code>null</code> will be returned.
	 * 
	 * @param model
	 * @return
	 */
	public static String getCharacteristicDocumentType(Resource model) {
		// empty model -> return null
		if ((model == null) || model.getContents().isEmpty()) {
			return null;
		}

		// no docTypes -> return null
		Set<String> docTypes = getDocumentTypes(model, Scope.RESOURCE);
		if (docTypes.isEmpty()) {
			return null;
		}

		List<String> documentTypes = new LinkedList<String>();
		documentTypes.addAll(docTypes);

		if (documentTypes.size() == 1) {
			// documentType is non-ambiguous
			return documentTypes.get(0);
		}

		boolean containsUML = containsDocType(documentTypes, "UML");
		boolean containsHenshinTrace = containsDocType(documentTypes, "Henshin/Trace");

		if (containsHenshinTrace) {
			removeDocType(documentTypes, "Henshin/Trace");
		}
		if (containsUML && documentTypes.size() > 1) {
			removeDocType(documentTypes, "UML");
		}

		// default
		if (documentTypes.size() == 1) {
			return documentTypes.get(0);
		} else {
			return docTypes.iterator().next();
		}
	}

	private static boolean containsDocType(Collection<String> documentTypes, String docTypeFragment) {
		for (String string : documentTypes) {
			String docType = string;
			if (docType.contains(docTypeFragment)) {
				return true;
			}
		}

		return false;
	}

	private static void removeDocType(List<String> documentTypes, String docTypeFragment) {
		for (Iterator<String> iterator = documentTypes.iterator(); iterator.hasNext();) {
			String docType = iterator.next();
			if (docType.contains(docTypeFragment)) {
				iterator.remove();
			}
		}
	}

	/**
	 * For each of the model objects of the given modelResource, the retrieval of
	 * the document type is simply delegated to the method
	 * {@link EMFModelAccessEx#getDocumentType(EObject)}. The set of document
	 * types which is obtained this way is finally returned.
	 * 
	 * @param modelResource
	 * @return
	 */
	public static Set<String> getDocumentTypes(Resource modelResource, Scope scope) {
		if (scope == Scope.RESOURCE_SET) {
			return getDocumentTypes(modelResource.getResourceSet().getResources());
		}
		return getDocumentTypes(Collections.singleton(modelResource));
	}

	public static Set<String> getDocumentTypes(Notifier context) {
		if(context instanceof ResourceSet) {
			return getDocumentTypes(((ResourceSet)context).getResources());
		} else if(context instanceof Resource) {
			return getDocumentTypes(Collections.singleton((Resource)context));
		} else if(context instanceof EObject) {
			EObject eObj = (EObject)context;
			Resource resource = eObj.eResource();
			if(resource == null) {
				return getDocumentTypes(Collections.singleton(eObj));
			}
			return getDocumentTypes(Collections.singleton(resource));			
		}
		throw new AssertionError();
	}

	/**
	 * Simple heuristic to check whether a model is profiled or not:
	 * <ul>
	 * <li>more than one docType</li>
	 * <li>containsUML</li>
	 * <li>!containsHenshinTrace</li>
	 * </ul>
	 * 
	 * Feel free to refine the above heuristic.
	 * 
	 * @return true if the model contained by the given resource is profiled.
	 */
	public static boolean isProfiled(Resource model) {
		Set<String> documentTypes = getDocumentTypes(model, Scope.RESOURCE);
		boolean containsUML = containsDocType(documentTypes, "UML");
		boolean containsHenshinTrace = containsDocType(documentTypes, "Henshin/Trace");

		return (documentTypes.size() > 1) && containsUML && !containsHenshinTrace;
	}

	/**
	 * Returns the Resource which contains or is the given context notifier.
	 * @param context the context (Resource, EObject)
	 * @return Resource containing/being the context, <code>null</code> if none
	 */
	public static Resource getResource(Notifier context) {
		if(context instanceof Resource) {
			return (Resource)context;
		} if(context instanceof EObject) {
			return ((EObject)context).eResource();
		}
		return null;
	}

	/**
	 * Returns the ResourceSet which contains or is the given context notifier.
	 * @param context the context (ResourceSet, Resource, EObject)
	 * @return ResourceSet containing/being the context, <code>null</code> if none
	 */
	public static ResourceSet getResourceSet(Notifier context) {
		if(context instanceof ResourceSet) {
			return (ResourceSet)context;
		} else if(context instanceof Resource) {
			return ((Resource)context).getResourceSet();
		} else if(context instanceof EObject) {
			return getResourceSet(((EObject)context).eResource());
		}
		return null;
	}
}
