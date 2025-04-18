package org.sidiff.common.emf.metrics.defaults;

import java.util.Objects;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.Notifier;
import org.sidiff.common.emf.metrics.ComparisonType;
import org.sidiff.common.emf.metrics.IMetric;
import org.sidiff.common.emf.metrics.IMetricValueAcceptor;
import org.sidiff.common.extension.AbstractTypedExtension;

/**
 * <p>Abstract metrics class which extends {@link AbstractTypedExtension} and implements
 * {@link IMetric}, such that the name, key, description, document types and comparison type of the
 * metric are derived from the extension element in the plugin manifest.</p>
 * <p>Also adds a type argument for the context, and a type-safe
 * {@link #doCalculate(T, IMetricValueAcceptor, IProgressMonitor)} method.</p>
 * @author rmueller
 */
public abstract class AbstractMetric<T extends Notifier> extends AbstractTypedExtension implements IMetric {

	public static final String ATTRIBUTE_COMPARISON_TYPE = "comparisonType";

	private final Class<T> contextType;
	private ComparisonType comparisonType = ComparisonType.UNSPECIFIED;

	/**
	 * Creates an AbstractMetric.
	 * @param contextType the type of notifier this metric supports
	 */
	public AbstractMetric(Class<T> contextType) {
		this.contextType = Objects.requireNonNull(contextType);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		super.setInitializationData(config, propertyName, data);
		String comparisonTypeString = config.getAttribute(ATTRIBUTE_COMPARISON_TYPE);
		if(comparisonTypeString != null) {
			try {
				this.comparisonType = ComparisonType.valueOf(comparisonTypeString);				
			} catch(IllegalArgumentException e) {
				this.comparisonType = ComparisonType.UNSPECIFIED;
			}
		}
	}

	/**
	 * Implementation of AbstractMetric checks type of context and
	 * calls {@link #doCalculate(T, IMetricValueAcceptor, IProgressMonitor)}
	 * only if the given context is compatible with {@link #getContextType()}.
	 */
	@Override
	public final void calculate(Notifier context, IMetricValueAcceptor acceptor, IProgressMonitor monitor) {
		Assert.isTrue(getContextType().isInstance(context), "Context is not compatible with context type");
		doCalculate(getContextType().cast(context), acceptor, monitor);
	}

	@Override
	public final Class<T> getContextType() {
		return contextType;
	}
	
	@Override
	public final ComparisonType getComparisonType() {
		return comparisonType;
	}

	/**
	 * Performs the calculation on the type checked context object.
	 * @param context the context, is instance of {@link #getContextType()}
	 * @param acceptor an acceptor for the resulting metric value/s
	 * @param monitor a progress monitor
	 */
	protected abstract void doCalculate(T context, IMetricValueAcceptor acceptor, IProgressMonitor monitor);
}
