package org.sidiff.common.emf.metrics;

/**
 * Defines how to compare the value of metrics and which kind of value is "better".
 * @author rmueller
 */
public enum ComparisonType {

	/**
	 * The metric has no defined "better" value.
	 */
	UNSPECIFIED,

	/**
	 * Higher values in this metric considered "better".
	 */
	HIGHER_IS_BETTER,

	/**
	 * Lower values in this metric considered "better".
	 */
	LOWER_IS_BETTER
}
