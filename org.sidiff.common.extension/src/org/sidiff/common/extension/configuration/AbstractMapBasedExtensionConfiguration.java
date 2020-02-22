package org.sidiff.common.extension.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.sidiff.common.util.StringListSerializer;

/**
 * An abstract extension configuration that uses a map to store options.
 * @author rmueller
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
		getOptionOrThrow(key).setValueUnsafe(value);
	}

	@Override
	public Object getOption(String key) {
		return getOptionOrThrow(key).getValue();
	}

	@Override
	public Collection<ConfigurationOption<?>> getConfigurationOptions() {
		return Collections.unmodifiableCollection(getOptionsMap().values());
	}

	@Override
	public String exportAssignments() {
		return StringListSerializer.DEFAULT.serialize(
			getConfigurationOptions().stream()
				.map(ConfigurationOption::exportAssignment)
				.collect(Collectors.toList()));
	}

	@Override
	public void importAssignments(String serializedValue) {
		Collection<ConfigurationOption<?>> options = getConfigurationOptions();
		for(String assignment : StringListSerializer.DEFAULT.deserialize(serializedValue)) {
			if(assignment.isEmpty()) {
				continue;
			}
			List<String> keyValue = ConfigurationOption.EQUAL_SIGN_SERIALIZER.deserialize(assignment);
			if(keyValue.size() > 2) {
				throw new IllegalArgumentException(
					"Only one equals sign in serialized configuration option entry expected. Assignment: " + assignment);
			}
			options.stream()
				.filter(option -> option.getKey().equals(keyValue.get(0)))
				.findFirst().ifPresent(option -> option.importAssignment(keyValue.size() > 1 ? keyValue.get(1) : ""));
		}
	}

	private ConfigurationOption<?> getOptionOrThrow(String key) {
		ConfigurationOption<?> option = getOptionsMap().get(key);
		if(option == null) {
			throw new IllegalArgumentException("No option with key '" + key + "' exists in " + this);
		}
		return option;
	}
}
