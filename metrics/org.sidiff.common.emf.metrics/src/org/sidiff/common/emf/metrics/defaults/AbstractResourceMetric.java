package org.sidiff.common.emf.metrics.defaults;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * An abstract metric class which computes a value for resources.
 * @author rmueller
 */
public abstract class AbstractResourceMetric extends AbstractMetric<Resource> {

	public AbstractResourceMetric() {
		super(Resource.class);
	}
}
