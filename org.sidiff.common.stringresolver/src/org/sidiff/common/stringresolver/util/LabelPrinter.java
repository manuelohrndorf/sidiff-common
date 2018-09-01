package org.sidiff.common.stringresolver.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.common.stringresolver.GenericStringResolver;
import org.sidiff.common.stringresolver.IStringResolver;

public class LabelPrinter {

	private final IStringResolver resolver;

	public LabelPrinter() {
		this.resolver = new GenericStringResolver();
	}

	public LabelPrinter(Resource model) {
		this(EMFModelAccess.getCharacteristicDocumentType(model));
	}

	public LabelPrinter(String docType) {
		this.resolver = IStringResolver.MANAGER.getDefaultExtension(docType).orElseGet(GenericStringResolver::new);
	}

	public String getLabel(Object object){
		if (object != null) {
			return resolver.resolve((EObject) object);
		}
		return "null";
	}
	
	public String getToolTipLabel(Object object){
		if (object != null) {
			return resolver.resolveQualified((EObject) object);
		}
		return "null";
	}
}
