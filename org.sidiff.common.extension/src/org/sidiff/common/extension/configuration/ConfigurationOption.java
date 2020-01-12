package org.sidiff.common.extension.configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sidiff.common.converter.ConverterUtil;
import org.sidiff.common.extension.ExtensionManager;
import org.sidiff.common.extension.IExtension;
import org.sidiff.common.util.RegExUtil;

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
	private final boolean multi;
	private final T minValue;
	private final T maxValue;
	private final List<T> defaultValues;
	private final Set<T> selectableValues;
	private final BiFunction<ConfigurationOption<T>,List<T>,Boolean> onSet;
	private final Function<? super T,String> valueLabelProvider;
	private List<T> values = new ArrayList<>();

	protected ConfigurationOption(String key, String name, Class<T> type, boolean multi,
			T minValue, T maxValue, List<T> defaultValues, Collection<? extends T> selectableValues,
			BiFunction<ConfigurationOption<T>,List<T>,Boolean> onSet,
			Function<? super T,String> valueLabelProvider) {

		this.key = key;
		this.name = name;
		this.type = type;
		this.multi = multi;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.defaultValues = new ArrayList<>(defaultValues);
		this.selectableValues = selectableValues == null ? null : new HashSet<>(selectableValues);
		this.onSet = onSet;
		this.valueLabelProvider = valueLabelProvider;
		resetToDefault();
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
	 * Returns whether this configuration options supports multiple values.
	 * 
	 * @return <code>true</code> if multiple values supported, <code>false</code> if single value
	 */
	public boolean isMulti() {
		return multi;
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
	public List<T> getDefault() {
		return Collections.unmodifiableList(defaultValues);
	}
	
	public Set<T> getSelectableValues() {
		return selectableValues == null ? null : Collections.unmodifiableSet(selectableValues);
	}

	public String getLabelForValue(T value) {
		if(value == null) {
			return "";
		}
		return valueLabelProvider.apply(value);
	}

	/**
	 * Sets the value of this option.
	 * For multi options, this sets the values to a singleton list, or an empty list if the value is <code>null</code>.
	 * @param value the new value, <code>null</code> to unset
	 */
	public void setValue(T value) {
		setValues(value == null ? Collections.emptyList() : Collections.singletonList(value));
	}

	/**
	 * Sets the values of this configuration option.
	 * Use the shorthand {@link #setValue(Object)} for single options.
	 * @param values the new values
	 */
	public void setValues(List<T> values) {
		if(values.size() > 1 && !multi) {
			throw new IllegalArgumentException("This configuration options does not support multiple values.");
		}
		values.forEach(this::validateValue);
		if(onSet.apply(this, values)) {
			this.values.clear();
			this.values.addAll(values);
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
	 * Returns the value of this single option.
	 * Call {@link #getValues()} for multi options.
	 * @return the current value of this option
	 */
	public T getValue() {
		if(multi) {
			throw new IllegalStateException("Use the ConfigurationOption.getValues() method for multi options");
		}
		return values.isEmpty() ? null : values.get(0);
	}
	
	/**
	 * Returns the values of this multi option.
	 * Call {@link #getValue()} for single options.
	 * @return the current value of this option
	 */
	public List<T> getValues() {
		if(!multi) {
			throw new IllegalStateException("Use the ConfigurationOption.getValue() method for non-multi options");
		}
		return Collections.unmodifiableList(values);
	}

	/**
	 * Returns whether this option currently has a non-null value.
	 * @return whether this option is set
	 */
	public boolean isSet() {
		return !values.isEmpty();
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
		} else if(value instanceof Collection<?>) {
			setValues(((Collection<?>)value).stream().map(this::getTypedValue).collect(Collectors.toList()));
		} else {
			setValue(getTypedValue(value));
		}
	}

	private T getTypedValue(Object value) {
		if(value == null) {
			throw new IllegalArgumentException("Values must not contain null-elements");
		} else if(getType().isInstance(value)) {
			return getType().cast(value);
		} else if(value instanceof String) {
			return ConverterUtil.unmarshalSafe(getType(), (String)value);
		} else {
			throw new ClassCastException("This value is incompatible with " + getType().getName() + ": " + value);
		}
	}

	/**
	 * Resets this option to its default value.
	 */
	public void resetToDefault() {
		values.clear();
		values.addAll(defaultValues);
	}

	@Override
	public String toString() {
		return "ConfigurationOption[" + getKey() + ":" + getType().getSimpleName() + " = " + values + "]";
	}

	String exportAssignment() {
		return getKey() + "=" + values.stream().map(this::getLabelForValue).collect(Collectors.joining(","));
	}

	void importAssignment(String valueString) {
		Set<String> values = Stream.of(valueString.split(",")).collect(Collectors.toSet());
		Set<T> selectable = getSelectableValues();
		if(selectable == null) {
			setValueUnsafe(values);
		} else {
			setValues(selectable.stream().filter(s -> values.contains(getLabelForValue(s))).collect(Collectors.toList()));
		}
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
	 * Returns a new Builder for configuration options.
	 * @param type the type of the option to be created, used to initialize default key and name
	 * @param extensionManager extension manager which provides the selectable values
	 * @return builder for this type which presets
	 */
	public static <T extends IExtension> Builder<T> builder(Class<T> type, ExtensionManager<? extends T> extensionManager) {
		return new Builder<T>(type)
			.key(typeToKey(type))
			.name(typeToName(type))
			.selectableValues(extensionManager.getSortedExtensions());
	}

	private static String typeToKey(Class<?> type) {
		String key = type.getSimpleName();
		if(key.startsWith("I")) {
			key = key.substring(1);
		}
		return Character.toLowerCase(key.charAt(0)) + key.substring(1);
	}

	private static String typeToName(Class<?> type) {
		String name = type.getSimpleName();
		if(name.startsWith("I")) {
			name = name.substring(1);
		}
		return RegExUtil.addSpacesToCamelCase(name);
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
		private boolean multi = false;
		private T minValue; // must extend Number
		private T maxValue; // must extend Number
		private List<T> defaultValues = new ArrayList<>();
		private Collection<? extends T> selectableValues;
		private BiFunction<ConfigurationOption<T>,List<T>,Boolean> onSet;
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

		public Builder<T> multi() {
			this.multi = true;
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
			return defaultValues(defaultValue == null ? Collections.emptyList() : Collections.singletonList(defaultValue));
		}

		public Builder<T> defaultValues(List<T> defaultValues) {
			if(defaultValues.size() > 1 && !multi) {
				throw new IllegalArgumentException("Must be a multi option if using multiple default values");
			}
			this.defaultValues.clear();
			this.defaultValues.addAll(defaultValues);
			return this;
		}

		/**
		 * <p>Sets a callback function which is called <i>before</i> a new value is set.
		 * The callback functions receives the ConfigurationOption and the new values
		 * as arguments and returns a boolean value, which indicates whether the new values
		 * should be applied (<code>true</code>) or discarded (<code>false</code>).</p>
		 * <p>The default callback just returns <code>true</code>.</p>
		 * @param onSet the function
		 * @return this builder for method chaining
		 */
		public Builder<T> onSet(BiFunction<ConfigurationOption<T>,List<T>,Boolean> onSet) {
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
			for(T defaultValue : defaultValues) {
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
			return new ConfigurationOption<T>(key, name, type, multi, minValue, maxValue,
					defaultValues, selectableValues, onSet, valueLabelProvider);
		}

		public Builder<T> selectableValues(Collection<? extends T> selectableValues) {
			this.selectableValues = Objects.requireNonNull(selectableValues);
			return this;
		}
	}
}
