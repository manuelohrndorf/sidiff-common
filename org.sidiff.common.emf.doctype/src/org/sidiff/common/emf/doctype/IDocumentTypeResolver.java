package org.sidiff.common.emf.doctype;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;


public interface IDocumentTypeResolver {

	public static String extensionPointID = "org.sidiff.common.emf.doctype.document_type_resolver";
	public static String attribute_class = "class";

	public List<String> resolve(Resource resource);

	public String getDocumentType();
}
