package org.sidiff.common.extension.configuration;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.converter.ConverterUtil;
import org.sidiff.common.extension.*;
import org.sidiff.common.util.RegExUtil;
import org.sidiff.common.util.StringListSerializer;

import com.eclipsesource.json.*;

/**
 * <p>A configuration option is an element of an {@link IExtensionConfiguration}
 * and store the value of a one option of an {@link IConfigurableExtension}.</p>
 * <p>Options can either be single (0-1) or multi (0-N) valued. Options always have a type, key and name.</p>
 * <p>Configuration options are instantiated using the <code>ConfigurationOption.builder(...)</code> factory methods.</p>
 * @author rmueller
 * @param <T> the type of the option
 * @see #builder(Class)
 * @see #builder(Class, ExtensionManager)
 * @see #builder(Class, TypedExtensionManager, Collection, boolean)
 */
public class ConfigurationOption<T> {

	static final StringListSerializer COMMA_SIGN_SERIALIZER = new StringListSerializer(",");

	private final String key;
	private final String name;
	private final Optional<String> description;
	private final Class<T> type;
	private final boolean multi;
	private final Optional<T> minValue;
	private final Optional<T> maxValue;
	private final List<T> defaultValues;
	private final Set<T> selectableValues;
	private final Function<? super T,String> valueLabelProvider;

	private final List<T> values = new ArrayList<>();
	private final Set<String> documentTypes = new HashSet<>();
	private boolean includeGeneric = true;

	protected ConfigurationOption(String key, String name, String description, Class<T> type, boolean multi,
			T minValue, T maxValue, List<T> defaultValues, Collection<? extends T> selectableValues,
			Function<? super T,String> valueLabelProvider) {

		this.key = key;
		this.name = name;
		this.description = Optional.ofNullable(description);
		this.type = type;
		this.multi = multi;
		this.minValue = Optional.ofNullable(minValue);
		this.maxValue = Optional.ofNullable(maxValue);
		this.defaultValues = new ArrayList<>(defaultValues);
		this.selectableValues = selectableValues == null ? null : new HashSet<>(selectableValues);
		this.valueLabelProvider = nullSafeLabelProvider(valueLabelProvider);
		resetToDefault();
	}

