package org.sidiff.common.emf.modelstorage;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;


public class XmiIdResourceFactoryImpl extends ResourceFactoryImpl {

	public static final Resource.Factory INSTANCE = new XmiIdResourceFactoryImpl();
	
	@Override
	public Resource createResource(URI uri) {
		return new XmiIdResourceImpl(uri);
	}
}
