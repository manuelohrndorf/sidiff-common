package org.sidiff.common.emf.stringresolver;

import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.sidiff.common.emf.EMFUtil;

public class GenericStringResolver extends AbstractStringResolver {

	@Override
	public String resolve(EObject eObject) {
		String name = null;
		if(eObject instanceof EGenericType) {
			name = EMFUtil.getEGenericTypeSignature((EGenericType)eObject);
		} else if(eObject instanceof EAnnotation) {
			name = EMFUtil.getEAnnotationSignature((EAnnotation)eObject);
		} else if(eObject instanceof BasicEMap.Entry<?,?>) {
			name = EMFUtil.getEMapEntrySignature((BasicEMap.Entry<?,?>)eObject);
		} else {
			name = EMFUtil.getEObjectName(eObject);
		}
		if(name == null) {
			name = eObject.toString();
		}

		String uuid = EMFUtil.getXmiId(eObject);
		if(uuid != null) {
			return name + " [" + uuid + "]";
		}
		return name;
	}
}
