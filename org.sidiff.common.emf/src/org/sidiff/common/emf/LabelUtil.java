package org.sidiff.common.emf;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.common.stringresolver.IStringResolver;
import org.sidiff.common.stringresolver.util.StringResolverUtil;

public class LabelUtil {

	public static String getLabel(Object object){
		EObject eObject = (EObject)object;
		String docType = EMFModelAccess.getCharacteristicDocumentType(eObject.eResource());
		IStringResolver resolver = StringResolverUtil.getAvailableStringResolver(docType);
		
		return resolver.resolve(eObject);
		
	}
	
	public static String getToolTipLabel(Object object){
		EObject eObject = (EObject)object;
		String docType = EMFModelAccess.getCharacteristicDocumentType(eObject.eResource());
		IStringResolver resolver = StringResolverUtil.getAvailableStringResolver(docType);
		
		if (resolver != null){
			return resolver.resolveQualified(eObject);
		} else {
			return eObject.toString();
		}
	}
}
