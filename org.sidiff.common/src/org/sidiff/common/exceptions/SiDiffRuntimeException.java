package org.sidiff.common.exceptions;

import org.sidiff.common.util.StringUtil;

/**
 * Root exception for all runtime exceptions in SiDiff.
 */
public class SiDiffRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -7081327426269876189L;

	private String shortMessage;

	public SiDiffRuntimeException(Object... extra) {
		super(StringUtil.resolve(extra), SiDiffException.getOriginalException(extra));
		this.shortMessage = "A runtime error occurred";
	}

	public SiDiffRuntimeException(String message, String shortMessage, Object... extra) {
		super(message, SiDiffException.getOriginalException(extra));
		this.shortMessage = shortMessage;
	}

	public String getShortMessage() {
		return shortMessage;
	}
}
