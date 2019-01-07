package org.sidiff.common.emf.modelstorage;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;

/**
 * 
 * @deprecated Use a {@link SiDiffResourceSet} instead of registering this factory globally,
 * as it breaks other functionality as all. SiDiffResourceSet provides finer control
 * over the resource factory.
 */
public class UUIDResourceFactoryImpl extends ResourceFactoryImpl {

	@Override
	public Resource createResource(URI uri) {
		return new UUIDResource(uri);
	}

}
