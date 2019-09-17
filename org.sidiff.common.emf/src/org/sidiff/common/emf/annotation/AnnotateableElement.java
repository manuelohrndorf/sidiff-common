package org.sidiff.common.emf.annotation;

import java.util.Collection;

import org.eclipse.emf.common.notify.Adapter;

/**
 * Adapter that allows us to extend EObjects with additional informations.
 * @author wenzel
 */
public interface AnnotateableElement extends Adapter {

	/**
	 * Sets an annotation value.
	 * 
	 * @param key
	 * @param value
	 */
	void setAnnotation(String key, Object value);

	/**
	 * Gets an annotation value.
	 * 
	 * @param <T>
	 * @param key
	 * @param type
	 * @return
	 */
	<T> T getAnnotation(String key, Class<T> type);

	/**
	 * Gets an annotation value, or if it does not exists creates one using the standard constructor, sets it, and returns it.
	 * 
	 * @param <T>
	 * @param key
	 * @param type
	 * @return
	 */
	<T> T getOrCreateAnnotation(String key, Class<T> type);

	/**
	 * Checks whether the object has the given annotation.
	 * 
	 * @param key
	 * @return
	 */
	boolean hasAnnotation(String key);

	/**
	 * Removes an annotation.
	 * 
	 * @param key
	 */
	void removeAnnotation(String key);

	/**
	 * Returns a collection of annotations which are set.
	 */
	Collection<String> getAnnotations();
}
