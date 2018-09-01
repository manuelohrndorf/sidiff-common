package org.sidiff.common.stringresolver;

import java.util.Collections;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.sidiff.common.emf.EMFUtil;
import org.sidiff.common.extension.ITypedExtension;

public class GenericStringResolver extends AbstractStringResolver {

	@Override
	public String resolve(EObject eObject) {
		String name = null;
		final EStructuralFeature nameFeature = eObject.eClass().getEStructuralFeature("name");
		if(nameFeature != null) {
			final Object value = eObject.eGet(nameFeature);
			if(value != null) {
				name = value.toString();
			}
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

	@Override
	public Set<String> getDocumentTypes() {
		return Collections.singleton(ITypedExtension.GENERIC_TYPE);
	}

	@Override
	public String getKey() {
		return "GenericStringResolver";
	}

	@Override
	public String getName() {
		return "Generic String Resolver";
	}
}
