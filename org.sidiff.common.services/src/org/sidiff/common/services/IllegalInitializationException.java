package org.sidiff.common.services;

import org.sidiff.common.exceptions.SiDiffRuntimeException;

public class IllegalInitializationException extends SiDiffRuntimeException {

	private static final long serialVersionUID = 8022671712451120327L;

	public IllegalInitializationException(Object... message) {
		super(message);
	}

}
