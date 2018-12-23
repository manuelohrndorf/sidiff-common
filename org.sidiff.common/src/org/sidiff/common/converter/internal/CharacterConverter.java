package org.sidiff.common.converter.internal;

import org.sidiff.common.converter.ObjectConverter;

public class CharacterConverter implements ObjectConverter<Character> {

	@Override
	public Class<Character> getType() {
		return Character.class;
	}

	@Override
	public String marshal(Character object) {
		return String.valueOf(object);
	}

	@Override
	public Character unmarshal(String string) {
		if(string == null || string.isEmpty()) {
			throw new IllegalArgumentException("");
		}
		return string.charAt(0);
	}

	@Override
	public Character getDefaultValue() {
		return 0;
	}
}
