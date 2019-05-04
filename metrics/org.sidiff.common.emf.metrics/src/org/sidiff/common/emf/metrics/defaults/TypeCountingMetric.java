package org.sidiff.common.emf.metrics.defaults;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.emf.metrics.IMetricValueAcceptor;
import org.sidiff.common.exceptions.ExceptionUtil;

/**
 * Metric which counts the number of objects in the resource
 * that have a given type. The fully qualified class name must be
 * specified as data in the plugin manifest. Note that the plugin
 * containing the class must be imported by the plugin declaring the extension.
 * Example for class attribute:
 * <pre>org.sidiff.common.emf.metrics.defaults.TypeCountingMetric:org.eclipse.emf.ecore.EClass</pre>
 * @author rmueller
 */
public class TypeCountingMetric extends AbstractResourceMetric {

	private Class<?> countedType;

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		super.setInitializationData(config, propertyName, data);

		if(!(data instanceof String)) {
			throw new IllegalArgumentException("Illegal data for TypeCountingMetricFactory: " + data);
		}
		try {
			countedType = Platform.getBundle(config.getContributor().getName()).loadClass((String)data);
		} catch (ClassNotFoundException e) {
			throw ExceptionUtil.asCoreException(e);
		}
	}

	@Override
	protected void doCalculate(Resource context, IMetricValueAcceptor acceptor, IProgressMonitor monitor) {
		acceptor.accept(
			CollectionUtil.asStream(EcoreUtil.getAllProperContents(context, true))
				.filter(countedType::isInstance).count());
	}
}
