package org.sidiff.core.annotators;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.sidiff.common.emf.EMFAdapter;
import org.sidiff.common.emf.EMFUtil;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.common.emf.access.EdgeSemantic;
import org.sidiff.common.emf.annotation.AnnotateableElement;
import org.sidiff.common.emf.collections.EMFComparators;
import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.util.ParamUtil;
import org.sidiff.common.util.ParamUtil.Argument;
import org.sidiff.common.util.ParamUtil.UnparsedParameterStringException;
import org.sidiff.core.annotation.Annotator;

/**
 * Annotator to compute hash values. This annotator builds signatures in a
 * subtractive way. It includes a fixed set of information in the hash value,
 * whereas some of these information can be subtracted by the use of
 * configuration parameters (see below).
 * 
 * 
 * 
 * ParameterString (Die Reihenfolge spielt hier keine Rolle):
 * 
 * localPositionAnnotationKey = <KEY>,					(required wenn globalMoveAllowed=false, sonst optional)
 * referencedObjectsPositionAnnotationKey = <KEY>,		(optional) Achtung: Wenn nicht angegeben, werden keine referenzierten Objekte gehasht!
 * globalMoveAllowed = true|false,						(required)
 * suppressChildren = <containment1>;<containment2> ...	(optional)
 * ignoreOrder = <ref1>;<ref2> ...						(optional)
 * exclude = <feature1>;<feature2> ...					(optional)
 * 
 */
public class HashAnnotator extends Annotator {

	public static final String HASH_ANNOTATION_KEY = "HASH";
	
	// Some constants
	private static final String NO_HASH_ECORE_ANNOTATION = "NOHASH";
	private static final String LOCAL_POS_ANNOTATION_KEY = "localPositionAnnotationKey";
	private static final String REF_POS_ANNOTATION_KEY = "referencedObjectsPositionAnnotationKey";
	private static final String GLOBAL_MOVE = "globalMoveAllowed";
	private static final String SUPPRESS_CHILDREN = "suppressChildren";
	private static final String IGNORE_ORDER = "ignoreOrder";
	private static final String EXCLUDE = "exclude";

	private static final String PARAM_ERROR_MSG = "Malformed HashAnnotator parameter string. See documentation for details!";
	private static final String DEFAULT_HASH_ALGORITHM = "SHA";

	// The parameter values
	private boolean allowGlobalMove;

	private String localPositionAnnotationKey = null;
	private String referencedObjectsPositionAnnotationKey = null;

	private Set<EReference> ignoreOrder = new HashSet<EReference>();
	private Set<EStructuralFeature> excludes = new HashSet<EStructuralFeature>();
	private Set<EReference> suppressedChildren = new HashSet<EReference>();
	
	/**
	 * The hash algorithm
	 */
	private MessageDigest md = null;
	
	/**
	 * 
	 * @param annotationKey
	 * @param parameter
	 *            see above
	 * @param acceptedTypes
	 * @param requiredAnnotations
	 * @param order
	 */
	@SuppressWarnings("unchecked")
	public HashAnnotator(EPackage documentType, String annotationKey, String parameter, EClass acceptedType, Collection<String> requiredAnnotations) {
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.POST);

		assert (annotationKey.equals(HASH_ANNOTATION_KEY)) : "Wrong Configuration: Annotation Key must be 'HASH'";

		// Parameter Parsing
		ParamUtil parser = new ParamUtil();
		Argument<String> arg_localPos = parser.createParameter(LOCAL_POS_ANNOTATION_KEY, String.class, false);
		Argument<String> arg_refPos = parser.createParameter(REF_POS_ANNOTATION_KEY, String.class, false);
		Argument<Boolean> arg_globalMove = parser.createParameter(GLOBAL_MOVE, Boolean.class, true);
		Argument<LinkedList> arg_suppressChildren = parser.createParameter(SUPPRESS_CHILDREN, LinkedList.class, false);
		Argument<LinkedList> arg_ignoreOrder = parser.createParameter(IGNORE_ORDER, LinkedList.class, false);
		Argument<LinkedList> arg_exclude = parser.createParameter(EXCLUDE, LinkedList.class, false);
		boolean res = parser.parse(getParameter());

		// parsing successful?
		assert (res) : PARAM_ERROR_MSG;

