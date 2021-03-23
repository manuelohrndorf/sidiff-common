package org.sidiff.common.exceptions;

/**
 * Root exception for all runtime exceptions in SiDiff.
 */
public class SiDiffRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -7081327426269876189L;

	private String shortMessage;

	public SiDiffRuntimeException(String message) {
		this(message, null, null);
	}

	public SiDiffRuntimeException(String message, Throwable exception) {
		this(message, null, exception);
	}

	public SiDiffRuntimeException(Throwable exception) {
		this(exception.getMessage(), null, exception);
	}

	public SiDiffRuntimeException(String message, String shortMessage) {
		this(message, shortMessage, null);
	}

	public SiDiffRuntimeException(String message, String shortMessage, Throwable exception) {
		super(message, exception);
		if(shortMessage == null) {
			this.shortMessage = "A runtime error occurred";
		} else {
			this.shortMessage = shortMessage;
		}
	}

	public String getShortMessage() {
		return shortMessage;
	}
}
