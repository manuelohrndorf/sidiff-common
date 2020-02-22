package org.sidiff.common.extension;

import java.util.Arrays;
import java.util.List;

import org.sidiff.common.extension.configuration.IConfigurableExtension;
import org.sidiff.common.extension.configuration.IExtensionConfiguration;
import org.sidiff.common.util.StringListSerializer;

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

	static final StringListSerializer COLON_SIGN_SERIALIZER = new StringListSerializer(":");

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
				return COLON_SIGN_SERIALIZER.serialize(
						Arrays.asList(extension.getKey(), configuration.exportAssignments()));
			}
		}
		return extension.getKey();
	}

	public static <T extends IExtension> T createExtension(ExtensionManager<? extends T> manager, String data) {
		if(data == null || data.isEmpty()) {
			return null;
		}
		List<String> splitData = COLON_SIGN_SERIALIZER.deserialize(data);
		if(splitData.size() > 2) {
			throw new IllegalArgumentException(
					"Only one colon sign in serialized extension configuration expected. Data: " + data);
		}
		T extension = manager.getExtension(splitData.get(0))
				.orElseThrow(() -> new IllegalArgumentException(
						"Could not find extension " + splitData.get(0) + " in manager " + manager));
		if(extension instanceof IConfigurableExtension && splitData.size() > 1) {
			((IConfigurableExtension)extension).getConfiguration().importAssignments(splitData.get(1));
		}
		return extension;
	}
}
