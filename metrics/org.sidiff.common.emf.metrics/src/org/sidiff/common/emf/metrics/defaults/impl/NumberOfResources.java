package org.sidiff.common.emf.metrics.defaults.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.sidiff.common.emf.metrics.IMetricValueAcceptor;
import org.sidiff.common.emf.metrics.defaults.AbstractResourceSetMetric;

/**
 * @author rmueller
 */
public class NumberOfResources extends AbstractResourceSetMetric {

	@Override
	protected void doCalculate(ResourceSet context, IMetricValueAcceptor acceptor, IProgressMonitor monitor) {
		acceptor.accept(context.getResources().size());
	}
}
