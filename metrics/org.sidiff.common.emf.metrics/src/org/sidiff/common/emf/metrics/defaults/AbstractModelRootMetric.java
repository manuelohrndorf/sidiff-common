package org.sidiff.common.emf.metrics.defaults;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.metrics.IMetricValueAcceptor;

/**
 * An abstract metric class which computes a value for all root objects
 * in a resource that have a given type.
 * @author rmueller
 * @param <T> the type of the root object
 */
public abstract class AbstractModelRootMetric<T extends EObject> extends AbstractMetric {

	private final Class<T> rootType;

	public AbstractModelRootMetric(Class<T> rootType) {
		this.rootType = Objects.requireNonNull(rootType);
	}

	@Override
	public void calculate(Resource resource, IMetricValueAcceptor acceptor, IProgressMonitor monitor) {
		resource.getContents().stream()
			.filter(rootType::isInstance)
			.map(rootType::cast)
			.forEach(root -> acceptor.accept(calculate(root)));
	}

	/**
	 * Computes the value for the given root object.
	 * @param modelRoot the root object
	 * @return value computes for this object
	 */
	public abstract Object calculate(T modelRoot);
}
