package org.sidiff.common.emf.metrics.defaults;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.emf.metrics.IMetricValueAcceptor;

/**
 * An abstract metric class which computes a value for all objects
 * or for all root objects, in a resource, that have a given type.
 * @author rmueller
 * @param <T> the type of the object to compute the metric for
 */
public abstract class AbstractModelElementMetric<T extends EObject> extends AbstractMetric {

	private final Class<T> elementType;
	private final boolean rootOnly;

	/**
	 * Creates a new abstract model element metric.
	 * @param elementType the type of element for which the metric is calculated
	 * @param rootOnly <code>true</code> to calculate only for root elements (of specified type) of the resource,
	 * <code>false</code> to calculate for all elements (of specified type)
	 */
	public AbstractModelElementMetric(Class<T> elementType, boolean rootOnly) {
		this.elementType = Objects.requireNonNull(elementType);
		this.rootOnly = rootOnly;
	}

	@Override
	public void calculate(Resource resource, IMetricValueAcceptor acceptor, IProgressMonitor monitor) {
		(rootOnly ? resource.getContents().stream() : CollectionUtil.asStream(resource.getAllContents()))
			.filter(elementType::isInstance)
			.map(elementType::cast)
			.map(this::calculate)
			.forEach(acceptor::accept);
	}

	/**
	 * Computes the value for the given model element.
	 * @param element the model element
	 * @return value computed for this object
	 */
	public abstract Object calculate(T element);
}