	private Function<? super T, String> nullSafeLabelProvider(Function<? super T, String> delegate) {
		return value -> value == null ? "" : delegate.apply(value);
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
	 * Returns a readable description for this option.
	 * @return option's readable description, or empty if none
	 */
	public Optional<String> getDescription() {
		return description;
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
	 * @return <code>true</code> if multiple values supported, <code>false</code> if single value
	 */
	public boolean isMulti() {
		return multi;
	}

	/**
	 * The minimum value of this option. If present, this is a {@link Number}.
	 * @return minimum value, or empty if none
	 */
	public Optional<T> getMinValue() {
		return minValue;
	}

	/**
	 * The maximum value of this option. If present, this is a {@link Number}.
	 * @return maximum value, or empty if none
	 */
	public Optional<T> getMaxValue() {
		return maxValue;
	}

	/**
	 * Returns the default values of this option.
	 * @return list of default values, may be empty
	 */
	public List<T> getDefault() {
		return Collections.unmodifiableList(defaultValues);
	}

	/**
	 * Returns a set of all values which can be selected for this option.
	 * The set is potentially filtered by {@link #setDocumentTypeFilter(Collection, boolean)}.
	 * Returns <code>null</code> if selectable values cannot be enumerated.
	 * @return unmodifiable set of selectable values, or <code>null</code> if unspecified
	 */
	public Set<T> getSelectableValues() {
		if(selectableValues == null) {
			return null;
		}
		if(ITypedExtension.class.isAssignableFrom(type)) {
			return Collections.unmodifiableSet(
				selectableValues.stream()
					.map(ITypedExtension.class::cast)
					.filter(ext -> includeGeneric && ext.isGeneric() || ext.getDocumentTypes().containsAll(documentTypes))
					.map(type::cast)
					.collect(Collectors.toSet()));
		}
		return Collections.unmodifiableSet(selectableValues);
	}

	/**
	 * Returns a unique and readable string value for the given option value.
	 * @param value the option value
	 * @return string value
	 */
	public String getLabel(T value) {
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
			if(minValue.filter(Number.class::isInstance).map(Number.class::cast)
					.flatMap(min -> compareNumbers(min, (Number)value)).filter(c -> c > 0).isPresent()) {
				throw new IllegalArgumentException("Value is smaller than minimum: " + value + " < " + minValue.get());
			}
			if(maxValue.filter(Number.class::isInstance).map(Number.class::cast)
					.flatMap(max -> compareNumbers(max, (Number)value)).filter(c -> c < 0).isPresent()) {
				throw new IllegalArgumentException("Value is larger than maximum: " + value + " > " + maxValue.get());
			}
		}
	}

	protected static Optional<Integer> compareNumbers(Number lhs, Number rhs) {
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
			throw new IllegalStateException("Use the ConfigurationOption.getValue() method for single options");
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
						.filter(selectable -> getLabel(selectable).equals(value))
						.findFirst().orElse(null);
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

	void exportAssignment(JsonObject result) {
		JsonValue value;
		if(isMulti()) {
			JsonArray array = Json.array();
			getValues().stream().map(this::getSerializableValue).forEach(array::add);
			value = array;
		} else if(isSet()) {
			value = getSerializableValue(getValue());
		} else {
			value = Json.NULL;
		}
		result.add(getKey(), value);
	}

	private JsonValue getSerializableValue(T value) {
		if(value == null) {
			return Json.NULL;
		} else if(value instanceof IExtension) {
			String extensionValue = ExtensionSerialization.convertToString((IExtension)value);
			return extensionValue.isEmpty() ? Json.NULL : Json.parse(extensionValue);
		}
		return Json.value(getLabel(value));
	}

	void importAssignment(JsonValue jsonValue) {
		if(jsonValue.isNull()) {
			setValueUnsafe(null);
		} else if(jsonValue.isArray()) {
			setValueUnsafe(CollectionUtil.asStream(jsonValue.asArray().iterator()).collect(Collectors.toList()));
		} else if(jsonValue.isString()) {
			setValueUnsafe(jsonValue.asString());
		} else {
			setValueUnsafe(jsonValue.toString());
		}
	}

	// Legacy import
	void importAssignment(String valueString) {
		if (valueString.isEmpty() && type == String.class && !multi) {
			// If this is a ConfigurationOption<String> and the value is empty,
			// unpacking it with the StringListSerializer will yield an empty list,
			// which would set the value of a single option to null, meaning unset.
			// There is no difference between unset and empty string in the serialized
			// form, but assuming empty string here makes other code cleaner.
			setValueUnsafe("");
		} else {
			setValueUnsafe(COMMA_SIGN_SERIALIZER.deserialize(valueString));
		}
	}


	/**
	 * Returns a new Builder for configuration options.
	 * Key and Name are initialized to defaults using given type.
	 * The selectable values for enums are set to all enum constants per default.
	 * @param type the type of the option to be created, used to initialize default key and name
	 * @return builder for this type
	 */
	public static <T> Builder<T> builder(Class<T> type) {
		return applyEnumDefaults(applyDefaults(new Builder<>(type)));
	}

	/**
	 * Returns a new Builder for configuration options for an extension manager.
	 * @param type the type of the option to be created, used to initialize default key and name
	 * @param extensionManager extension manager which provides the selectable values (all available extensions)
	 * @return builder for this type which presets
	 */
	public static <T extends IExtension> Builder<T> builder(Class<T> type, ExtensionManager<? extends T> extensionManager) {
		return applyDefaults(new Builder<>(type))
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
		return applyDefaults(new Builder<>(type))
			.valueLabelProvider(IExtension::getName)
			.selectableValues(extensionManager.getExtensions(documentTypes, includeGeneric));
	}

	private static <T> Builder<T> applyDefaults(Builder<T> builder) {
		return builder
			.key(typeToKey(builder.type))
			.name(typeToName(builder.type));
	}

	private static <T> Builder<T> applyEnumDefaults(Builder<T> builder) {
		if (builder.type.isEnum()) {
			return builder.selectableValues(Arrays.asList(builder.type.getEnumConstants()));
		}
		return builder;
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
	 * Use one of the factory methods of {@link ConfigurationOption} to create a builder.
	 * @author rmueller
	 * @param <T> the type of configuration option this builder creates
	 */
	public static class Builder<T> {

		private static final Function<Object, String> DEFAULT_LABEL_PROVIDER =
				value -> value == null ? "No value" : value.toString();

		private final Class<T> type;
		private String key;
		private String name;
		private String description;
		private boolean multi = false;
		private T minValue; // must extend Number
		private T maxValue; // must extend Number
		private List<T> defaultValues = new ArrayList<>();
		private Collection<? extends T> selectableValues;
		private Function<? super T,String> valueLabelProvider;

		protected Builder(Class<T> type) {
			this.type = Objects.requireNonNull(type, "Type must not be null");
		}

		public Builder<T> key(String key) {
			this.key = Objects.requireNonNull(key, "Key must not be null");
			return this;
		}

		public Builder<T> name(String name) {
			this.name = Objects.requireNonNull(name, "Name must not be null");
			return this;
		}

		/**
		 * Sets the description of the configuration option, which is used as tooltip text.
		 * The description is optional.
		 * @param description the description
		 * @return this builder, for method chaining
		 */
		public Builder<T> description(String description) {
			this.description = Objects.requireNonNull(description, "Description must not be null");
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

		/**
		 * Uses the first selectable value as a default value.
		 * @return this builder
		 */
		public Builder<T> defaultValueAny() {
			Assert.isLegal(selectableValues != null, "Must call 'selectableValues' before 'defaultValuesAny'");
			return defaultValues(selectableValues.isEmpty()
				? Collections.emptyList()
				: Collections.singletonList(selectableValues.iterator().next()));
		}

		public Builder<T> defaultValues(List<T> defaultValues) {
			Assert.isLegal(defaultValues.size() <= 1 || multi, "Must be a multi option if using multiple default values");
			this.defaultValues.clear();
			this.defaultValues.addAll(defaultValues);
			return this;
		}

		/**
		 * For multi-options, this sets the default values to all selectable values.
		 * Not applicable to single-options.
		 * @return this builder
		 */
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
			// Ensure minValue <= maxValue
			if(minValue != null && maxValue != null) {
				if(compareNumbers((Number)minValue, (Number)maxValue).filter(c -> c > 0).isPresent()) {
					throw new IllegalStateException("Minimum value is greater than maximum value");
				}
			}
			// Ensure minValue <= each default value <= maxValue
			for(T defaultValue : defaultValues) {
				if(defaultValue instanceof Number) {
					if(minValue != null) {
						if(compareNumbers((Number)defaultValue, (Number)minValue).filter(c -> c < 0).isPresent()) {
							throw new IllegalStateException("Default value is smaller than the minimum value");
						}
					}
					if(maxValue != null) {
						if(compareNumbers((Number)defaultValue, (Number)maxValue).filter(c -> c > 0).isPresent()) {
							throw new IllegalStateException("Default value is greater than the maximum value");
						}
					}
				}
			}
			// Default name is the key
			if(name == null) {
				name = key;
			}
			if(valueLabelProvider == null) {
				valueLabelProvider = DEFAULT_LABEL_PROVIDER;
			}
			return new ConfigurationOption<>(key, name, description, type, multi, minValue, maxValue,
					defaultValues, selectableValues, valueLabelProvider);
		}
	}
}
