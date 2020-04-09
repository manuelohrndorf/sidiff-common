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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.sidiff.common.converter.ConverterUtil;
import org.sidiff.common.extension.ExtensionManager;
import org.sidiff.common.extension.ExtensionManagerFinder;
import org.sidiff.common.extension.ExtensionSerialization;
import org.sidiff.common.extension.IExtension;
import org.sidiff.common.extension.ITypedExtension;
import org.sidiff.common.extension.TypedExtensionManager;
import org.sidiff.common.util.RegExUtil;
import org.sidiff.common.util.StringListSerializer;

/**
 * <p>A configuration option is a single option of a {@link IExtensionConfiguration},
 * with its key, name, type, default value, and current value.</p>
 * <p>Configuration options are instantiated using {@link #builder(Class) ConfigurationOption.builder(Class)}.</p>
 * @author rmueller
 * @param <T> the type of the option
 */
public class ConfigurationOption<T> {

	static final StringListSerializer EQUAL_SIGN_SERIALIZER = new StringListSerializer("=");
	static final StringListSerializer COMMA_SIGN_SERIALIZER = new StringListSerializer(",");

	private final String key;
	private final String name;
	private final Class<T> type;
	private final boolean multi;
	private final T minValue;
	private final T maxValue;
	private final List<T> defaultValues;
	private final Set<T> selectableValues;
	private final Function<? super T,String> valueLabelProvider;

	private final List<T> values = new ArrayList<>();
	private final Set<String> documentTypes = new HashSet<>();
	private boolean includeGeneric = true;

