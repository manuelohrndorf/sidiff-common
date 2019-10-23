package org.sidiff.common.emf.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.sidiff.common.file.CSVWriter;

/**
 * A metrics list is a list of {@link MetricHandle}s, which provides methods
 * to compute and export all contained handles.
 * @author rmueller
 */
public class MetricsList extends ArrayList<MetricHandle> {

	private static final long serialVersionUID = 185591622916918461L;

	public MetricsList() {
	}

	public MetricsList(Collection<? extends MetricHandle> c) {
		super(c);
	}

	public MetricsList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Exports the metrics in this list as a map of "<i>metric-key</i> : <i>resource-uri</i>" to 
	 * the value of the metric. Only metrics which have been computed before will be exported.
	 * The map is suitable to use with <code>StatisticsUtil.createStatisticsUtil(Map)</code>.
	 * @return map of "<i>metric-key</i> : <i>resource-uri</i>" -> <i>metric-value</i>
	 */
	public Map<String, Object> export() {
		return stream()
			.filter(MetricHandle::isValuePresent)
			.collect(Collectors.toMap(
				h -> h.getMetric().getKey() + " : " + h.getContextLabel(),
				h -> h.getValues()));
	}

	/**
	 * Exports the metrics in this list in CSV format, as
	 * "<code>metric-key,resource-uri,metric-value</code>".
	 * Only metrics which have been computed before will be exported.
	 * @return metrics in CSV format
	 */
	public String exportAsCsv() {
		return CSVWriter.writeToString(csvWriter -> {
			for(MetricHandle handle : this) {
				if(handle.isValuePresent()) {
					csvWriter.write(
						handle.getMetric().getKey(),
						handle.getContextLabel(),
						handle.getValues());
				}
			}
		});
	}

	/**
	 * Recomputes the values of all metrics of in this list.
	 * @param monitor a progress monitor
	 */
	public void recomputeAll(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, size());
		forEach(handle -> handle.recompute(progress.split(1)));
	}

	/**
	 * Removes all {@link MetricHandle#isIrrelevant() irrelevant} metrics from this list.
	 */
	public void removeAllIrrelevant() {
		removeIf(MetricHandle::isIrrelevant);
	}
}
