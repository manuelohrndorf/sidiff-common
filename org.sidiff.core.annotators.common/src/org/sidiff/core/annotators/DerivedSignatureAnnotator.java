package org.sidiff.core.annotators;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.common.emf.access.value.RemoteAttribute;
import org.sidiff.core.annotation.Annotator;

/**
 * Annotator to compose a unique Object-ID/Signature and for use with exact one 
 * Type!
 * 
 * The generated ID is composed of a (optional) remote, and at least one (but
 * arbitrary many) local Attribute Values.
 * 
 * Syntax:
 * 
 * Local and remote Parts are seperated by '::' There can be any count of remote
 * parts, seperated by '?' The values will be concatenated with ":" If many
 * local attribute names are given, they are separated by ','
 * 
 * supports also configuration with only remote or local parts. 
 */
//both=remote1?remote2::local1,local2 
//only remote = remote1?remote2 
//only local = ::local1,local2
/**
 * Examples:
 * 
 * "Incomming.myref#myobjects[justthebest]/Parent#*"
 * "Incomming.myref#myobjects[justthebest]/Parent#*@remoteIdAttribute?{...}::localIdAttributeName,anotherLocalAttr"
 * 
 */
public class DerivedSignatureAnnotator extends Annotator {

	private static final String VIRTUAL_TYPE_ATTRIBUTE = "{type}";

	public static final String DEFAULT_DERIVED_ID_ANNOTATION_KEY = "DERIVED_SIG";

	private final static String REMOTE_LOCAL_PART_SEPERATOR = "::";
	private final static String REMOTE_ITEM_SEPERATOR = "\\?";
	private final static String LOCAL_ITEM_SEPERATOR = ",";

	private final List<RemoteAttribute> valueDescriptors = new LinkedList<RemoteAttribute>();
	private final List<EAttribute> localAttributes = new LinkedList<EAttribute>();

	public DerivedSignatureAnnotator(EPackage documentType, String annotationKey, String parameter,
			EClass acceptedType, Collection<String> requiredAnnotations) {
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);

		// Check optional Preconditions
		if (parameter == null || parameter.equals("")) {
			throw new IllegalArgumentException("Parameter needed!");
		}
		// Precompute
		// only local
		if (parameter.startsWith(REMOTE_LOCAL_PART_SEPERATOR)) {
			fillLocalAttributes(parameter.replace(REMOTE_LOCAL_PART_SEPERATOR, "").split(LOCAL_ITEM_SEPERATOR));
		}
		// remote and local
		else if (parameter.contains(REMOTE_LOCAL_PART_SEPERATOR)) {
			fillLocalAttributes(parameter.split(REMOTE_LOCAL_PART_SEPERATOR)[1].split(LOCAL_ITEM_SEPERATOR));
			fillRemoteAttributes(parameter.split(REMOTE_LOCAL_PART_SEPERATOR)[0].split(REMOTE_ITEM_SEPERATOR));
		}
		// only remote
		else {
			fillRemoteAttributes(parameter.split(REMOTE_ITEM_SEPERATOR));
		}
	}

	private void fillLocalAttributes(String[] localAttributeNames) {
		for (String localAttribute : localAttributeNames) {
			if (!localAttribute.equals(VIRTUAL_TYPE_ATTRIBUTE)) {
				EStructuralFeature feature = getType().getEStructuralFeature(localAttribute.trim());
				if (feature != null && feature instanceof EAttribute) {
					this.localAttributes.add((EAttribute) feature);
				} else {
					throw new IllegalArgumentException("Syntax Error in Local Expression:" + localAttribute);
				}
			}
		}
	}

	private void fillRemoteAttributes(String[] remoteExpressions) {
		for (String remoteExpression : remoteExpressions) {
			remoteExpression = remoteExpression.replaceAll("\\s", "");
			valueDescriptors.add(EMFMetaAccess.translateRemoteAttribute(getType(), remoteExpression));
		}
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		assert (getType().isSuperTypeOf(object.eClass())) : "Invalid argument (" + object
				+ ") Invalid Object (wrong type)!" + getType().getName() + "/" + object.eClass().getName();

		StringBuffer buffer = new StringBuffer();

		if (valueDescriptors.isEmpty() && localAttributes.isEmpty()) {
			throw new IllegalArgumentException("valueDescriptors and localAttributes are empty");
		}

		if (!valueDescriptors.isEmpty()) {

			Object remoteValue = null;

			Iterator<RemoteAttribute> descriptors = valueDescriptors.iterator();
			while (descriptors.hasNext()) {
				remoteValue = EMFModelAccess.computeRemoteAttributeValue(object, descriptors.next());
				buffer.append(remoteValue);

				if (descriptors.hasNext()) {
					buffer.append(":");
				}
			}
		}

		if (localAttributes.size() > 0) {
			if (!buffer.toString().isEmpty()) {
				buffer.append("/");
			}
			for (Iterator<EAttribute> iterator = localAttributes.iterator(); iterator.hasNext();) {
				EAttribute localAttribute = iterator.next();
				buffer.append(object.eGet(localAttribute));
				if (iterator.hasNext()) {
					buffer.append(",");
				}
			}
		} else {
			// buffer.append(object.eClass().getName());
		}

		return buffer.toString();
	}
}
