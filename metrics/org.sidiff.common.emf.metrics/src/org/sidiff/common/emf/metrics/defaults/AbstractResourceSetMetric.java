package org.sidiff.common.emf.metrics.defaults;

import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * An abstract metric class which computes a value for resource sets.
 * @author rmueller
 */
public abstract class AbstractResourceSetMetric extends AbstractMetric<ResourceSet> {

	public AbstractResourceSetMetric() {
		super(ResourceSet.class);
	}
}
