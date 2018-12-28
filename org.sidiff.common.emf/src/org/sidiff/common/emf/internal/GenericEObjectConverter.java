package org.sidiff.common.emf.internal;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sidiff.common.converter.ObjectConverter;

public class GenericEObjectConverter implements ObjectConverter<EObject> {

	@Override
	public Class<EObject> getType() {
		return EObject.class;
	}

	@Override
	public String marshal(EObject object) {		
		return EcoreUtil.getURI(object).toString();
	}

	@Override
	public EObject unmarshal(String string) {
		URI uri = URI.createURI(string);
		Resource resource = new ResourceSetImpl().getResource(uri.trimFragment(), true);
		return resource.getEObject(uri.fragment());
	}

	@Override
	public EObject getDefaultValue() {
		return null;
	}
}
