package org.sidiff.common.emf.metrics;

import java.util.function.Consumer;

/**
 * An acceptor for the value/s computes by {@link IMetric}s.
 * @author rmueller
 */
@FunctionalInterface
public interface IMetricValueAcceptor extends Consumer<Object> {

	/**
	 * Accepts this metric value, adding it to the result.
	 * The value <code>null</code> is ignored.
	 * @param value the value to add
	 */
	void accept(Object value);
}
