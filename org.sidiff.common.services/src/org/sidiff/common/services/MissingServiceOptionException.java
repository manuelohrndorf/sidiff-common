package org.sidiff.common.services;

import org.sidiff.common.exceptions.SiDiffRuntimeException;

@SuppressWarnings("serial")
public class MissingServiceOptionException extends SiDiffRuntimeException {
	
	public MissingServiceOptionException(Class<?> optionClass) {
		super(optionClass);
	}
}
