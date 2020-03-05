package org.sidiff.common.emf.metrics;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.sidiff.common.file.CSVWriter;

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

	/**
	 * Exports the differences in this list in CSV format, as
	 * "<code>metric-key;origin-uri;change-uri;comparison-result</code>".
	 * Only differences which have been computed and have a result before will be exported.
	 * @return differences in CSV format
	 */
	public String exportAsCsv() {
		return CSVWriter.writeToString(csvWriter -> {
			csvWriter.setColumnSeparator(";");
			for(MetricHandleDifference handle : this) {
				if(handle.hasResults()) {
					csvWriter.write(
						handle.getMetric().getKey(),
						handle.getOrigin().getContextLabel(),
						handle.getChanged().getContextLabel(),
						MetricsUtil.getLabel(handle.getResults()));
				}
			}
		});
	}
}
