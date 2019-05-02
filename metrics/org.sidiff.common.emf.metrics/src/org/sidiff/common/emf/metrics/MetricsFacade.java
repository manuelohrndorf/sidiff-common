package org.sidiff.common.emf.metrics;

import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.common.emf.access.Scope;

/**
 * Provides high level access to the {@link IMetric} extension,
 * and allows retrieving all metrics for a resource.
 * @author rmueller
 */
public class MetricsFacade {

	/**
	 * Returns a {@link MetricsList} of {@link MetricHandle}s, for the resource and all {@link IMetric}s, which
	 * support the given resource's document types. The values of the metrics are not actually computed until
	 * {@link MetricsList#recomputeAll} or {@link MetricHandle#recompute} are called.
	 * @param resource the resource
	 * @return a list of handles which can be used to compute the metrics values
	 */
	public static MetricsList getMetrics(Resource resource) {
		return IMetric.MANAGER.getAllMetrics(EMFModelAccess.getDocumentTypes(resource, Scope.RESOURCE)).stream()
				.map(metric -> new MetricHandle(metric, resource))
				.collect(Collectors.toCollection(MetricsList::new));
	}
}
