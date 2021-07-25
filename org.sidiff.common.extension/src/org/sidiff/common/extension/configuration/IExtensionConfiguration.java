package org.sidiff.common.extension.configuration;

import java.util.Collection;
import java.util.Map;

import org.sidiff.common.extension.internal.NullExtensionConfiguration;

import com.eclipsesource.json.JsonObject;

/**
 * <p>An extension configuration stores multiple configuration
 * options, identified by keys, that each have a name, value and
 * default value.</p>
 * <p>{@link BasicExtensionConfiguration} is a basic implementation
 * of an extension configuration.</p>
 * @author rmueller
 */
public interface IExtensionConfiguration {

	/**
	 * A singleton empty configuration without options.
	 * Trying to modify it will result in runtime exceptions.
	 */
	IExtensionConfiguration NULL = new NullExtensionConfiguration();

	/**
	 * <p>Sets the configuration option with the given key to the given value.</p>
	 * <p>The option with this key must exist and the value's type must
	 * match the option's type or be convertible to it.</p>
	 * @param key the option's key
	 * @param value the new value
	 */
	void setOption(String key, Object value);

	/**
	 * <p>Returns the value of the configuration option with the given key.</p>
	 * <p>The option with this key must exist.</p>
	 * @param key the option's key
	 * @return the option's current value
	 */
	Object getOption(String key);

	/**
	 * Returns a map of all options' keys to their values.
	 * @return option key to value map
	 */
	Map<String,Object> getOptions();

	/**
	 * Sets multiple options to the given key to value mappings.
	 * @param options the key to value map of options to set
	 */
	void setOptions(Map<String,Object> options);

	/**
	 * Resets all options to their default values.
	 */
	void resetToDefaults();

	/**
	 * Returns all configuration options of this configuration.
	 * @return collection of all configuration options
	 */
	Collection<ConfigurationOption<?>> getConfigurationOptions();

	/**
	 * Filters the selectable values for all configuration options based on the given arguments.
	 * Only applies to configuration options that support it.
	 * @param documentTypes the document types, empty to allow all
	 * @param includeGeneric whether to include generic extensions as well
	 */
	void setDocumentTypeFilter(Collection<String> documentTypes, boolean includeGeneric);

	interface Internal {

		/**
		 * Exports the configuration option assignments to a serializable format.
		 * @return serialized option assignments
		 */
		JsonObject exportAssignments();

		/**
		 * Imports configuration options assignments, which have been previously
		 * exported with {@link #exportAssignments()}.
		 * @param serializedValue the serialized value to import
		 */
		void importAssignments(JsonObject serializedValue);

		/**
		 * Legacy. Imports configuration options assignments, which have been previously
		 * exported with {@link #exportAssignments()}.
		 * @param serializedValue the serialized value to import
		 */
		void importAssignments(String serializedValue);
	}
}
