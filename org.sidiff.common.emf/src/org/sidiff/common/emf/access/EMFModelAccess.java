package org.sidiff.common.emf.access;

import java.util.*;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.access.impl.*;
import org.sidiff.common.emf.access.path.*;
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
	 * Returns all referenced objects which are neither children nor parent and which are connected by an edge with the given semantic.
	 * 
	 * @param eObject
	 * @param semantic
	 * @return A list of referenced objects which are neither children nor parent and which are connected by an edge with the given semantic.
	 */
	public static List<EObject> getReferencedObjects(EObject eObject, EdgeSemantic semantic) {
		return modelAccessor.getReferencedObjects(eObject, semantic);
	}

	/**
	 * Returns all referenced objects of the given type which are neither children nor parent and which are connected by an edge with the given semantic.
	 * 
	 * @param eObject
	 * @param semantic
	 * @param type
	 * @return A list of referenced objects of the given type which are neither children nor parent and which are connected by an edge with the given semantic.
	 */
	public static List<EObject> getReferencedObjects(EObject eObject, EdgeSemantic semantic, EClass type) {
		return modelAccessor.getReferencedObjects(eObject, semantic, type);
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
	 * Returns all neighbored nodes, which are connected by a reference (including container/containment-references) of the given semantic.
	 * 
	 * @param object
	 * @param semantic
	 * @return A list of neighbored nodes, which are connected by a reference (including container/containment-references) of the given semantic.
	 */
	public static List<EObject> getNodeNeighbors(EObject object, EdgeSemantic semantic) {
		return modelAccessor.getNodeNeighbors(object, semantic);
	}

	/**
	 * Returns all neighbored nodes, which are connected by a reference (including container/containment-references) of the given semantic and which have the given type.
	 * 
	 * @param object
	 * @param semantic
	 * @param types
	 * @return A list of neighbored nodes, which are connected by a reference (including container/containment-references) of the given semantic and which have the given type.
	 */
	public static List<EObject> getNodeNeighbors(EObject object, EdgeSemantic semantic, EClass... types) {
		return modelAccessor.getNodeNeighbors(object, semantic, types);
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

}
