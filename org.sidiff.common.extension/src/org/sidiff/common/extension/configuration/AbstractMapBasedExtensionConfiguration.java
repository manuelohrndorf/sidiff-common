package org.sidiff.common.extension.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An abstract extension configuration that uses a map to store options.
 * @author Robert Müller
 */
public abstract class AbstractMapBasedExtensionConfiguration extends AbstractExtensionConfiguration {

	private Map<String,ConfigurationOption<?>> optionsMap;

	private Map<String, ConfigurationOption<?>> getOptionsMap() {
		if(optionsMap == null) {
			optionsMap = new LinkedHashMap<>();
			getOptionsFactory().accept(option -> optionsMap.put(option.getKey(), option));
		}
		return optionsMap;
	}

	protected abstract Consumer<Consumer<ConfigurationOption<?>>> getOptionsFactory();

	@Override
	public void setOption(String key, Object value) {
		ConfigurationOption<?> option = getOptionsMap().get(key);
		if(option == null) {
			throw new IllegalArgumentException("No option with key '" + key + "' exists in " + this);
		}
		option.setValueUnsafe(value);
	}

	@Override
	public Object getOption(String key) {
		ConfigurationOption<?> option = getOptionsMap().get(key);
		if(option == null) {
			throw new IllegalArgumentException("No option with key '" + key + "' exists in " + this);
		}
		return option.getValue();
	}

	@Override
	public Collection<ConfigurationOption<?>> getConfigurationOptions() {
		return Collections.unmodifiableCollection(getOptionsMap().values());
	}
}
