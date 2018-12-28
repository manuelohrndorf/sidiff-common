package org.sidiff.common.extension.configuration;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * An abstract extension configuration which implements some
 * methods which are likely to be equal in a subclasses.
 * @author Robert Müller
 */
public abstract class AbstractExtensionConfiguration implements IExtensionConfiguration {

	@Override
	public Map<String, Object> getOptions() {
		return getConfigurationOptions().stream()
				.collect(Collectors.toMap(ConfigurationOption::getKey, ConfigurationOption::getValue));
	}

	@Override
	public void setOptions(Map<String, Object> options) {
		options.entrySet().forEach(e -> setOption(e.getKey(), e.getValue()));
	}

	@Override
	public void resetToDefaults() {
		getConfigurationOptions().forEach(ConfigurationOption::resetToDefault);
	}
}
