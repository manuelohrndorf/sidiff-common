package org.sidiff.common.emf.metrics;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.sidiff.common.collections.Pair;

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

	/**
	 * Calculates a difference between two {@link MetricsList}s and returns a {@link MetricsListDifference},
	 * which contains {@link MetricHandleDifference}s for each matched {@link MetricHandle} in the two lists.
	 * @param origin the origin metrics list for comparison
	 * @param changed the changed metrics list for comparison
	 * @return list of individual differences handles, which can be used to compute the difference values
	 */
	public static MetricsListDifference calculateDifference(MetricsList origin, MetricsList changed) {
		// Create matching between the two lists
		Set<Pair<MetricHandle,MetricHandle>> correspondences = new HashSet<>();
		for(MetricHandle originHandle : origin) {
			changed.findMatching(originHandle).ifPresent(match -> correspondences.add(Pair.of(originHandle, match)));
		}
		for(MetricHandle changedHandle : changed) {
			origin.findMatching(changedHandle).ifPresent(match -> correspondences.add(Pair.of(match, changedHandle)));
		}

		// Remove incomparable correspondence
		correspondences.removeIf(correspondence -> correspondence.getFirst() == null || correspondence.getSecond() == null
				|| correspondence.getFirst().isIrrelevant() || correspondence.getSecond().isIrrelevant());

		return correspondences.stream()
			.map(correspondence -> new MetricHandleDifference(correspondence.getFirst(), correspondence.getSecond()))
			.collect(Collectors.toCollection(MetricsListDifference::new));
	}
}
