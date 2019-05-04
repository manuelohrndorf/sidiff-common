package org.sidiff.common.emf.metrics.defaults;

import java.util.Objects;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.Notifier;
import org.sidiff.common.emf.metrics.IMetric;
import org.sidiff.common.emf.metrics.IMetricValueAcceptor;
import org.sidiff.common.extension.AbstractTypedExtension;

/**
 * <p>Abstract metrics class which extends {@link AbstractTypedExtension} and implements
 * {@link IMetric}, such that the name, key, description and document types of the
 * metric are derived from the extension element in the plugin manifest.</p>
 * <p>Also adds a type argument for the context, and a type-safe
 * {@link #doCalculate(T, IMetricValueAcceptor, IProgressMonitor)} method.</p>
 * @author rmueller
 */
public abstract class AbstractMetric<T extends Notifier> extends AbstractTypedExtension implements IMetric {

	private final Class<T> contextType;

	public AbstractMetric(Class<T> contextType) {
		this.contextType = Objects.requireNonNull(contextType);
	}

	@Override
	public void calculate(Notifier context, IMetricValueAcceptor acceptor, IProgressMonitor monitor) {
		Assert.isTrue(getContextType().isInstance(context), "Context is not compatible with context type");
		doCalculate(getContextType().cast(context), acceptor, monitor);
	}

	@Override
	public Class<T> getContextType() {
		return contextType;
	}

	protected abstract void doCalculate(T context, IMetricValueAcceptor acceptor, IProgressMonitor monitor);
}
