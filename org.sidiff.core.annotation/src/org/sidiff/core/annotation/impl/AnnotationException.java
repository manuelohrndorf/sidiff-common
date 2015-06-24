package org.sidiff.core.annotation.impl;

import org.sidiff.common.exceptions.SiDiffRuntimeException;

@SuppressWarnings("serial")
public class AnnotationException extends SiDiffRuntimeException {

	public AnnotationException(Object... message) {
		super(message);
	}

}
