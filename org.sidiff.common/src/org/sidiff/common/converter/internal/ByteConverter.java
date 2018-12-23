package org.sidiff.common.converter.internal;

import org.sidiff.common.converter.ObjectConverter;

public class ByteConverter implements ObjectConverter<Byte> {

	@Override
	public Class<Byte> getType() {
		return Byte.class;
	}

	@Override
	public String marshal(Byte object) {
		return String.valueOf(object);
	}

	@Override
	public Byte unmarshal(String string) {
		return Byte.parseByte(string);
	}

	@Override
	public Byte getDefaultValue() {
		return 0;
	}
}
