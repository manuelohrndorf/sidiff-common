package org.sidiff.common.exceptions;

/**
 * Root exception for all (non-runtime) exceptions in SiDiff.
 */
public class SiDiffException extends Exception {

	private static final long serialVersionUID = -1726238997249174286L;

	private String shortMessage;

	public SiDiffException(String message) {
		this(message, null, null);
	}

	public SiDiffException(String message, Throwable exception) {
		this(message, null, exception);
	}

	public SiDiffException(Throwable exception) {
		this(exception.getMessage(), null, exception);
	}

	public SiDiffException(String message, String shortMessage) {
		this(message, shortMessage, null);
	}

	public SiDiffException(String message, String shortMessage, Throwable exception) {
		super(message, exception);
		if(shortMessage == null) {
			shortMessage = "An error occurred";
		}
		this.shortMessage = shortMessage;
	}

	public String getShortMessage() {
		return shortMessage;
	}
}
