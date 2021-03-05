package org.sidiff.common.emf.metrics.defaults.impl;

import java.util.Collections;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.emf.metrics.IMetricValueAcceptor;
import org.sidiff.common.emf.metrics.defaults.AbstractResourceMetric;

/**
 * @author rmueller
 */
public class TypeCounting extends AbstractResourceMetric {

	@Override
	protected void doCalculate(Resource context, IMetricValueAcceptor acceptor, IProgressMonitor monitor) {
		CollectionUtil.asStream(EcoreUtil.<EObject>getAllProperContents(context, true))
			.collect(Collectors.groupingBy(EObject::eClass, Collectors.counting()))
			.entrySet().stream().forEach(entry -> acceptor.accept(Collections.singleton(entry.getKey()), entry.getValue()));
	}
}
