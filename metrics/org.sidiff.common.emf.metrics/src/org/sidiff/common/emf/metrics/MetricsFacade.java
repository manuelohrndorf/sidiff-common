package org.sidiff.common.emf.metrics;

import java.util.stream.Collectors;

/**
 * Provides high level access to the {@link IMetric} extension,
 * and allows retrieving all metrics for ResourceSets, Resources and EObjects.
 * @author rmueller
 */
public class MetricsFacade {

	/**
	 * Returns a {@link MetricsList} of {@link MetricHandle}s, for notifiers selected by the MetricsScope and
	 * all {@link IMetric}s, which support the given notifiers' document types.
	 * The values of the metrics are not actually computed until {@link MetricsList#recomputeAll}
	 * or {@link MetricHandle#recompute} are called.
	 * @param scope the scope which selected the notifiers to calculate metrics for
	 * @return a list of handles for notifier contents which can be used to compute the metrics values
	 */
	public static MetricsList getMetrics(MetricsScope scope) {
		return IMetric.MANAGER.getAllMetrics(scope.getDocumentTypes()).stream()
			.flatMap(metric -> scope.getApplicableContexts(metric.getContextType())
				.map(notifier -> new MetricHandle(metric, notifier)))
			.collect(Collectors.toCollection(MetricsList::new));
	}
}
