package org.sidiff.common.extension.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.sidiff.common.extension.configuration.ConfigurationOption;
import org.sidiff.common.extension.configuration.IExtensionConfiguration;

/**
 * An empty extension configuration implementation that does nothing.
 * @author Robert Müller
 */
public class NullExtensionConfiguration implements IExtensionConfiguration {

	@Override
	public void setOption(String key, Object value) {
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
	}

	@Override
	public void resetToDefaults() {
	}

	@Override
	public Collection<ConfigurationOption<?>> getConfigurationOptions() {
		return Collections.emptySet();
	}
}