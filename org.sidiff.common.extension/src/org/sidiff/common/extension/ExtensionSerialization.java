package org.sidiff.common.extension;

import org.sidiff.common.extension.configuration.IConfigurableExtension;
import org.sidiff.common.extension.configuration.IExtensionConfiguration;

/**
 * <p>Contains static factory methods which can be used to serialize / deserialize extensions
 * (including their configuration, if present), particularly to use an extension as an
 * EDataType in an ecore meta model.</p>
 * <p>Example: To serialize an EDataType named IMyExtension, create an EAnnotation inside the EDataType:</p>
 * <pre>
 * EAnnotation[source="http://www.eclipse.org/emf/2002/GenModel"]
 *   entry[key="convert", value="return ExtensionSerialization.convertToString(it);"]
 *   entry[key="create", value="return ExtensionSerialization.createExtension(IMyExtension.MANAGER, it);"]
 *</pre>
 * @author rmueller
 */
public class ExtensionSerialization {

	private ExtensionSerialization() {
		throw new AssertionError();
	}

	public static String convertToString(IExtension extension) {
		if(extension == null) {
			return "";
		}
		if(extension instanceof IConfigurableExtension) {
			IExtensionConfiguration configuration = ((IConfigurableExtension)extension).getConfiguration();
			if(!configuration.getConfigurationOptions().isEmpty()) {
				return extension.getKey() + ":" + configuration.exportAssignments();				
			}
		}
		return extension.getKey();
	}

	public static <T extends IExtension> T createExtension(ExtensionManager<? extends T> manager, String data) {
		if(data == null || data.isEmpty()) {
			return null;
		}
		String splitData[] = data.split(":");
		T extension = manager.getExtension(splitData[0])
				.orElseThrow(() -> new IllegalArgumentException("Could not find extension " + splitData[0] + " in manager " + manager));
		if(extension instanceof IConfigurableExtension && splitData.length >= 2) {
			((IConfigurableExtension)extension).getConfiguration().importAssignments(splitData[1]);
		}
		return extension;
	}
}
