package org.sidiff.common.emf.annotation.internal;

import java.util.*;

import org.sidiff.common.emf.adapters.SiDiffAdapterImpl;
import org.sidiff.common.emf.annotation.AnnotateableElement;
import org.sidiff.common.exceptions.SiDiffRuntimeException;

/**
 * Implementation of the adapter AnnotateableElement
 * @author wenzel
 *
 */
public class AnnotateableElementImpl extends SiDiffAdapterImpl implements AnnotateableElement {

	private Map<String, Object> annotations = new TreeMap<String, Object>();

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
		if (!hasAnnotation(key))
			try {
				setAnnotation(key, type.getDeclaredConstructor().newInstance());
			} catch (Exception e) {
				throw new SiDiffRuntimeException("Cannot instantiate '" + type.getName() + "' for annotations. ", e);
			}
		return type.cast(annotations.get(key));
	}

	@Override
	public Collection<String> getAnnotations() {
		return Collections.unmodifiableSet(annotations.keySet());
	}
}
