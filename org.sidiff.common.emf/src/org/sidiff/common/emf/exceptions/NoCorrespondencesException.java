package org.sidiff.common.emf.exceptions;

import org.sidiff.common.exceptions.SiDiffException;

public class NoCorrespondencesException extends SiDiffException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4911109852204584000L;
	private static final String MESSAGE = "No Correspondences found between models.\n" +
			"This could be due to a wrong Matcher for this document/model type.";

	public NoCorrespondencesException() {
		super(MESSAGE, "No correspondences found");
	}
}
