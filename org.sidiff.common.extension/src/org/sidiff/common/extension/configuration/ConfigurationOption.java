package org.sidiff.common.extension.configuration;

import java.util.Objects;

import org.sidiff.common.converter.ConverterUtil;

/**
 * <p>A configuration option is a single option of a {@link IExtensionConfiguration},
 * with its key, name, type, default value, and current value.</p>
 * <p>Configuration options are instantiated using {@link #builder(Class) ConfigurationOption.builder(Class)}.</p>
 * @author Robert Müller
 * @param <T> the type of the option
 */
public class ConfigurationOption<T> {

	private final String key;
	private final String name;
	private final Class<T> type;
	private final T defaultValue;
	private T value;

	protected ConfigurationOption(String key, String name, Class<T> type, T defaultValue) {
		this.key = key;
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	/**
	 * Returns the unique key of this option.
	 * @return option's key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Returns the readable name of this option.
	 * @return option's readable name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the type of this option.
	 * @return option's type
	 */
	public Class<T> getType() {
		return type;
	}

	/**
	 * Returns the default value of this option.
	 * @return the default value
	 */
	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Sets the value of this option.
	 * @param value the new value
	 */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * Returns the value of this option.
	 * @return the current value of this option
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Returns whether this option currently has a non-null value.
	 * @return whether this option is set
	 */
	public boolean isSet() {
		return value != null;
	}

	/**
	 * Sets the value of this option. If the value is <code>null</code>
	 * or this options type, it is set directly. If the value is a String,
	 * it is converted to a primitive type if possible.
	 * @param value the value, unsafe type
	 */
	public void setValueUnsafe(Object value) {
		if(value == null) {
			setValue(null);
		} else if(getType().isInstance(value)) {
			setValue(getType().cast(value));
		} else if(value instanceof String) {
			setValue(ConverterUtil.unmarshalSafe(getType(), (String)value));
		} else {
			throw new ClassCastException("This value is incompatible with " + getType().getName() + ": " + value);
		}
	}

	/**
	 * Resets this option to its default value.
	 */
	public void resetToDefault() {
		value = defaultValue;
	}

	/**
	 * Returns a new Builder for configuration options.
	 * @param type the type of the option to be created
	 * @return builder for this type
	 */
	public static <T> Builder<T> builder(Class<T> type) {
		return new Builder<T>(type);
	}

	/**
	 * The builder is used to create configuration options.
	 * @author Robert Müller
	 * @param <T> the type of configuration option this builder creates
	 */
	public static class Builder<T> {

		private final Class<T> type;
		private String key;
		private String name;
		private T defaultValue;

		protected Builder(Class<T> type) {
			this.type = Objects.requireNonNull(type);
		}

		public Builder<T> key(String key) {
			this.key = Objects.requireNonNull(key);
			return this;
		}

		public Builder<T> name(String name) {
			this.name = Objects.requireNonNull(name);
			return this;
		}

		public Builder<T> defaultValue(T defaultValue) {
			this.defaultValue = Objects.requireNonNull(defaultValue);
			return this;
		}

		public ConfigurationOption<T> build() {
			if(key == null) {
				throw new IllegalStateException("ConfigurationOption requires a key");
			}
			if(name == null) {
				name = key;
			}
			return new ConfigurationOption<T>(key, name, type, defaultValue);
		}
	}
}
