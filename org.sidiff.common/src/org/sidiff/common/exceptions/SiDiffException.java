package org.sidiff.common.exceptions;

import org.sidiff.common.util.StringUtil;

/**
 * Root exception for all (non-runtime) exceptions in SiDiff.
 */
public class SiDiffException extends Exception {

	private static final long serialVersionUID = -1726238997249174286L;

	private String shortMessage;

	public SiDiffException(Object... extra) {
		super(StringUtil.resolve(extra), getOriginalException(extra));
		this.shortMessage = "An error occurred";
	}

	public SiDiffException(String message, String shortMessage, Object... extra) {
		super(message, getOriginalException(extra));
		this.shortMessage = shortMessage;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	static Throwable getOriginalException(Object[] message) {
		for (Object msgPart : message) {
			if (msgPart instanceof Throwable) {
				return (Throwable)msgPart;
			}
		}
		return null;
	}

}
