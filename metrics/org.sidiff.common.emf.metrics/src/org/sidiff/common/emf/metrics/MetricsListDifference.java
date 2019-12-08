package org.sidiff.common.emf.metrics;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

public class MetricsListDifference extends ArrayList<MetricHandleDifference> {

	private static final long serialVersionUID = 3153386032148717312L;

	public MetricsListDifference() {
	}

	public MetricsListDifference(Collection<? extends MetricHandleDifference> c) {
		super(c);
	}

	public MetricsListDifference(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Recomputes all differences in this list.
	 * @param monitor a progress monitor
	 */
	public void recomputeAll(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, size());
		forEach(difference -> difference.recompute(progress.split(1)));
	}
}
