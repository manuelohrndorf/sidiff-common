package org.sidiff.common.emf.access;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.sidiff.common.emf.modelstorage.SiDiffResourceSet;

/**
 * @author cpietsch
 * @author rmueller
 */
public class EMFGenModelAccess {

	/**
	 * Returns all resource file extensions for a given set of document types.
	 * 
	 * @param nsURIs
	 *            The namespace URI(s) representing the document type(s) of a
	 *            resource
	 * @return file extensions for the given document types
	 */
	public static Set<String> getFileExtensions(Set<String> nsURIs) {
		return internalGetFileExtensions(nsURIs).collect(Collectors.toSet());
	}

	/**
	 * Returns the first available file extension for the given set of document types.
	 * Falls back to extension "xmi" if nothing else available.
	 * @param nsURIs
	 *            The namespace URI(s) representing the document type(s) of a
	 *            resource
	 * @return first available file extension for the document types, or "xmi"
	 */
	public static String getFileExtension(Set<String> nsURIs) {
		return internalGetFileExtensions(nsURIs).findFirst().orElse("xmi");
	}

	private static Stream<String> internalGetFileExtensions(Set<String> nsURIs) {
		SiDiffResourceSet resourceSet = SiDiffResourceSet.create();
		Map<String, URI> nsURIMap = EcorePlugin.getEPackageNsURIToGenModelLocationMap(false);
		return nsURIs.stream()
			.map(nsURIMap::get).filter(Objects::nonNull)
			.map(uri -> resourceSet.loadEObject(uri, GenModel.class)).filter(Objects::nonNull)
			.map(GenModel::getGenPackages).flatMap(Collection::stream)
			.map(GenPackage::getFileExtensionList).flatMap(Collection::stream);
	}
}
