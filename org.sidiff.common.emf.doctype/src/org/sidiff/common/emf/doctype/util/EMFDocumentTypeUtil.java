package org.sidiff.common.emf.doctype.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.common.emf.access.Scope;
import org.sidiff.common.emf.doctype.IDocumentTypeResolver;

/**
 * 
 * @author cpietsch
 *
 */
public class EMFDocumentTypeUtil {
	
	public static List<String> resolve(Resource resource) {
		List<String> docTypes = new ArrayList<String>();
		
		// try to resolve document types via registered IDocumentTypeResolvers
		//
		for(IDocumentTypeResolver resolver : getAvailableDeltaModuleOperations()) {
			docTypes.addAll(resolver.resolve(resource));
			if(!docTypes.isEmpty()) break;
		}
		
		// if no document types are resolved use the document types of the resource
		//
		if(docTypes.isEmpty()) {
			docTypes.addAll(EMFModelAccess.getDocumentTypes(resource, Scope.RESOURCE_SET));
			
		}
		
		docTypes.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		
		return docTypes;
	}

	/**
	 * 
	 * @return
	 */
	private static List<IDocumentTypeResolver> getAvailableDeltaModuleOperations(){
		List<IDocumentTypeResolver> resolver = new ArrayList<IDocumentTypeResolver>();

		for (IConfigurationElement configurationElement : Platform.getExtensionRegistry().getConfigurationElementsFor(
				IDocumentTypeResolver.extensionPointID)) {
			try {
				IDocumentTypeResolver resolverExtension = (IDocumentTypeResolver) configurationElement.createExecutableExtension(IDocumentTypeResolver.attribute_class);
				resolver.add(resolverExtension);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		resolver.sort(new Comparator<IDocumentTypeResolver>() {

			@Override
			public int compare(IDocumentTypeResolver arg0, IDocumentTypeResolver arg1) {
				
				return arg0.getDocumentType().compareTo(arg1.getDocumentType());
			}
			
		});
		return resolver;
	}
}
