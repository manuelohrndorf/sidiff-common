package org.sidiff.common.emf.metrics.defaults.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.metrics.IMetricValueAcceptor;
import org.sidiff.common.emf.metrics.defaults.AbstractResourceMetric;

public class NumberOfResourceErrors extends AbstractResourceMetric {

	@Override
	protected void doCalculate(Resource context, IMetricValueAcceptor acceptor, IProgressMonitor monitor) {
		acceptor.accept(context.getErrors().size());
	}
}
