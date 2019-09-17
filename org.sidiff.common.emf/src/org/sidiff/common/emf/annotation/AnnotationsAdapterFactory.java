package org.sidiff.common.emf.annotation;

import org.eclipse.emf.common.notify.Notifier;
import org.sidiff.common.emf.adapters.SiDiffAdapterFactory;
import org.sidiff.common.emf.annotation.internal.AnnotateableElementImpl;

/**
 * Adapter factory for creating annotateable element adapters.
 * @author wenzel
 *
 */
public class AnnotationsAdapterFactory extends SiDiffAdapterFactory {

	public AnnotationsAdapterFactory() {
		super(AnnotateableElement.class);
	}

	@Override
	public AnnotateableElement createAdapter(Notifier target) {
		return new AnnotateableElementImpl();
	}
}
