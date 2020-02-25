package org.sidiff.common.emf.access;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.sidiff.common.emf.modelstorage.SiDiffResourceSet;

/**
 * 
 * @author cpietsch
 *
 */
public class EMFGenModelAccess {

	/**
	 * Returns all resource file extensions for a given set of document types.
	 * 
	 * @param nsURI
	 *            The namespace URI(s) representing the document type(s) of a
	 *            resource
	 * @return file extensions for the given document type
	 */
	public static Set<String> getFileExtensions(Set<String> nsURIs) {
		Set<String> fileExtensions = new HashSet<>();
		SiDiffResourceSet resourceSet = SiDiffResourceSet.create();
		Map<String, URI> nsURIMap = EcorePlugin.getEPackageNsURIToGenModelLocationMap(false);
		for (String nsURI : nsURIs) {
			URI uriGenModel = nsURIMap.get(nsURI);
			if (uriGenModel != null) {
				GenModel genModel = resourceSet.loadEObject(uriGenModel, GenModel.class);
				if (genModel != null) {
					for (GenPackage genPackage : genModel.getGenPackages()) {
						fileExtensions.addAll(genPackage.getFileExtensionList());
					}
				}
			}
		}
		return fileExtensions;
	}
}