	protected ConfigurationOption(String key, String name, Class<T> type, boolean multi,
			T minValue, T maxValue, List<T> defaultValues, Collection<? extends T> selectableValues,
			Function<? super T,String> valueLabelProvider) {

		this.key = key;
		this.name = name;
		this.type = type;
		this.multi = multi;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.defaultValues = new ArrayList<>(defaultValues);
		this.selectableValues = selectableValues == null ? null : new HashSet<>(selectableValues);
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
	 * Returns the default values of this option
	 * @return the default values, may be empty
	 */
	public List<T> getDefault() {
		return Collections.unmodifiableList(defaultValues);
	}

	public Set<T> getSelectableValues() {
		if(selectableValues == null) {
			return null;
		}
		if(ITypedExtension.class.isAssignableFrom(type)) {
			return Collections.unmodifiableSet(
				selectableValues.stream()
					.map(ITypedExtension.class::cast)
					.filter(ext -> (includeGeneric && ext.isGeneric()) || ext.getDocumentTypes().containsAll(documentTypes))
					.map(type::cast)
					.collect(Collectors.toSet()));
		}
		return Collections.unmodifiableSet(selectableValues);
	}

	public String getLabelForValue(T value) {
		if(value == null) {
			return "";
		}
		return valueLabelProvider.apply(value);
	}

	protected String getSerializableValue(T value) {
		if(value instanceof IExtension) {
			return ExtensionSerialization.convertToString((IExtension)value);
		}
		return getLabelForValue(value);
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
		this.values.clear();
		this.values.addAll(values);

		// For configurable extensions we must replace the selectable values with equal selected values,
		// or else the nested configuration options work on unused extension instances.
		List<IConfigurableExtension> configurableValues =
			values.stream()
				.filter(IConfigurableExtension.class::isInstance)
				.map(IConfigurableExtension.class::cast)
				.collect(Collectors.toList());
		if(!configurableValues.isEmpty()) {
			// Remove selectable extensions which have same key but not same instance
			selectableValues.removeIf(
				selectable -> selectable instanceof IConfigurableExtension
				&& configurableValues.stream()
					.anyMatch(configurable -> selectable != configurable
							&& ((IExtension)selectable).getKey().equals(configurable.getKey())));
			// Add the active instances of configurable extensions to selectable
			configurableValues.stream()
				.filter(configurable -> selectableValues.stream()
					.noneMatch(selectable -> selectable instanceof IExtension
							&& configurable.getKey().equals(((IExtension)selectable).getKey())))
				.filter(type::isInstance)
				.map(type::cast)
				.forEach(selectableValues::add);
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
	 * @return the current value of this option, <code>null</code> if none
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
	 * @return the current values of this option, may be empty
	 */
	public List<T> getValues() {
		if(!multi) {
			throw new IllegalStateException("Use the ConfigurationOption.getValue() method for non-multi options");
		}
		return Collections.unmodifiableList(values);
	}

	/**
	 * Returns whether this option currently has a non-null/non-empty value.
	 * @return <code>true</code> if any value present, <code>false</code> otherwise
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
		} else if(type.isInstance(value)) {
			return type.cast(value);
		} else if(value instanceof String) {
			if(IExtension.class.isAssignableFrom(type)) {
				ExtensionManager<? extends T> manager = ExtensionManagerFinder.findManager(type);
				if(manager == null) {
					throw new IllegalStateException("The type is IExtension but no ExtensionManager was found");
				}
				T ext = ExtensionSerialization.createExtension(manager, (String)value);
				if(selectableValues != null) {
					List<T> selectable = new ArrayList<>(selectableValues);
					selectable.replaceAll(s -> s instanceof IExtension
							&& ((IExtension)s).getKey().equals(((IExtension)ext).getKey()) ? ext : s);
					selectableValues.clear();
					selectableValues.addAll(selectable);
				}
				return ext;
			}
			if(selectableValues != null) {
				T matchingSelectable = selectableValues.stream()
						.filter(selectable -> getLabelForValue(selectable).equals(value)).findFirst().orElse(null);
				if(matchingSelectable != null) {
					return matchingSelectable;
				}
			}
			return ConverterUtil.unmarshalSafe(type, (String)value);
		} else {
			throw new ClassCastException("This value is incompatible with " + type.getName() + ": " + value);
		}
	}

	/**
	 * Resets this option to its default value.
	 */
	public void resetToDefault() {
		values.clear();
		values.addAll(defaultValues);
	}

	/**
	 * Filters the selectable values for the configuration option based on the given arguments.
	 * @param documentTypes the document types, empty to allow all
	 * @param includeGeneric whether to include generic extensions as well
	 */
	public void setDocumentTypeFilter(Collection<String> documentTypes, boolean includeGeneric) {
		this.documentTypes.clear();
		this.documentTypes.addAll(documentTypes);
		this.includeGeneric = includeGeneric;
		if(selectableValues != null) {
			for(T selectable : selectableValues) {
				if(selectable instanceof IConfigurableExtension) {
					((IConfigurableExtension)selectable).getConfiguration().setDocumentTypeFilter(documentTypes, includeGeneric);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "ConfigurationOption[" + getKey() + ":" + getType().getSimpleName() + " = " + values + "]";
	}

	String exportAssignment() {
		return EQUAL_SIGN_SERIALIZER.serialize(Arrays.asList(
				getKey(),
				COMMA_SIGN_SERIALIZER.serialize(values.stream().map(this::getSerializableValue).collect(Collectors.toList()))));
	}

	void importAssignment(String valueString) {
		setValueUnsafe(COMMA_SIGN_SERIALIZER.deserialize(valueString));
	}

	/**
	 * Returns a new Builder for configuration options.
	 * @param type the type of the option to be created
	 * @return builder for this type
	 */
	public static <T> Builder<T> builder(Class<T> type) {
		return new Builder<>(type);
	}

	/**
	 * Returns a new Builder for configuration options for an extension manager.
	 * @param type the type of the option to be created, used to initialize default key and name
	 * @param extensionManager extension manager which provides the selectable values (all available extensions)
	 * @return builder for this type which presets
	 */
	public static <T extends IExtension> Builder<T> builder(Class<T> type, ExtensionManager<? extends T> extensionManager) {
		return new Builder<>(type)
			.key(typeToKey(type))
			.name(typeToName(type))
			.valueLabelProvider(IExtension::getName)
			.selectableValues(extensionManager.getSortedExtensions());
	}

	/**
	 * Returns a new Builder for configuration options for a typed extension manager.
	 * @param type the type of the option to be created, used to initialize default key and name
	 * @param extensionManager typed extension manager which provides the selectable values (filtered by document type)
	 * @param documentTypes document types which must be supported by selectable values
	 * @param includeGeneric whether generic extensions are selectable values
	 * @return builder for this type which presets
	 */
	public static <T extends ITypedExtension> Builder<T> builder(Class<T> type,
			TypedExtensionManager<? extends T> extensionManager, Collection<String> documentTypes, boolean includeGeneric) {
		return new Builder<>(type)
			.key(typeToKey(type))
			.name(typeToName(type))
			.valueLabelProvider(IExtension::getName)
			.selectableValues(extensionManager.getExtensions(documentTypes, includeGeneric));
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
	 * @author rmueller
	 * @param <T> the type of configuration option this builder creates
	 */
	public static class Builder<T> {

		private static final Function<Object, String> DEFAULT_LABEL_PROVIDER =
				value -> value == null ? "No value" : value.toString();

		private final Class<T> type;
		private String key;
		private String name;
		private boolean multi = false;
		private T minValue; // must extend Number
		private T maxValue; // must extend Number
		private List<T> defaultValues = new ArrayList<>();
		private Collection<? extends T> selectableValues;
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

		/**
		 * Call iff this configuration option supports multiple values.
		 * Must be called before all other builders methods, directly after creating it.
		 * @return this builder, for method chaining
		 */
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

		public Builder<T> defaultValuesAll() {
			Assert.isLegal(selectableValues != null, "Must call 'selectableValues' before 'defaultValuesAll'");
			return defaultValues(new ArrayList<>(selectableValues));
		}

		public Builder<T> valueLabelProvider(Function<? super T,String> valueLabelProvider) {
			this.valueLabelProvider = Objects.requireNonNull(valueLabelProvider);
			return this;
		}

		public Builder<T> selectableValues(Collection<? extends T> selectableValues) {
			this.selectableValues = Objects.requireNonNull(selectableValues);
			return this;
		}

		public ConfigurationOption<T> build() {
			if(key == null) {
				throw new IllegalStateException("ConfigurationOption requires a key");
			}
			// Ensure minValue < maxValue
			if(minValue != null && maxValue != null) {
				if(compareValues((Number)minValue, (Number)maxValue).filter(c -> c > 0).isPresent()) {
					throw new IllegalStateException("Minimum value is greater than maximum value");
				}
			}
			// Ensure minValue <= each default value <= maxValue
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
			// Default name is the key
			if(name == null) {
				name = key;
			}
			// Default selectable values for Enum classes
			if(type.isEnum() && selectableValues == null) {
				selectableValues = Arrays.asList(type.getEnumConstants());
			}
			if(valueLabelProvider == null) {
				valueLabelProvider = DEFAULT_LABEL_PROVIDER;
			}
			return new ConfigurationOption<>(key, name, type, multi, minValue, maxValue,
					defaultValues, selectableValues, valueLabelProvider);
		}
	}
}
