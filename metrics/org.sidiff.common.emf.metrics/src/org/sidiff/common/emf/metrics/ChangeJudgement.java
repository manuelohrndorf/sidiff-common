package org.sidiff.common.emf.metrics;

/**
 * Describes how the change of a metric's value is judged based on the metric's {@link ComparisonType}.
 * @author rmueller
 */
public enum ChangeJudgement {

	/**
	 * There is nothing to judge or the metric is only informative
	 * and cannot be judged (i.e. {@link ComparisonType#UNSPECIFIED}).
	 */
	NONE,

	/**
	 * Metric can be judged, but has not changed.
	 */
	UNCHANGED,

	/**
	 * Metric value improved.
	 */
	GOOD,

	/**
	 * Metric value deteriorated.
	 */
	BAD
}