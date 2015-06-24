package org.sidiff.core.annotation.impl;

import org.sidiff.common.exceptions.SiDiffRuntimeException;

@SuppressWarnings("serial")
public class MissingRequirementException extends SiDiffRuntimeException {

	public MissingRequirementException(Object... message) {
		super(message);
	}

}
