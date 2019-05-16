package org.sidiff.common.emf.doctype;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.extension.ITypedExtension;
import org.sidiff.common.extension.TypedExtensionManager;

/**
 * An <code>IDocumentTypeResolver</code> resolves the document types
 * of {@link Resource}s for models with a specific document type.
 * @author cpietsch, Robert M�ller
 *
 */
public interface IDocumentTypeResolver extends ITypedExtension {

	Description<IDocumentTypeResolver> DESCRIPTION = Description.of(IDocumentTypeResolver.class,
			"org.sidiff.common.emf.doctype.document_type_resolver", "document_type_resolver", "class");

	TypedExtensionManager<IDocumentTypeResolver> MANAGER = new TypedExtensionManager<>(DESCRIPTION);

	/**
	 * Resolve the document types of the given resource. May return
	 * <code>null</code> if no document types could be resolved.
	 * @param resource the resource
	 * @return list of document types
	 */
	List<String> resolve(Resource resource);
}
