package org.sidiff.common.emf.doctype;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * An <code>IDocumentTypeResolver</code> resolves the document types
 * of {@link Resource}s for models with a specific document type.
 * @author cpietsch, Robert Müller
 *
 */
public interface IDocumentTypeResolver {

	public static final String extensionPointID = "org.sidiff.common.emf.doctype.document_type_resolver";
	public static final String attribute_class = "class";

	/**
	 * Resolve the document types of the given resource. May return
	 * <code>null</code> if no document types could be resolved.
	 * @param resource the resource
	 * @return list of document types
	 */
	public List<String> resolve(Resource resource);

	/**
	 * Returns the type of documents that this resolver can handle.
	 * @return document type
	 */
	public String getDocumentType();
}
