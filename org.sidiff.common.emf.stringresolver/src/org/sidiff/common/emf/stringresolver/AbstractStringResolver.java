package org.sidiff.common.emf.stringresolver;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.common.extension.AbstractTypedExtension;

/**
 * 
 * @author cpietsch
 *
 */
public abstract class AbstractStringResolver extends AbstractTypedExtension implements IStringResolver {

	@Override
	public String resolveQualified(EObject eObject) {
		String res = resolve(eObject);
		EObject eContainer = eObject.eContainer();
		while(eContainer != null){
			res = resolve(eContainer) + "." + res;		
			eContainer = eContainer.eContainer();
		}
		return res;
	}
}
