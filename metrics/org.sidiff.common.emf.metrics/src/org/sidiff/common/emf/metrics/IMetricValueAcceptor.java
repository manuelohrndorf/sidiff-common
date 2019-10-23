package org.sidiff.common.emf.metrics;

import java.util.Collections;
import java.util.Set;

/**
 * An acceptor for the value/s computes by {@link IMetric}s.
 * @author rmueller
 */
public interface IMetricValueAcceptor {

	/**
	 * Accepts this metric value, adding it to the result.
	 * The value <code>null</code> is ignored.
	 * @param value the value to add
	 */
	default void accept(Object value) {
		accept(Collections.emptySet(), value);
	}

	void accept(Set<Object> keys, Object value);
}
