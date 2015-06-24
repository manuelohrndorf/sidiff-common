package org.sidiff.core.annotation;

import java.util.*;

import org.eclipse.emf.ecore.*;
import org.sidiff.common.emf.EMFAdapter;
import org.sidiff.common.emf.annotation.AnnotateableElement;

/**
 * Diese Klasse repraesentiert einen abstrakten annotator. Die Funktion eines
 * annotators ist es fuer einen bestimmten Objekttyp (bzw. dessen Subtypen)
 * einen definierten "Annotationswert" zu berechnen.
 * 
 * Konkrete implementierungen _muessen_ folgenden Konstruktor implementieren:
 * 
 * Annotator(EPackage documentType, String annotationKey, String parameter, 
 *			EClass acceptedType, Collection<String> requiredAnnotations)
 * 
 * Die Execution Order muss die konkrete Implemenierung wissen und waehrend
 * des super-Aufrufs mitteilen.  		
 * 
 * @author Maik Schmidt
 *
 */
public abstract class Annotator {

	public enum ExecutionOrder {
		PRE, 
		POST;
	}

	private String annotationKey = null;
	private String parameter = null;
	private EClass acceptedType = null;
	private ExecutionOrder order = null;	
	private EPackage documentType = null;
	
	private List<String> requiredAnnotations = new LinkedList<String>();
	
	/**
	 * @param documentType The document type (Root Package regarding the URI)
	 * @param annotationKey Key produced by the Annotator.
	 * @param parameter Parameter String (Optional) 
	 * @param acceptedTypes Set of acceped Types (Even all Subtypes will be processed, expect of annotationkey conflicts)
	 * @param requiredAnnotations Set of required annotation keys (executed annotatons) as precondition.
	 * @param order Pre od Postorder @see ExecutionOrder
	 */
	protected Annotator(EPackage documentType, String annotationKey, String parameter, 
			EClass acceptedType, Collection<String> requiredAnnotations, ExecutionOrder order) {
		
		assert(documentType!=null) : "Missing documenttype!";
		assert(acceptedType!=null) : "Don't know what to be accepted!";
		assert(order!=null) : "Don't know my execution order!";
		assert(annotationKey!=null&&!annotationKey.equals("")) : "Don't know what to annotate";
		
		// mandatory
		this.documentType = documentType;
		this.annotationKey = annotationKey;
		this.order = order;
		this.acceptedType = acceptedType;
		
		// optional 
		this.parameter = parameter;
		
		if(requiredAnnotations!=null){
			this.requiredAnnotations.addAll(requiredAnnotations);
		}
		this.requiredAnnotations = Collections.unmodifiableList(this.requiredAnnotations); // Protect state
	}

	/**
	 * Returns the key which is used to store the annotations.
	 * 
	 * @return The key which is used to store the annotations.
	 */
	public String getAnnotationKey() {
		
		return annotationKey;
	}

	/**
	 * Returns the EClass on which the annotator can be executed.
	 * 
	 * @return The EClass on which the annotator is executeable.
	 */
	public EClass getType() {
		
		return this.acceptedType;
	}

	/**
	 * Returns the parameter which can be used by the annotator while traversing the tree. E.g. name of an attribute to be analyzed.
	 * 
	 * @return Parameter which can be used by the annotator while traversing the tree
	 */
	public String getParameter() {
		
		return this.parameter;
	}

	/**
	 * Returns the annotations which a prerequisites for this annotation to be executed.
	 * 
	 * @return The annotations which a prerequisites for this annotation to be executed.
	 */
	public Collection<String> getRequiredAnnotations() {
		
		return Collections.unmodifiableList(requiredAnnotations);
	}

	/**
	 * Returns the order of this annotator. I.e. whether it is executed before or after traversing the subtree of the given object.
	 * 
	 * @return The order of this annotator.
	 */
	public ExecutionOrder getOrder() {
		
		return this.order;
	}

	public EPackage getDocumentType() {
		
		return this.documentType;
	}


	public void annotate(EObject object) {
		
		if(acceptedType.isSuperTypeOf(object.eClass())||acceptedType==EcorePackage.eINSTANCE.getEObject()){
				AnnotateableElement annotateable = EMFAdapter.INSTANCE.adapt(object, AnnotateableElement.class);
				Object value = computeAnnotationValue(object);
				annotateable.setAnnotation(annotationKey, value);
		} else {
			throw new IllegalArgumentException("Cannot process object of type "+object.eClass().getName());
		}
	}

	/**
	 * This method computes the actual annotation value.
	 * 
	 * @param object
	 *            The object for which an annotation value should be computed.
	 * @return The annotation value
	 */
	protected abstract Object computeAnnotationValue(EObject object);

	/**
	 * Helper Method to compute a extended set of requirements while calling the super constructor.
	 * 
	 * @param requiredAnnotations
	 * @param additionalRequirements
	 * @return a new set of required annotations 
	 */
	protected static Collection<String> addAdditionalRequirements(Collection<String> requiredAnnotations, String... additionalRequirements) {

		List<String> requirements = new LinkedList<String>(requiredAnnotations);
		for(String additionalRequirement : additionalRequirements){
			requirements.add(additionalRequirement);
		}
		return requirements;
	}

}
