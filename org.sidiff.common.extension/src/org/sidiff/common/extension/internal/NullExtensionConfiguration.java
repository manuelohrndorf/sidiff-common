package org.sidiff.common.extension.internal;

import java.util.*;

import org.sidiff.common.extension.configuration.ConfigurationOption;
import org.sidiff.common.extension.configuration.IExtensionConfiguration;

import com.eclipsesource.json.*;

/**
 * An empty extension configuration implementation without options.
 * Trying to add or set options results in runtime exceptions.
 * @author rmueller
 */
public class NullExtensionConfiguration implements IExtensionConfiguration, IExtensionConfiguration.Internal {

	@Override
	public void setOption(String key, Object value) {
		throw new UnsupportedOperationException("This extension has no options");
	}

	@Override
	public Object getOption(String key) {
		return null;
	}

	@Override
	public Map<String, Object> getOptions() {
		return Collections.emptyMap();
	}

	@Override
	public void setOptions(Map<String, Object> options) {
		if(!options.isEmpty()) {
			throw new UnsupportedOperationException("This extension cannot have options");
		}
	}

	@Override
	public void resetToDefaults() {
		// always default
	}

	@Override
	public Collection<ConfigurationOption<?>> getConfigurationOptions() {
		return Collections.emptySet();
	}

	@Override
	public JsonObject exportAssignments() {
		return Json.object();
	}

	@Override
	public void importAssignments(JsonObject serializedValue) {
		if(!serializedValue.isEmpty()) {
			throw new UnsupportedOperationException("This configuration cannot load any values");
		}
	}

	@Override
	public void importAssignments(String serializedValue) {
		if(!serializedValue.isEmpty()) {
			throw new UnsupportedOperationException("This configuration cannot load any values");
		}
	}

	@Override
	public void setDocumentTypeFilter(Collection<String> documentTypes, boolean includeGeneric) {
		// nothing selectable can be filtered
	}
}