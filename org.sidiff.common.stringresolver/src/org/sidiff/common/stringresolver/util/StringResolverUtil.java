package org.sidiff.common.stringresolver.util;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.sidiff.common.stringresolver.GenericStringResolver;
import org.sidiff.common.stringresolver.IStringResolver;

/**
 * 
 * @author cpietsch
 *
 */
public class StringResolverUtil {

	/**
	 * Returns an available string resolver for the given document type. If no
	 * convenient resolver is found, <code>null</code> will be returned.
	 * 
	 * @param documentType
	 * @return an {@link IStringResolver} which is able to handle the given
	 *         document type, <code>null</code> otherwise.
	 */
	public static IStringResolver getAvailableStringResolver(String documentType){
		
		IStringResolver stringResolver = null;
		for (IConfigurationElement configurationElement : Platform.getExtensionRegistry().getConfigurationElementsFor(IStringResolver.extensionPointID)) {
			try {
				IStringResolver stringResolverExtension = (IStringResolver) configurationElement.createExecutableExtension("string_resolver");
				if (documentType.equals(stringResolverExtension.getDocType())) {
					stringResolver = stringResolverExtension;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(stringResolver == null){
			stringResolver = new GenericStringResolver();
		}
		
		return stringResolver;
	}
}
