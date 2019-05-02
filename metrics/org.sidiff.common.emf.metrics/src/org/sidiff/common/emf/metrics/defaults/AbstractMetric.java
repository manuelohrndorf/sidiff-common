package org.sidiff.common.emf.metrics.defaults;

import org.sidiff.common.emf.metrics.IMetric;
import org.sidiff.common.extension.AbstractTypedExtension;

/**
 * Abstract metrics class which extends {@link AbstractTypedExtension} and implements
 * {@link IMetric}, such that the name, key, description and document types of the
 * metric are derived from the extension element in the plugin manifest.
 * @author rmueller
 */
public abstract class AbstractMetric extends AbstractTypedExtension implements IMetric {

}
