package org.sidiff.common.emf.metrics.jobs;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.sidiff.common.emf.metrics.MetricHandle;

public class RecomputeMetricsJob extends Job {

	private final Collection<? extends MetricHandle> handles;
	private final Runnable doneCallback;

	public RecomputeMetricsJob(Collection<? extends MetricHandle> handles, Runnable doneCallback) {
		super("Recomputing " + handles.size() + " metrics");
		this.handles = handles;
		this.doneCallback = Objects.requireNonNull(doneCallback);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, handles.size());
		for(MetricHandle handle : handles) {
			handle.recompute(progress.split(1));
		}
		doneCallback.run();
		return Status.OK_STATUS;
	}
}
