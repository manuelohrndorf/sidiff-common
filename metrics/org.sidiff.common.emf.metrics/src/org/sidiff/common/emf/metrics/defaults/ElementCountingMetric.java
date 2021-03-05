package org.sidiff.common.emf.metrics.defaults;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.emf.metrics.IMetricValueAcceptor;

/**
 * @author cpietsch
 */
public class ElementCountingMetric extends AbstractResourceMetric {

	@Override
	protected void doCalculate(Resource context, IMetricValueAcceptor acceptor, IProgressMonitor monitor) {
		acceptor.accept(
			CollectionUtil.asStream(EcoreUtil.getAllProperContents(context, true)).count());
	}
}