		try {
			this.localPositionAnnotationKey = arg_localPos.getValue();

			this.referencedObjectsPositionAnnotationKey = arg_refPos.getValue();
			this.allowGlobalMove = arg_globalMove.getValue();

			// Ignore Order
			if (arg_ignoreOrder.getValue() != null) {
				for (Object featureName : arg_ignoreOrder.getValue()) {
					assert (featureName instanceof String) : PARAM_ERROR_MSG;
					List<EStructuralFeature> refs = EMFMetaAccess.getEStructuralFeaturesByRegEx(acceptedType, (String)featureName, true);
					assert(refs.size()>=1):"No valid feature found: " +  featureName;
					for (EStructuralFeature eStructuralFeature : refs) {
						assert (eStructuralFeature instanceof EReference) : "No valid reference type: " + eStructuralFeature.getName();
						assert (eStructuralFeature.isOrdered()) : "ignore order parameter only makes sense for ordered reference types";
						ignoreOrder.add((EReference) eStructuralFeature);
					}
				}
			}

			// Excludes
			if (arg_exclude.getValue() != null) {
				for (Object featureName : arg_exclude.getValue()) {
					assert (featureName instanceof String) : PARAM_ERROR_MSG;
					List<EStructuralFeature> feats = EMFMetaAccess.getEStructuralFeaturesByRegEx(acceptedType, (String) featureName, true);
					assert(feats.size()>=1):"No valid feature found: " +  featureName;
					for (EStructuralFeature eStructuralFeature : feats) {						
						assert(checkExcludeConstraint(eStructuralFeature)):featureName + " must no be a containment reference. These should be handled by suppressChildren parameter";
						excludes.add(eStructuralFeature);
					}
				}
			}
			
			// Suppressed Children
			if (arg_suppressChildren.getValue() != null) {
				for (Object featureName : arg_exclude.getValue()) {
					assert (featureName instanceof String) : PARAM_ERROR_MSG;
					List<EStructuralFeature> feats = EMFMetaAccess.getEStructuralFeaturesByRegEx(acceptedType, (String) featureName, true);
					assert(feats.size()>=1):"No valid feature found: " +  featureName;
					for (EStructuralFeature eStructuralFeature : feats) {						
						assert(eStructuralFeature instanceof EReference): featureName +" must be a reference";
						EReference ref = (EReference) eStructuralFeature;
						assert(ref.isContainment()): featureName +" must be a containment reference";
						suppressedChildren.add(ref);
					}
				}
			}
			
			assert(checkGlobalMoveParamConstraint()) : "globalMove is not allowed, so we need a localPositionAnnotationKey!";
			assert(checkUseRequiredConstraint()) : "There are used Keys that are not declared as required!";
			
		} catch (UnparsedParameterStringException e) {
			// Should never happen
		}
		
		try {
			this.md = MessageDigest.getInstance(DEFAULT_HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new SiDiffRuntimeException("Unknown hashing algorithm " + DEFAULT_HASH_ALGORITHM, e);
		}
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		AnnotateableElement annotateable = EMFAdapter.INSTANCE.adapt(object, AnnotateableElement.class);

		// clear the MessageDigest
		this.md.reset();

		// HASHING the NodeType
		this.md.update(object.eClass().getName().getBytes());

		if (!allowGlobalMove) {
			// Move not allowed, so hash own path
			this.md.update(annotateable.getAnnotation(this.localPositionAnnotationKey, String.class).getBytes());
		}

		// HASHING the Attributes
		hashAttributes(object);

		// HASHING the Children
		
		List<EReference> childrenRefs =	EMFMetaAccess.getChildrenReferences(object.eClass());
		for (EReference eReference : childrenRefs) {
			if (!suppressedChildren.contains(eReference)){
				hashReferencedObjects(object, EMFMetaAccess.getChildrenReferences(object.eClass()), HASH_ANNOTATION_KEY);
			}
		}
			
		// HASHING the References
		if (referencedObjectsPositionAnnotationKey != null)
			hashReferencedObjects(object, EMFMetaAccess.getReferences(object.eClass(), EdgeSemantic.Outgoing), referencedObjectsPositionAnnotationKey);

		// Return the generated hash value
		return getCurrentHashValue();
	}

	/**
	 * Returns the hash value that is calculated so far
	 * 
	 * @return A String containing the hash value that is calculated so far
	 */
	private String getCurrentHashValue() {
		StringBuffer buf = new StringBuffer();
		byte[] digest = md.digest();
		for (int i = 0; i < digest.length; i++) {
			buf.append(Integer.toHexString(digest[i] & 0xff));
		}
		return buf.toString();
	}

