package org.sidiff.common.emf.metrics.jobs;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.sidiff.common.emf.metrics.MetricHandleDifference;

public class RecomputeMetricsDifferencesJob extends Job {

	private final Collection<? extends MetricHandleDifference> handleDifferences;
	private final Runnable doneCallback;

	public RecomputeMetricsDifferencesJob(Collection<? extends MetricHandleDifference> handleDifferences, Runnable doneCallback) {
		super("Recomputing " + handleDifferences.size() + " metric differences");
		this.handleDifferences = handleDifferences;
		this.doneCallback = Objects.requireNonNull(doneCallback);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, handleDifferences.size());
		for(MetricHandleDifference handleDiff : handleDifferences) {
			handleDiff.recompute(progress.split(1));
		}
		doneCallback.run();
		return Status.OK_STATUS;
	}
}
