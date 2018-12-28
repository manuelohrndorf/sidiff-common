package org.sidiff.common.extension.configuration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A basic implementation of {@link IExtensionConfiguration}
 * that uses a fixed collection of {@link ConfigurationOption}s.
 * @author Robert Müller
 */
public class BasicExtensionConfiguration extends AbstractExtensionConfiguration {

	private final Map<String,ConfigurationOption<?>> optionsMap;

	/**
	 * Creates a new basic configuration using the given configuration options.
	 * @param options the configuration options
	 */
	public BasicExtensionConfiguration(Collection<ConfigurationOption<?>> options) {
		this.optionsMap = options.stream()
				.collect(Collectors.toMap(ConfigurationOption::getKey, Function.identity(),
					(v1,v2) -> { throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2)); },
                    LinkedHashMap::new));
	}

	@Override
	public void setOption(String key, Object value) {
		optionsMap.get(key).setValueUnsafe(value);
	}

	@Override
	public Object getOption(String key) {
		return optionsMap.get(key).getValue();
	}

	@Override
	public Collection<ConfigurationOption<?>> getConfigurationOptions() {
		return optionsMap.values();
	}
}
