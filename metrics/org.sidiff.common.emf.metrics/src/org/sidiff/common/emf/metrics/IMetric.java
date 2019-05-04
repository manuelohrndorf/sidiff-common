package org.sidiff.common.emf.metrics;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.Notifier;
import org.sidiff.common.extension.ITypedExtension;

/**
 * A metric is a typed extension which computes a value for input notifiers.
 * Metrics only compute values for arbitrary resources and must be stateless.
 * @author rmueller
 */
public interface IMetric extends ITypedExtension {

	Description<IMetric> DESCRIPTION = Description.of(IMetric.class,
		"org.sidiff.common.emf.metrics.metrics_extension", "metric", "class");

	MetricExtensionManager MANAGER = new MetricExtensionManager(DESCRIPTION);

	/**
	 * Returns the type of the notifier that this metric calculates values for.
	 * Can be an arbitrary Resource's, ResourceSet's or EObject's class.
	 * @return the context type
	 */
	Class<? extends Notifier> getContextType();

	/**
	 * Calculates the value of the metric for the notifier.
	 * @param context the context for which to compute the metric, is always an instance of {@link #getContextType()}
	 * @param acceptor an acceptor for the resulting metric value/s
	 * @param monitor a progress monitor
	 */
	void calculate(Notifier context, IMetricValueAcceptor acceptor, IProgressMonitor monitor);
}
