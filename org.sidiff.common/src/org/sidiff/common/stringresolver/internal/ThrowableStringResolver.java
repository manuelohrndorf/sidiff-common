package org.sidiff.common.stringresolver.internal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.sidiff.common.stringresolver.StringResolver;

/**
 * StringResolver to compute the string representation of a throwable.
 */
public class ThrowableStringResolver implements StringResolver {

	@Override
	public Class<?> dedicatedClass() {
		return Throwable.class;
	}

	@Override
	public String resolve(Object obj) {
		if (!(obj instanceof Throwable)) {
			return null;
		}
		Throwable throwable = (Exception)obj;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		throwable.printStackTrace(new PrintStream(baos));
		return " Exception Message: " + throwable.getMessage() + "\nSee StackTrace for details:\n" + baos.toString();
	}
}