package org.sidiff.common.emf.metrics;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.sidiff.common.extension.IExtension.Description;
import org.sidiff.common.extension.TypedExtensionManager;

/**
 * Typed extension manager for the {@link IMetric} typed extension.
 * @author rmueller
 */
public class MetricExtensionManager extends TypedExtensionManager<IMetric> {

	public MetricExtensionManager(Description<? extends IMetric> description) {
		super(description);
	}

	/**
	 * Returns all metrics for any of the given document types.
	 * @param documentTypes the document types
	 * @return all metrics for any given document type
	 */
	public List<IMetric> getAllMetrics(Set<String> documentTypes) {
		return documentTypes.stream()
			.map(docType -> getExtensions(Collections.singleton(docType), true))
			.flatMap(Collection::stream)
			.distinct()
			.sorted(getComparator())
			.collect(Collectors.toList());
	}
}
