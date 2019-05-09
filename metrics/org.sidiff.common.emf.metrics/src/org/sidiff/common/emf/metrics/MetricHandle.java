package org.sidiff.common.emf.metrics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

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

	private Object cachedValue = NOT_COMPUTED;

	MetricHandle(IMetric metric, Notifier context) {
		Assert.isLegal(metric.getContextType().isInstance(context), "Type of metric is incompatible with context");
		this.metric = Objects.requireNonNull(metric);
		this.context = Objects.requireNonNull(context);
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
	 * Returns the cached value of metric for the context of this handle.
	 * May also be {@link #NOT_COMPUTED} or {@link #NOT_APPLICABLE}.
	 * @return the value of the metric for the context
	 */
	public Object getValue() {
		return cachedValue;
	}

	/**
	 * Clears the cached value of this handle, i.e. sets it to {@link #NOT_COMPUTED}.
	 */
	public void clearCache() {
		cachedValue = NOT_COMPUTED;
	}

	/**
	 * Recomputes the metric using the context notifier.
	 * @param monitor a monitor for progress reporting
	 */
	public void recompute(IProgressMonitor monitor) {
		List<Object> values = new ArrayList<>();
		metric.calculate(context, value -> {
			if(value != null) {
				values.add(value);
			}
		}, monitor);
		switch(values.size()) {
			case 0:
				cachedValue = NOT_APPLICABLE;
				return;
			case 1:
				cachedValue = values.get(0);
				return;
			default:
				cachedValue = values;
				return;
		}
	}

	/**
	 * Returns whether a value is present, i.e. it is computed and the metric is applicable.
	 * @return <code>true</code> if a value is present, <code>false</code> otherwise
	 */
	public boolean isValuePresent() {
		return cachedValue != NOT_COMPUTED && cachedValue != NOT_APPLICABLE;
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
		if(cachedValue instanceof Number) {
			double doubleValue = ((Number)cachedValue).doubleValue();
			return doubleValue == 0 || Double.isNaN(doubleValue);
		}
		return false;
	}

	public String getContextLabel() {
		return getLabelForNotifier(context);
	}

	public static String getLabelForNotifier(Notifier notifier) {
		if(notifier == null) {
			return "no selection";
		} else if(notifier instanceof ResourceSet) {
			return ((ResourceSet)notifier).getResources().stream()
				.map(Resource::getURI)
				.map(Object::toString)
				.collect(Collectors.joining(", ", "ResourceSet[", "]"));
		} else if(notifier instanceof Resource) {
			return ((Resource)notifier).getURI().toString();
		} else if(notifier instanceof EObject) {
			return EcoreUtil.getURI((EObject)notifier).toString();
		}
		throw new AssertionError();
	}

	@Override
	public String toString() {
		return "[" + metric.getKey() + " : " + getContextLabel() + " : " + cachedValue + "]";
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
			if(h.getValue() == MetricHandle.NOT_APPLICABLE) {
				return 1;
			} else if(h.getValue() == MetricHandle.NOT_APPLICABLE) {
				return 2;
			}
			return 0;
		}).thenComparing(h -> h.getValue().toString());
	}
}