	/**
	 * Hashes the attributes of a given node.
	 * 
	 * @param node
	 *            The node whos attributes are hashed.
	 */
	private void hashAttributes(EObject object) {

		List<EAttribute> attributes = new ArrayList<EAttribute>(object.eClass().getEAllAttributes());

		Collections.sort(attributes, EMFComparators.ATTRIBUTE_BY_NAME);

		for (EAttribute attrib : attributes) {
			if (attrib.isID() || attrib.getEAnnotation(NO_HASH_ECORE_ANNOTATION) != null)
				continue;
			if (excludes.contains(attrib))
				continue;

			md.update(attrib.getName().getBytes());
			try {
				md.update(("" + object.eGet(attrib)).getBytes());
			} catch (UnsupportedOperationException e) {
				// Nothing to do, data not available (MS,05.02.2010)
			}
		}

		// // TODO ?? Implement: Maps allgemein als Attribute zugaenglich machen
		// z.B. EMFUtil (SW, 17.10.08)
		// // include Attribute Maps
		// ArrayList<EReference> sortedRefs = new
		// ArrayList<EReference>(EMFMetaAccess.getChildrenReferences(object.eClass()));
		// Collections.sort(sortedRefs, EREFERENCE_COMPARATOR);
		// for (EReference reference : sortedRefs) {
		// // Only analyze Maps
		// if (!reference.getEType().getName().matches(".*To.*Map"))
		// continue;
		// EMap<String, Object> map = (EMap<String, Object>)
		// object.eGet(reference);
		// List<String> keys = new ArrayList<String>(map.keySet());
		// Collections.sort(keys);
		// for (String key : keys) {
		// if (isIgnored(object.eClass().getName() + "_" + key))
		// continue;
		// md.update(key.getBytes());
		// md.update(("" + map.get(key)).getBytes());
		// }
		// }

	}

	/**
	 * Hashes the referenced target nodes of a given node.
	 * 
	 * @param object
	 *            The node whos target nodes shall be hashed
	 * @param references
	 *            The reference types whos instances we shall follow in order to
	 *            get the target nodes
	 * @param annotationKey
	 *            The annotation key for the computed hash value
	 */
	private void hashReferencedObjects(EObject object, List<EReference> references, String annotationKey) {

		ArrayList<EReference> sortedRefs = new ArrayList<EReference>(references);
		Collections.sort(sortedRefs, EMFComparators.REFERENCE_BY_NAME);

		for (EReference reference : sortedRefs) {
			if (reference.getEAnnotation(NO_HASH_ECORE_ANNOTATION) != null)
				continue;
			if (excludes.contains(reference))
				continue;

			// Ignore Maps
			if (reference.getEType().getName().matches(".*To.*Map"))
				continue;

			// Get the target nodes that are referenced
			ArrayList<EObject> targets = new ArrayList<EObject>();
			EMFUtil.fillObjectListFromReference(targets, object, reference);

			// If order is irrelevant, sort the list of target nodes
			if (!reference.isOrdered() || ignoreOrder.contains(reference)) {
				assert(referencedObjectsPositionAnnotationKey != null) : "Reference '" + reference.getName() + "' is either unordered or its order shall be ignored, so the HashAnnotator needs parameter referencedObjectsAnnotationKey for class '" + this.getType().getName() + "' to be specified";
				Collections.sort(targets, EMFComparators.createObjectByAnnotationComparator(referencedObjectsPositionAnnotationKey));
			}

			for (EObject target : targets) {
				this.md.update(reference.getName().getBytes());

				AnnotateableElement annoTarget = EMFAdapter.INSTANCE.adapt(target, AnnotateableElement.class);
				String anno = annoTarget.getAnnotation(annotationKey, String.class);
				if (anno == null) {
					// externe (proxy) elemente mit der URI einhashen
					anno = EMFUtil.getEObjectURI(target);
				}

				this.md.update(anno.getBytes());
			}
		}
	}

	private boolean checkExcludeConstraint(EStructuralFeature feature){
		if (feature instanceof EReference){
			return !((EReference)feature).isContainment();
		}
		return true;
	}
	
	/**
	 * When global move is not allowed, parameter 'localPositionAnnotationKey' must
	 * be given
	 * 
	 * @return
	 */
	private boolean checkGlobalMoveParamConstraint() {	
		
		if (!allowGlobalMove) {
			return localPositionAnnotationKey != null;
		}
		return true;
	}

	private boolean checkUseRequiredConstraint() {
		
		boolean result = true;
		
		result &= (localPositionAnnotationKey==null||getRequiredAnnotations().contains(localPositionAnnotationKey));
		result &= (referencedObjectsPositionAnnotationKey==null||getRequiredAnnotations().contains(referencedObjectsPositionAnnotationKey));
		
		return result;
	}

}
