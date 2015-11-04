package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.sidiff.common.emf.EMFAdapter;
import org.sidiff.common.emf.annotation.AnnotateableElement;
import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.util.ReflectionUtil;
import org.sidiff.core.annotation.Annotator;

/**
 * Dieser Annotator kann Werte aus anderen Annotationen kopieren oder verschieben. Ausserdem kann er
 * Konstanten an die Modellelemente annotieren.
 * 
 * @author wenzel
 *
 */
public class SetAttributeAnnotator extends Annotator {

	public SetAttributeAnnotator(EPackage documentType, String annotationKey, String parameter, EClass acceptedType, Collection<String> requiredAnnotations) {
		super(documentType, annotationKey, parameter.trim(), acceptedType, requiredAnnotations, ExecutionOrder.PRE);
	}

	public static final String SET = "set:";
	public static final String COPY = "copy:";
	public static final String MOVE = "move:";
	public static final String CREATE = "create:";

	@Override
	protected Object computeAnnotationValue(EObject object) {
		if (getParameter().startsWith(SET)) {
			return getParameter().substring(4);
		} else if (getParameter().startsWith(COPY)) {
			return EMFAdapter.INSTANCE.adapt(object, AnnotateableElement.class).getAnnotation(getParameter().substring(5), Object.class);
		} else if (getParameter().startsWith(MOVE)) {
			AnnotateableElement ae = EMFAdapter.INSTANCE.adapt(object, AnnotateableElement.class);
			Object obj = ae.getAnnotation(getParameter().substring(5), Object.class);
			ae.removeAnnotation(getParameter().substring(5));
			return obj;
		} else if (getParameter().startsWith(CREATE)) {
			String className = getParameter().substring(7);
			Object createdObject = ReflectionUtil.createInstance(className, Object.class);
			return createdObject;
		} else {
			throw new SiDiffRuntimeException("Paramter " + getParameter() + " not allowed. set:, copy: or move:required.");
		}
	}

}
