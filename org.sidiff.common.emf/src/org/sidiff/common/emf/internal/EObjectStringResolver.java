package org.sidiff.common.emf.internal;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.common.emf.EMFUtil;
import org.sidiff.common.util.StringResolver;

/**
 * StringResolver that creates a string representation of an EObject.
 * @author wenzel
 *
 */
public class EObjectStringResolver implements StringResolver {

	@Override
	public String resolve(Object obj) {
		if (obj==null || !(obj instanceof EObject))
			return null;
		EObject eobj = (EObject)obj;
		String s = EMFUtil.getModelRelativeName(eobj.eClass()) + "(";
		String n = EMFUtil.getEObjectName(eobj);
		if(n != null) {
			s += n + ",";
		}
		s += EMFUtil.getEObjectID(eobj)+")";
		return s;
	}

	@Override
	public Class<?> dedicatedClass() {
		
		return EObject.class;
	}

}
