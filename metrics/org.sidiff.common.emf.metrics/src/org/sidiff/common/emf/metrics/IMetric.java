package org.sidiff.common.emf.metrics;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.extension.ITypedExtension;

/**
 * A metric is a typed extension which computes a value for input resources.
 * Metrics only compute values for arbitrary resources and must be stateless.
 * @author rmueller
 */
public interface IMetric extends ITypedExtension {

	Description<IMetric> DESCRIPTION = Description.of(IMetric.class,
		"org.sidiff.common.emf.metrics.metrics_extension", "metric", "class");

	MetricExtensionManager MANAGER = new MetricExtensionManager(DESCRIPTION);

	/**
	 * Calculates the value of the metric for the resource.
	 * @param resource the resource for which to compute the metric
	 * @param acceptor an acceptor for the resulting metric value/s
	 * @param monitor a progress monitor
	 */
	void calculate(Resource resource, IMetricValueAcceptor acceptor, IProgressMonitor monitor);
}
