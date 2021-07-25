package org.sidiff.common.converter.internal;

import org.sidiff.common.converter.ObjectConverter;

public class ShortConverter implements ObjectConverter<Short> {

	@Override
	public Class<Short> getType() {
		return Short.class;
	}

	@Override
	public String marshal(Short object) {
		return String.valueOf(object);
	}

	@Override
	public Short unmarshal(String string) {
		return Short.parseShort(string);
	}

	@Override
	public Short getDefaultValue() {
		return 0;
	}
}
