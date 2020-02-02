package org.sidiff.common.emf.copiers;

import org.sidiff.common.emf.EMFAdapter;
import org.sidiff.common.emf.annotation.AnnotateableElement;

/**
 * Extends the {@link XmiIdCopier} to also copy all annotations using {@link AnnotateableElement}.
 * @author dreuling
 */
public class AnnotationCopier extends XmiIdCopier {

	private static final long serialVersionUID = -8772023217222753459L;

	/**
	 * <h1>AnnotationCopier</h1>
	 * <p>Copies all annotations of all {@link AnnotateableElement} adapters
	 * of the original to the copies</p>
	 * <hr>
	 * {@inheritDoc}
	 */
	@Override
	public void copyReferences() {
		super.copyReferences();
		copyAnnotations();
	}

	protected void copyAnnotations() {
		forEach((original, copy) -> {
			AnnotateableElement annotElem = EMFAdapter.INSTANCE.adapt(original, AnnotateableElement.class);
			AnnotateableElement annotElemCopy = EMFAdapter.INSTANCE.adapt(copy, AnnotateableElement.class);

			for(String annotation : annotElem.getAnnotations()) {
				Object value = annotElem.getAnnotation(annotation, Object.class);
				annotElemCopy.setAnnotation(annotation, value);
			}
		});
	}
}
