package org.sidiff.common.emf.annotation.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.sidiff.common.emf.annotation.AnnotateableElement;
import org.sidiff.common.exceptions.SiDiffRuntimeException;

/**
 * Implementation of the adapter AnnotateableElement
 * @author wenzel
 */
public class AnnotateableElementImpl extends AdapterImpl implements AnnotateableElement {

	private Map<String, Object> annotations = new TreeMap<>();

	@Override
	public <T> T getAnnotation(String key, Class<T> type) {
		return type.cast(annotations.get(key));
	}

	@Override
	public boolean hasAnnotation(String key) {
		return annotations.containsKey(key);
	}

	@Override
	public void removeAnnotation(String key) {
		annotations.remove(key);
	}

	@Override
	public void setAnnotation(String key, Object value) {
		annotations.put(key, value);
	}

	@Override
	public <T> T getOrCreateAnnotation(String key, Class<T> type) {
		if (!hasAnnotation(key)) {
			try {
				setAnnotation(key, type.getDeclaredConstructor().newInstance());
			} catch (Exception e) {
				throw new SiDiffRuntimeException("Cannot instantiate '" + type.getName() + "' for annotations.", e);
			}
		}
		return type.cast(annotations.get(key));
	}

	@Override
	public Collection<String> getAnnotations() {
		return Collections.unmodifiableSet(annotations.keySet());
	}

	@Override
	public boolean isAdapterForType(Object type) {
		return type == AnnotateableElement.class;
	}

	@Override
	public String toString() {
		return "AnnotateableElement[target=" + getTarget() + ", annotations=" + annotations + "]";
	}
}
