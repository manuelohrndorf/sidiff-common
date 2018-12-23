package org.sidiff.common.converter.internal;

import org.sidiff.common.converter.ObjectConverter;

public class StringConverter implements ObjectConverter<String> {

	@Override
	public Class<String> getType() {
		return String.class;
	}

	@Override
	public String marshal(String object) {
		return object;
	}

	@Override
	public String unmarshal(String string) {
		return string;
	}

	@Override
	public String getDefaultValue() {
		return "";
	}
}
