package org.sidiff.common.emf.stringresolver.util;

import java.util.Collections;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.common.emf.stringresolver.GenericStringResolver;
import org.sidiff.common.emf.stringresolver.IStringResolver;

/**
 * The label printer provides human readable labels
 * and tooltips using the available {@link IStringResolver}s.
 */
public class LabelPrinter {

	private final IStringResolver resolver;

	/**
	 * Creates a label printer using a generic string resolver.
	 */
	public LabelPrinter() {
		this.resolver = new GenericStringResolver();
	}

	/**
	 * Creates a label printer using the string resolver for the
	 * characteristic document type of the given model, or a
	 * generic string resolver if no alternative was found.
	 * @param model the model resource
	 */
	public LabelPrinter(Resource model) {
		this(EMFModelAccess.getCharacteristicDocumentType(model));
	}

	/**
	 * Creates a label printer using the string resolver for the
	 * given document type, or a generic string resolver if no
	 * alternative was found.
	 * @param docType the document type
	 */
	public LabelPrinter(String docType) {
		this.resolver = IStringResolver.MANAGER.getDefaultExtension(Collections.singleton(docType)).orElseGet(GenericStringResolver::new);
	}

	/**
	 * Returns a label for the given EObject.
	 * @param object the object
	 * @return label, "null" if null
	 */
	public String getLabel(EObject object){
		if (object != null) {
			return resolver.resolve(object);
		}
		return "null";
	}

	/**
	 * Returns a tooltip for the given EObject.
	 * @param object the object
	 * @return tooltip, "null" if null
	 */
	public String getToolTipLabel(EObject object){
		if (object != null) {
			return resolver.resolveQualified(object);
		}
		return "null";
	}
}
