package org.sidiff.common.emf.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
	 * "<code>metric-key;resource-uri;metric-value</code>".
	 * Only metrics which have been computed before will be exported.
	 * @return metrics in CSV format
	 */
	public String exportAsCsv() {
		return CSVWriter.writeToString(csvWriter -> {
			csvWriter.setColumnSeparator(";");
			for(MetricHandle handle : this) {
				if(handle.isValuePresent()) {
					csvWriter.write(
						handle.getMetric().getKey(),
						handle.getContextLabel(),
						MetricsUtil.getLabel(handle.getValues()));
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

	/**
	 * Finds a metric handle in this list which matches the given handle in metric and context.
	 * @param needle the metric to find
	 * @return matching handle in this list, empty if none
	 */
	public Optional<MetricHandle> findMatching(MetricHandle needle) {
		if(contains(needle)) {
			return Optional.of(needle);
		}
		Predicate<MetricHandle> metricFilter =
				handle -> handle.getMetric().getKey().equals(needle.getMetric().getKey());

		Notifier context = needle.getContext();
		if(context instanceof Resource) {
			// Match Resources based on URI
			URI contextUri = ((Resource)context).getURI();
			return stream().filter(metricFilter)
					.filter(handle -> handle.getContext() instanceof Resource
							&& contextUri.equals(((Resource)handle.getContext()).getURI()))
					.findFirst() // Then match based on only the file name if nothing found
						.or(() -> stream().filter(metricFilter)
						.filter(handle -> handle.getContext() instanceof Resource
								&& contextUri.lastSegment().equals(((Resource)handle.getContext()).getURI().lastSegment()))
						.findFirst());
		} else if(context instanceof ResourceSet) {
			// Match Resource Set because there is only a single one
			return stream().filter(metricFilter)
					.filter(handle -> handle.getContext() instanceof ResourceSet)
					.findFirst();
		} else if(context instanceof EObject) {
			// Match EObjects based on URI fragment
			String contextUriFragment = EcoreUtil.getURI((EObject)context).fragment();
			return stream().filter(metricFilter)
					.filter(handle -> handle.getContext() instanceof EObject
							&& contextUriFragment.equals(EcoreUtil.getURI((EObject)handle.getContext()).fragment()))
					.findFirst();
		}
		throw new AssertionError();
	}
}
