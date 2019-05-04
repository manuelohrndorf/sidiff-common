package org.sidiff.common.emf.metrics;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.emf.access.EMFModelAccess;

/**
 * Provides high level access to the {@link IMetric} extension,
 * and allows retrieving all metrics for ResourceSets, Resources and EObjects.
 * @author rmueller
 */
public class MetricsFacade {

	/**
	 * Returns a {@link MetricsList} of {@link MetricHandle}s, for the contents of the given notifier and
	 * all {@link IMetric}s, which support the given notifier's document types.
	 * The values of the metrics are not actually computed until {@link MetricsList#recomputeAll}
	 * or {@link MetricHandle#recompute} are called.
	 * @param context the context notifier (ResourceSet, Resource, EObject)
	 * @return a list of handles for notifier contents which can be used to compute the metrics values
	 */
	public static MetricsList getMetrics(Notifier context) {
		if(context instanceof ResourceSet) {
			ResourceSet resourceSet = (ResourceSet)context;
			return Stream.concat(
					getMetricsImpl(resourceSet),
					resourceSet.getResources().stream().flatMap(MetricsFacade::getMetricsImpl))
				.collect(Collectors.toCollection(MetricsList::new));
		} else if(context instanceof Resource) {
			return getMetricsImpl((Resource)context)
				.collect(Collectors.toCollection(MetricsList::new));
		} else if(context instanceof EObject) {
			return getMetricsImpl((EObject)context)
				.collect(Collectors.toCollection(MetricsList::new));
		}
		throw new IllegalArgumentException("Illegal metrics context: " + context);
	}

	private static Stream<MetricHandle> getMetricsImpl(ResourceSet resourceSet) {
		return IMetric.MANAGER.getAllMetrics(EMFModelAccess.getDocumentTypes(resourceSet)).stream()
				.filter(metric -> ResourceSet.class.isAssignableFrom(metric.getContextType()))
				.map(metric -> new MetricHandle(metric, resourceSet));
	}

	private static Stream<MetricHandle> getMetricsImpl(Resource resource) {
		return IMetric.MANAGER.getAllMetrics(EMFModelAccess.getDocumentTypes(resource)).stream()
				.flatMap(metric -> {
					Class<?> contextType = metric.getContextType();
					if(Resource.class.isAssignableFrom(contextType)) {
						return Stream.of(new MetricHandle(metric, resource));
					} else if(EObject.class.isAssignableFrom(contextType)) {
						return CollectionUtil.<EObject>asStream(EcoreUtil.getAllContents(resource, true))
							.filter(contextType::isInstance)
							.map(obj -> new MetricHandle(metric, obj));
					}
					return Stream.empty();
				});
	}

	private static Stream<MetricHandle> getMetricsImpl(EObject eObject) {
		return IMetric.MANAGER.getAllMetrics(EMFModelAccess.getDocumentTypes(eObject)).stream()
				.flatMap(metric ->
					CollectionUtil.<EObject>asStream(EcoreUtil.getAllContents(eObject, true))
						.filter(metric.getContextType()::isInstance)
						.map(obj -> new MetricHandle(metric, obj))
				);
	}
}
