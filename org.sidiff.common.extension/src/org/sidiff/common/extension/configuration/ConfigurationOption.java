package org.sidiff.common.extension.configuration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

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
	private final T minValue;
	private final T maxValue;
	private final T defaultValue;
	private final Set<T> selectableValues;
	private final BiFunction<ConfigurationOption<T>,T,Boolean> onSet;
	private final Function<? super T,String> valueLabelProvider;
	private T value;

	protected ConfigurationOption(String key, String name, Class<T> type,
			T minValue, T maxValue, T defaultValue, Collection<? extends T> selectableValues,
			BiFunction<ConfigurationOption<T>,T,Boolean> onSet,
			Function<? super T,String> valueLabelProvider) {

		this.key = key;
		this.name = name;
		this.type = type;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.defaultValue = defaultValue;
		this.selectableValues = selectableValues == null ? null : new HashSet<>(selectableValues);
		this.onSet = onSet;
		this.valueLabelProvider = valueLabelProvider;
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
	 * The minimum value of this option, <code>null</code> if none.
	 * @return minimum value, or <code>null</code>
	 */
	public T getMinValue() {
		return minValue;
	}

	/**
	 * The maximum value of this option, <code>null</code> if none.
	 * @return maximum value, or <code>null</code>
	 */
	public T getMaxValue() {
		return maxValue;
	}

	/**
	 * Returns the default value of this option, <code>null</code> if unset.
	 * @return the default value, or <code>null</code>
	 */
	public T getDefaultValue() {
		return defaultValue;
	}
	
	public Set<T> getSelectableValues() {
		return selectableValues == null ? null : Collections.unmodifiableSet(selectableValues);
	}

	public String getLabelForValue(T value) {
		return valueLabelProvider.apply(value);
	}

	/**
	 * Sets the value of this option.
	 * @param value the new value
	 */
	public void setValue(T value) {
		validateValue(value);
		if(onSet.apply(this, value)) {
			this.value = value;
		}
	}

	protected void validateValue(T value) {
		if(value instanceof Number) {
			if(minValue instanceof Number) {
				if(compareValues((Number)minValue, (Number)value).filter(c -> c > 0).isPresent()) {
					throw new IllegalArgumentException("Value is smaller than minimum: " + value + " < " + minValue);
				}
			}
			if(maxValue instanceof Number) {
				if(compareValues((Number)maxValue, (Number)value).filter(c -> c < 0).isPresent()) {
					throw new IllegalArgumentException("Value is larger than maximum: " + value + " > " + maxValue);
				}
			}
		}
	}

	protected static Optional<Integer> compareValues(Number lhs, Number rhs) {
		try {
			return Optional.of(new BigDecimal(lhs.toString()).compareTo(new BigDecimal(rhs.toString())));							
		} catch(NumberFormatException e) {
			return Optional.empty();
		}
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

	@Override
	public String toString() {
		return "ConfigurationOption[" + getKey() + ":" + getType().getSimpleName() + " = " + getValue() + "]";
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
		private T minValue; // must extend Number
		private T maxValue; // must extend Number
		private T defaultValue;
		private Collection<? extends T> selectableValues;
		private BiFunction<ConfigurationOption<T>,T,Boolean> onSet;
		private Function<? super T,String> valueLabelProvider;

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

		public Builder<T> minValue(T minValue) {
			if(!(minValue instanceof Number)) {
				throw new IllegalArgumentException("Minimum value only supported for subclasses of Number");
			}
			this.minValue = minValue;
			return this;
		}
		
		public Builder<T> maxValue(T maxValue) {
			if(!(maxValue instanceof Number)) {
				throw new IllegalArgumentException("Maximum value only supported for subclasses of Number");
			}
			this.maxValue = maxValue;
			return this;
		}
		
		public Builder<T> defaultValue(T defaultValue) {
			this.defaultValue = Objects.requireNonNull(defaultValue);
			return this;
		}

		/**
		 * <p>Sets a callback function which is called <i>before</i> a new value is set.
		 * The callback functions receives the ConfigurationOption and the new value
		 * as arguments and returns a boolean value, which indicates whether the new value
		 * should be applied (<code>true</code>) or discarded (<code>false</code>).</p>
		 * <p>The default callback just returns <code>true</code>.</p>
		 * @param onSet the function
		 * @return this builder for method chaining
		 */
		public Builder<T> onSet(BiFunction<ConfigurationOption<T>,T,Boolean> onSet) {
			this.onSet = Objects.requireNonNull(onSet);
			return this;
		}
		
		public Builder<T> valueLabelProvider(Function<? super T,String> valueLabelProvider) {
			this.valueLabelProvider = Objects.requireNonNull(valueLabelProvider);
			return this;
		}

		public ConfigurationOption<T> build() {
			if(key == null) {
				throw new IllegalStateException("ConfigurationOption requires a key");
			}
			if(minValue != null && maxValue != null) {
				if(compareValues((Number)minValue, (Number)maxValue).filter(c -> c > 0).isPresent()) {
					throw new IllegalStateException("Minimum value is greater than maximum value");
				}
			}
			if(defaultValue instanceof Number) {
				if(minValue != null) {
					if(compareValues((Number)defaultValue, (Number)minValue).filter(c -> c < 0).isPresent()) {
						throw new IllegalStateException("Default value is smaller than the minimum value");
					}
				}
				if(maxValue != null) {
					if(compareValues((Number)defaultValue, (Number)maxValue).filter(c -> c > 0).isPresent()) {
						throw new IllegalStateException("Default value is greater than the maximum value");
					}
				}
			}
			if(name == null) {
				name = key;
			}
			if(type.isEnum() && selectableValues == null) {
				selectableValues = Arrays.asList(type.getEnumConstants());
			}
			if(onSet == null) {
				onSet = (option, newValue) -> true;
			}
			if(valueLabelProvider == null) {
				valueLabelProvider = value -> value == null ? "No value" : value.toString();
			}
			return new ConfigurationOption<T>(key, name, type, minValue, maxValue,
					defaultValue, selectableValues, onSet, valueLabelProvider);
		}

		public Builder<T> selectableValues(Collection<? extends T> selectableValues) {
			this.selectableValues = Objects.requireNonNull(selectableValues);
			return this;
		}
	}
}
