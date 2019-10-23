package org.sidiff.common.emf.metrics;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class MetricsUtil {

	public MetricsUtil() {
		throw new AssertionError();
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

	public static String getLabel(Object object) {
		if(object == null) {
			return "null";
		}
		if(object instanceof Map<?,?>) {
			Map<?,?> map = (Map<?,?>)object;
			if(map.isEmpty()) {
				return "<empty map>";
			}
			if(map.size() == 1 && map.containsKey(Collections.emptySet())) {
				return getLabel(map.get(Collections.emptySet()));
			}
			return map.entrySet().stream()
					.map(entry -> getLabel(entry.getKey()) + "=" + getLabel(entry.getValue()))
					.collect(Collectors.joining(", ", "[", "]"));
		}
		if(object instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>)object;
			if(collection.size() <= 1) {
				return collection.stream().findFirst().map(MetricsUtil::getLabel).orElse("<empty>");
			}
			return collection.stream().map(MetricsUtil::getLabel).collect(Collectors.joining(", ", "[", "]"));
		}
		if(object instanceof Notifier) {
			return getLabelForNotifier((Notifier)object);
		}
		return object.toString();
	}
}
