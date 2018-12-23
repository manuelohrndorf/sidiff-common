package org.sidiff.common.emf.internal;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sidiff.common.converter.ObjectConverter;
import org.sidiff.common.emf.modelstorage.ModelStorage;

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
		String path = uri.toFileString();
		String fragment = uri.fragment();
		Resource resource = ModelStorage.getInstance().loadEMF(URI.createFileURI(path));
		return resource.getEObject(fragment);
	}

	@Override
	public EObject getDefaultValue() {
		return null;
	}
}
