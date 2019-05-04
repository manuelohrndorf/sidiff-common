package org.sidiff.common.emf.metrics.defaults;

import org.eclipse.emf.ecore.EObject;

/**
 * An abstract metric class which computes a value for
 * all {@link EObject}s in a resource that have a given type.
 * @author rmueller
 * @param <T> the type of the object to compute the metric for
 */
public abstract class AbstractModelElementMetric<T extends EObject> extends AbstractMetric<T> {

	public AbstractModelElementMetric(Class<T> elementType) {
		super(elementType);
	}
}
