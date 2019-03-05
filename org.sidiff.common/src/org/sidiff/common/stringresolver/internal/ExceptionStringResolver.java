package org.sidiff.common.stringresolver.internal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.sidiff.common.stringresolver.StringResolver;

/**
 * StringResolver to compute the string representation of an exception.
 */
public class ExceptionStringResolver implements StringResolver {

	@Override
	public Class<?> dedicatedClass() {
		return Exception.class;
	}

	@Override
	public String resolve(Object obj) {
		
		if (!(obj instanceof Exception))
			return null;
		
		Exception exception = (Exception)obj;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exception.printStackTrace(new PrintStream(baos));
		return " Exception Message: " + exception.getMessage() + "\nSee StackTrace for details:\n" + baos.toString();
	}
}