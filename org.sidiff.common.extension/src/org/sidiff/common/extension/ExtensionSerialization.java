package org.sidiff.common.extension;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.sidiff.common.extension.configuration.IConfigurableExtension;
import org.sidiff.common.extension.configuration.IExtensionConfiguration;
import org.sidiff.common.util.StringListSerializer;

import com.eclipsesource.json.*;
import com.eclipsesource.json.JsonObject.Member;

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
			return Json.NULL.toString();
		} else if(extension instanceof IConfigurableExtension) {
			IExtensionConfiguration configuration = ((IConfigurableExtension)extension).getConfiguration();
			if(!configuration.getConfigurationOptions().isEmpty()) {
				return Json.object()
						.add(extension.getKey(), ((IExtensionConfiguration.Internal)configuration).exportAssignments())
						.toString();
			}
		}
		return Json.value(extension.getKey()).toString();
	}

	public static <T extends IExtension> T createExtension(ExtensionManager<? extends T> manager, String data) {
		if(data == null || data.isEmpty()) {
			return null;
		}
		try {
			JsonValue parsed = Json.parse(data);
			String extensionKey;
			JsonObject configuration;
			if(parsed.isNull()) {
				return null;
			} else if(parsed.isString()) {
				extensionKey = parsed.asString();
				configuration = null;
			} else if(parsed.isObject()) {
				JsonObject object = parsed.asObject();
				Assert.isLegal(object.size() == 1, "Extension must be single key value pair object");
				Member member = object.iterator().next();
				Assert.isLegal(member.getValue().isObject(), "Configuration must be a json object");
				extensionKey = member.getName();
				configuration = member.getValue().asObject();
			} else {
				throw new IllegalArgumentException("Unexpected json value: " + data);
			}
			T extension = manager.getExtension(extensionKey)
					.orElseThrow(() -> new IllegalArgumentException(
							"Could not find extension " + extensionKey + " in manager " + manager));
			if(extension instanceof IConfigurableExtension && configuration != null) {
				((IExtensionConfiguration.Internal)((IConfigurableExtension)extension).getConfiguration())
					.importAssignments(configuration);
			}
			return extension;
		} catch(ParseException e) {
			// Legacy import
			List<String> splitData = COLON_SIGN_SERIALIZER.deserialize(data);
			if(splitData.size() > 2) {
				throw new IllegalArgumentException(
						"Only one colon sign in serialized extension configuration expected. Data: " + data);
			}
			T extension = manager.getExtension(splitData.get(0))
					.orElseThrow(() -> new IllegalArgumentException(
							"Could not find extension " + splitData.get(0) + " in manager " + manager));
			if(extension instanceof IConfigurableExtension && splitData.size() > 1) {
				((IExtensionConfiguration.Internal)((IConfigurableExtension)extension).getConfiguration())
					.importAssignments(splitData.get(1));
			}
			return extension;
		}
	}
}
