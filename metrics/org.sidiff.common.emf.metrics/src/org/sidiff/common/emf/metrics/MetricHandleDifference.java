package org.sidiff.common.emf.metrics;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

public class MetricHandleDifference {

	private final IMetric metric;
	private final MetricHandle origin;
	private final MetricHandle changed;

	private final Map<Set<Object>,MetricValueComparisonResult> results;

	MetricHandleDifference(MetricHandle origin, MetricHandle changed) {
		this.origin = Objects.requireNonNull(origin);
		this.changed = Objects.requireNonNull(changed);
		Assert.isLegal(origin.getMetric().getKey().equals(changed.getMetric().getKey()), "metrics not matching");
		this.metric = origin.getMetric();

		this.results = new HashMap<>();
		clearCache();
	}

	public void clearCache() {
		results.clear();
	}

	public void recompute(IProgressMonitor monitor) {
		Map<Set<Object>,List<Object>> originValues = origin.getValues();
		Map<Set<Object>,List<Object>> changedValues = changed.getValues();

		Set<Set<Object>> allKeys = new HashSet<>();
		allKeys.addAll(originValues.keySet());
		allKeys.addAll(changedValues.keySet());

		clearCache();
		for(Set<Object> key : allKeys) {
			MetricValueComparisonResult result =
				new MetricValueComparisonResult(
					metric.getComparisonType(),
					originValues.getOrDefault(key, Collections.emptyList()),
					changedValues.getOrDefault(key, Collections.emptyList()));
			results.put(key, result);
		}
	}
	
	public Map<Set<Object>, MetricValueComparisonResult> getResults() {
		return Collections.unmodifiableMap(results);
	}

	public IMetric getMetric() {
		return metric;
	}

	public MetricHandle getOrigin() {
		return origin;
	}

	public MetricHandle getChanged() {
		return changed;
	}
	
	public boolean hasResults() {
		return !results.isEmpty();
	}

	public boolean isUncategorized() {
		return results.size() == 1 && results.containsKey(Collections.emptySet());
	}

	public MetricValueComparisonResult getUncategorizedResults() {
		if(!isUncategorized()) {
			throw new IllegalStateException("Handle is not uncategorized");
		}
		return results.get(Collections.emptySet());
	}

	@Override
	public String toString() {
		return "MetricHandleDifference[origin=" + origin + ", changed=" + changed + ", results=" + MetricsUtil.getLabel(results) + "]";
	}

	public static Comparator<MetricHandleDifference> getByKeyComparator() {
		return Comparator.comparing(h -> h.getMetric().getKey());
	}

	public static Comparator<MetricHandleDifference> getByNameComparator() {
		return Comparator.comparing(h -> h.getMetric().getName());
	}

	public static Comparator<MetricHandleDifference> getByContextComparator() {
		return Comparator.comparing(h -> h.getOrigin().getContextLabel());
	}
}
