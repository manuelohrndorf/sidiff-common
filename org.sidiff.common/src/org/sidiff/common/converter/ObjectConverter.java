package org.sidiff.common.converter;

public interface ObjectConverter<T> {

	Class<T> getType();
	T getDefaultValue();
	String marshal(T object);
	T unmarshal(String string);
}
