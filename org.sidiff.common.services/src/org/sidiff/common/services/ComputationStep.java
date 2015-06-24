package org.sidiff.common.services;

import java.util.Map;

/**
 * Interface to describe one computation step that has one data type of input and one data type of output.
 *
 * @param <I>
 * @param <O>
 */
public interface ComputationStep<I, O> {

	/**
	 * @param options 
	 * @param input
	 * @return 
	 */
	public O execute(Map<Object,Object> options, I... input);
}
