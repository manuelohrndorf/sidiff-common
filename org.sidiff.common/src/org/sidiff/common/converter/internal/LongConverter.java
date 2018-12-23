package org.sidiff.common.converter.internal;

import org.sidiff.common.converter.ObjectConverter;

public class LongConverter implements ObjectConverter<Long> {

	@Override
	public Class<Long> getType() {
		return Long.class;
	}

	@Override
	public String marshal(Long object) {
		return String.valueOf(object);
	}

	@Override
	public Long unmarshal(String string) {
		return Long.parseLong(string);
	}

	@Override
	public Long getDefaultValue() {
		return 0L;
	}
}
