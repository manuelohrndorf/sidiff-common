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
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.common.emf.access.value.RemoteAttribute;
import org.sidiff.core.annotation.Annotator;

/**
 * Annotator to compose a unique Object-ID/Signature and for use with exact one Type!
 * 
 * The generated ID is composed of a (optional) remote, and at least one (but arbitrary many) local Attribute Values.
 * 
 * Syntax:
 * 
 * Local and remote Parts are seperated by '::'
 * There can be any count of remote parts, seperated by '?' - The first remote part, which deliver a result will be used! 
 * When there is no result, EcoreUtil.generateUUID() will be used to generate a value ! 
 * 
 * If many local attribute names are given, they are separated by ','
 * 
 * Examples:
 * 
 *  "Incomming.myref#myobjects[justthebest]/Parent#*"
 *  "Incomming.myref#myobjects[justthebest]/Parent#*@remoteIdAttribute?{...}::localIdAttributeName,anotherLocalAttr"
 *
 */
@Deprecated
public class DerivedIDAnnotator extends Annotator {
	
	private static final String VIRTUAL_TYPE_ATTRIBUTE = "{type}";

	public static final String DEFAULT_DERIVED_ID_ANNOTATION_KEY = "DERIVED_ID";
	
	private final static String REMOTE_LOCAL_PART_SEPERATOR = "::";
	private final static String REMOTE_ITEM_SEPERATOR ="\\?";
	private final static String LOCAL_ITEM_SEPERATOR =",";
	
	private final List<RemoteAttribute> valueDescriptors = new LinkedList<RemoteAttribute>();
	private final List<EAttribute> localAttributes = new LinkedList<EAttribute>();

	public DerivedIDAnnotator(EPackage documentType, String annotationKey, String parameter, EClass acceptedType, Collection<String> requiredAnnotations) {
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);

		// Check optional Preconditions
		if(parameter==null||parameter.equals("")){
			throw new IllegalArgumentException("Parameter needed!");
		}
		
		// Precompute
		String localAttributeNames[] = null;
		String params[] = parameter.split(REMOTE_LOCAL_PART_SEPERATOR);
		
		if(params.length>1){
			if(params.length>2){
				throw new IllegalArgumentException("Syntax Error: '"+parameter +"' (Multiple local sections)");
			}
			
			String remoteExpressions[] = params[0].split(REMOTE_ITEM_SEPERATOR);	
			for(String remoteExpression : remoteExpressions){
				remoteExpression = remoteExpression.replaceAll("\\s", "");
				valueDescriptors.add(EMFMetaAccess.translateRemoteAttribute(getType(),remoteExpression));
			}
			
			localAttributeNames = params[1].split(LOCAL_ITEM_SEPERATOR);
		} else {
			localAttributeNames = params[0].split(LOCAL_ITEM_SEPERATOR);
		}
		
		for (int i = 0; i < localAttributeNames.length; i++) {
			if(!localAttributeNames[i].equals(VIRTUAL_TYPE_ATTRIBUTE)){
				EStructuralFeature feature = getType().getEStructuralFeature(localAttributeNames[i].trim());
				if(feature!=null&&feature instanceof EAttribute){
					this.localAttributes.add((EAttribute)feature);
				} else {
					throw new IllegalArgumentException("Syntax Error in Local Expression:"+localAttributeNames[i]);
				}
			}
		}
		
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		assert(getType().isSuperTypeOf(object.eClass())) : "Invalid argument ("+object+") Invalid Object (wrong type)!"+getType().getName()+"/"+object.eClass().getName();
		
		StringBuffer buffer = new StringBuffer();
		
		if (!valueDescriptors.isEmpty()) {
			
			Object remoteValue = null;
			
			Iterator<RemoteAttribute> descriptors = valueDescriptors.iterator();
			while(remoteValue==null&&descriptors.hasNext()){
				remoteValue = EMFModelAccess.computeRemoteAttributeValue(object, descriptors.next());
			}
			
			buffer.append((remoteValue!=null)? remoteValue.toString() : EcoreUtil.generateUUID());
			// TODO: might be a recursive lookup! (derivedID of addressed Object)
			// This implementation does not lead to a match anyway
			
			buffer.append("/");
		}
		
		if(localAttributes.size()>0){
			for (Iterator<EAttribute> iterator = localAttributes.iterator(); iterator.hasNext();) {
				EAttribute localAttribute = iterator.next();
				buffer.append(object.eGet(localAttribute));
				if (iterator.hasNext()){
					buffer.append("/");
				}
			}					
		} else {
			buffer.append(object.eClass().getName());
		}
		
		return buffer.toString();
	}
}
