package org.sidiff.common.services;

import org.sidiff.common.exceptions.SiDiffRuntimeException;

public class ServiceNotFoundException extends SiDiffRuntimeException {

	private static final long serialVersionUID = -7149404465914277842L;

	public ServiceNotFoundException(Object... message) {
		super(message);
	}

}
