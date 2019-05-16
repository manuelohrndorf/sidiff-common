package org.sidiff.common.emf.doctype.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.common.emf.access.Scope;
import org.sidiff.common.emf.doctype.IDocumentTypeResolver;

/**
 * <code>EMFDocumentTypeUtil</code> contains utility functions for resolving
 * the document types of {@link Resource}s.
 * @author cpietsch, Robert M�ller
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
		Set<String> docTypes = new HashSet<>();
		for(Resource resource : resources) {
			if(resource == null || resource.getContents().isEmpty()) {
				continue;
			}

			Set<String> modelDocTypes = EMFModelAccess.getDocumentTypes(resource, Scope.RESOURCE_SET);

			// try to resolve document types via registered IDocumentTypeResolvers
			//
			for(String docType : modelDocTypes) {
				Collection<IDocumentTypeResolver> resolvers =
						IDocumentTypeResolver.MANAGER.getExtensions(Collections.singleton(docType), false);
				if(!resolvers.isEmpty()) {
					for(IDocumentTypeResolver resolver : resolvers) {
						List<String> resolvedTypes = resolver.resolve(resource);
						if(resolvedTypes != null) {
							// the document type is resolved with the first resolver that
							// returns a result, i.e. not null
							docTypes.addAll(resolvedTypes);
							break; // other resolvers for this document type are ignored
						}
					}
				} else {
					// if no resolver for this document type was found,
					// it is added to the result as is
					docTypes.add(docType);
				}
			}
		}

		// create and sort list of document types
		List<String> docTypesList = new ArrayList<>(docTypes);
		docTypesList.sort(Comparator.naturalOrder());
		return docTypesList;
	}
}
