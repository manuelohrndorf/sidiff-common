package org.sidiff.common.emf.doctype.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.common.emf.access.Scope;
import org.sidiff.common.emf.doctype.IDocumentTypeResolver;

/**
 * <code>EMFDocumentTypeUtil</code> contains utility functions for resolving
 * the document types of {@link Resource}s.
 * @author cpietsch, Robert Müller
 *
 */
public class EMFDocumentTypeUtil {

	/**
	 * Returns the resolved document types of the given resource.
	 * @param resource the resource
	 * @return list of document types
	 */
	public static List<String> resolve(Resource resource) {
		return resolve(Collections.singleton(resource));
	}

	/**
	 * Returns the resolved document types of the given resources.
	 * @param resources the resources
	 * @return list of document types
	 */
	public static List<String> resolve(Collection<Resource> resources) {
		Set<String> docTypes = new HashSet<String>();
		Map<String, IDocumentTypeResolver> resolvers = getAvailableDocumentTypeResolvers();

		for(Resource resource : resources) {
			if(resource == null || resource.getContents().isEmpty()) {
				continue;
			}

			Set<String> modelDocTypes = EMFModelAccess.getDocumentTypes(resource, Scope.RESOURCE_SET);

			// try to resolve document types via registered IDocumentTypeResolvers
			//
			for(String docType : modelDocTypes) {
				IDocumentTypeResolver resolver = resolvers.get(docType);
				if(resolver != null) {
					List<String> resolvedTypes = resolver.resolve(resource);
					if(resolvedTypes != null) {
						docTypes.addAll(resolvedTypes);
					}
				}else {
					docTypes.add(docType);
				}
			}
		}

		// create and sort list of document types
		List<String> docTypesList = new ArrayList<>(docTypes);
		docTypesList.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		return docTypesList;
	}

	/**
	 * Returns all available document type resolvers, as a map of document type -> resolver.
	 * @return all available document type resolvers
	 */
	protected static Map<String, IDocumentTypeResolver> getAvailableDocumentTypeResolvers() {
		Map<String, IDocumentTypeResolver> resolvers = new HashMap<>();

		for (IConfigurationElement configurationElement : Platform.getExtensionRegistry().getConfigurationElementsFor(
				IDocumentTypeResolver.extensionPointID)) {
			try {
				IDocumentTypeResolver resolverExtension = (IDocumentTypeResolver) configurationElement.createExecutableExtension(
						IDocumentTypeResolver.attribute_class);
				resolvers.put(resolverExtension.getDocumentType(), resolverExtension);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return resolvers;
	}
}
