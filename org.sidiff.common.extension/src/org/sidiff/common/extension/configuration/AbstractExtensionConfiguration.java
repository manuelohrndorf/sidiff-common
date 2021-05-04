package org.sidiff.common.extension.configuration;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An abstract extension configuration which implements some
 * methods which are likely to be equal in all subclasses.
 * @author rmueller
 */
public abstract class AbstractExtensionConfiguration implements IExtensionConfiguration, IExtensionConfiguration.Internal {

	@Override
	public Map<String, Object> getOptions() {
		return getConfigurationOptions().stream()
				.collect(Collectors.toMap(ConfigurationOption::getKey, ConfigurationOption::getValue));
	}

	@Override
	public void setOptions(Map<String, Object> options) {
		options.forEach(this::setOption);
	}

	@Override
	public void resetToDefaults() {
		getConfigurationOptions().forEach(ConfigurationOption::resetToDefault);
	}

	@Override
	public void setDocumentTypeFilter(Collection<String> documentTypes, boolean includeGeneric) {
		getConfigurationOptions().forEach(option -> option.setDocumentTypeFilter(documentTypes, includeGeneric));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + getConfigurationOptions();
	}
}
