package org.sidiff.common.converter.internal;

import org.sidiff.common.converter.ObjectConverter;

public class IntegerConverter implements ObjectConverter<Integer> {

	@Override
	public Class<Integer> getType() {
		return Integer.class;
	}

	@Override
	public String marshal(Integer object) {
		return String.valueOf(object);
	}

	@Override
	public Integer unmarshal(String string) {
		return Integer.parseInt(string);
	}

	@Override
	public Integer getDefaultValue() {
		return 0;
	}
}
