package org.sidiff.common.stringresolver;

/**
 * Interface for a class that converts an object of a specific class into string representation.
 * I.e. used by the StringUtil. 
 */
public interface StringResolver {

	public Class<?> dedicatedClass();
	public String resolve(Object obj);
}
