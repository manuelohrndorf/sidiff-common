package org.sidiff.common.converter.internal;

import org.sidiff.common.converter.ObjectConverter;

public class BooleanConverter implements ObjectConverter<Boolean> {

	@Override
	public Class<Boolean> getType() {
		return Boolean.class;
	}

	@Override
	public String marshal(Boolean object) {
		return String.valueOf(object);
	}

	@Override
	public Boolean unmarshal(String string) {
		return Boolean.parseBoolean(string);
	}

	@Override
	public Boolean getDefaultValue() {
		return false;
	}
}
