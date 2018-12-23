package org.sidiff.common.converter.internal;

import org.sidiff.common.converter.ObjectConverter;

public class FloatConverter implements ObjectConverter<Float> {

	@Override
	public Class<Float> getType() {
		return Float.class;
	}

	@Override
	public String marshal(Float object) {
		return String.valueOf(object);
	}

	@Override
	public Float unmarshal(String string) {
		return Float.parseFloat(string);
	}
	
	@Override
	public Float getDefaultValue() {
		return 0.0f;
	}
}
