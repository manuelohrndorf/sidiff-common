package org.sidiff.common.converter;

import java.util.*;

import org.sidiff.common.converter.internal.*;

public class ConverterUtil {

	private static Map<Class<?>, ObjectConverter<?>> converters = new HashMap<>();

	static {
		registerConverter(new BooleanConverter());
		registerConverter(new ByteConverter());
		registerConverter(new ShortConverter());
		registerConverter(new IntegerConverter());
		registerConverter(new LongConverter());
		registerConverter(new FloatConverter());
		registerConverter(new DoubleConverter());
		registerConverter(new CharacterConverter());
		registerConverter(new StringConverter());
	}

	public static <T> void registerConverter(ObjectConverter<T> converter) {
		converters.put(converter.getType(), converter);
	}

	public static <T> void unregisterConverter(Class<T> type) {
		converters.remove(type);
	}

	@SuppressWarnings("unchecked")
	public static <T> String marshal(T object) {
		return getConverter((Class<T>)object.getClass())
				.map(c -> c.marshal(object))
				.orElseThrow(() -> new IllegalArgumentException("No converter found for type " + object.getClass().getName()));
	}

	public static <T> T unmarshal(Class<T> type, String string) {
		return getConverter(type)
				.map(c -> c.unmarshal(string))
				.orElseThrow(() -> new IllegalArgumentException("No converter found for type " + type.getName()));
	}

	public static <T> T unmarshalSafe(Class<T> type, String string) {
		return getConverter(type).map(c -> {
				try {
					return c.unmarshal(string);
				} catch(IllegalArgumentException e) {
					return c.getDefaultValue();
				}
			}).orElseThrow(() -> new IllegalArgumentException("No converter found for type " + type.getName()));
	}

	@SuppressWarnings("unchecked")
	private static <T> Optional<ObjectConverter<T>> getConverter(Class<T> type) {
		return Optional.ofNullable((ObjectConverter<T>)converters.get(type));
	}
}
