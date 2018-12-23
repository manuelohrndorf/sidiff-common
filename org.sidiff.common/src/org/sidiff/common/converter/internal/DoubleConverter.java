package org.sidiff.common.converter.internal;

import org.sidiff.common.converter.ObjectConverter;

public class DoubleConverter implements ObjectConverter<Double> {

	@Override
	public Class<Double> getType() {
		return Double.class;
	}

	@Override
	public String marshal(Double object) {
		return String.valueOf(object);
	}

	@Override
	public Double unmarshal(String string) {
		return Double.parseDouble(string);
	}
	
	@Override
	public Double getDefaultValue() {
		return 0.0;
	}
}
