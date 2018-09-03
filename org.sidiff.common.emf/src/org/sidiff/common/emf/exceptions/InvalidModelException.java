package org.sidiff.common.emf.exceptions;

import org.sidiff.common.exceptions.SiDiffException;

public class InvalidModelException extends SiDiffException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 878132164942287950L;

	public InvalidModelException(String message) {
		super(message, "Model is invalid");
	}

}
