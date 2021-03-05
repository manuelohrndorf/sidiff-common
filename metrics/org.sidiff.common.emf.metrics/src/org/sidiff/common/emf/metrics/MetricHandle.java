package org.sidiff.common.emf.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.Notifier;

/**
 * A metric handle is a wrapper for a {@link IMetric} and a context {@link Notifier},
 * which computes the value of the metric for the context and caches the result.
 * @author rmueller
 */
public class MetricHandle {

	/**
	 * A special value which indicates that the value of a handle
	 * has not yet been computed.
	 */
	public static final Object NOT_COMPUTED = new Object() {
		@Override
		public String toString() {
			return "<not computed>";
		}
	};

	/**
	 * A special value which indicates that the metric of a handle
	 * is not applicable for its context.
	 */
	public static final Object NOT_APPLICABLE = new Object() {
		@Override
		public String toString() {
			return "<not applicable>";
		}
	};


	private final IMetric metric;
	private final Notifier context;
	private final Map<Set<Object>,List<Object>> cachedValues;

	MetricHandle(IMetric metric, Notifier context) {
		Assert.isLegal(metric.getContextType().isInstance(context), "Type of metric is incompatible with context");
		this.metric = Objects.requireNonNull(metric);
		this.context = Objects.requireNonNull(context);

		cachedValues = new HashMap<>();
		clearCache();
	}

	/**
	 * The underlying metric which this handle computes.
	 * @return the metric
	 */
	public IMetric getMetric() {
		return metric;
	}

	/**
	 * The notifier for which the metric is computed by this handle.
	 * @return the context notifier
	 */
	public Notifier getContext() {
		return context;
	}

	/**
	 * Returns the cached values of metric for the context of this handle.
	 * @return the value of the metric for the context
	 */
	public Map<Set<Object>,List<Object>> getValues() {
		Assert.isTrue(!cachedValues.isEmpty(), "cachedValues should never be empty");
		return Collections.unmodifiableMap(cachedValues);
	}

	public boolean isUncategorized() {
		return cachedValues.size() == 1 && cachedValues.containsKey(Collections.emptySet());
	}

	public List<Object> getUncategorizedValues() {
		if(!isUncategorized()) {
			throw new IllegalStateException("Handle is not uncategorized");
		}
		return cachedValues.get(Collections.emptySet());
	}

	/**
	 * Clears the cached value of this handle, i.e. sets it to {@link #NOT_COMPUTED}.
	 */
	public void clearCache() {
		cachedValues.clear();
		cachedValues.put(Collections.emptySet(), Collections.singletonList(NOT_COMPUTED));
	}

	/**
	 * Recomputes the metric using the context notifier.
	 * @param monitor a monitor for progress reporting
	 */
	public void recompute(IProgressMonitor monitor) {
		Map<Set<Object>,List<Object>> values = new HashMap<>();
		metric.calculate(context, (keys, value) -> {
			if(value != null && keys != null) {
				values.computeIfAbsent(keys, unused -> new ArrayList<>()).add(value); 
			}
		}, monitor);

		cachedValues.clear();
		if(values.isEmpty()) {
			cachedValues.put(Collections.emptySet(), Collections.singletonList(NOT_APPLICABLE));
		} else {
			cachedValues.putAll(values);
		}
	}

	public boolean isNotComputed() {
		return cachedValues.getOrDefault(Collections.emptySet(), Collections.emptyList()).contains(NOT_COMPUTED);
	}

	public boolean isNotApplicable() {
		return cachedValues.getOrDefault(Collections.emptySet(), Collections.emptyList()).contains(NOT_APPLICABLE);
	}

	/**
	 * Returns whether a value is present, i.e. it is computed and the metric is applicable.
	 * @return <code>true</code> if a value is present, <code>false</code> otherwise
	 */
	public boolean isValuePresent() {
		return !isNotComputed() && !isNotApplicable();
	}

	/**
	 * Returns whether the handle has {@link #isValuePresent() none} or an
	 * irrelevant/uninteresting value which may be filtered before display or exporting.
	 * Uninteresting values include the number 0, and NaN. 
	 * @return
	 */
	public boolean isIrrelevant() {
		if(!isValuePresent()) {
			return true;
		}
		if(!isUncategorized()) {
			return false;
		}
		return getUncategorizedValues().stream()
				.allMatch(value -> {
					if(value instanceof String) {
						return ((String)value).isEmpty();
					} else if(value instanceof Number) {
						double doubleValue = ((Number)value).doubleValue();
						return doubleValue == 0 || Double.isNaN(doubleValue);
					}
					return false;
				});
	}

	public String getContextLabel() {
		return MetricsLabelUtil.getLabelForNotifier(context);
	}

	@Override
	public String toString() {
		return "[" + metric.getKey() + " : " + getContextLabel() + " : " + MetricsLabelUtil.getLabel(cachedValues) + "]";
	}

	public MetricHandle createCopy() {
		MetricHandle copy = new MetricHandle(metric, context);
		copy.cachedValues.clear();
		copy.cachedValues.putAll(cachedValues);
		return copy;
	}

	public static Comparator<MetricHandle> getByKeyComparator() {
		return Comparator.comparing(h -> h.getMetric().getKey());
	}
	
	public static Comparator<MetricHandle> getByNameComparator() {
		return Comparator.comparing(h -> h.getMetric().getName());
	}

	public static Comparator<MetricHandle> getByContextComparator() {
		return Comparator.comparing(MetricHandle::getContextLabel);
	}

	public static Comparator<MetricHandle> getByValueComparator() {
		return Comparator.<MetricHandle>comparingInt(h -> {
			if(h.isNotComputed()) {
				return 1;
			} else if(h.isNotApplicable()) {
				return 2;
			}
			return 0;
		}).thenComparing(h -> h.getValues().toString());
	}
}
