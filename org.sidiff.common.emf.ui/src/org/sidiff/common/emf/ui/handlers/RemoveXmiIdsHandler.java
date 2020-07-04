package org.sidiff.common.emf.ui.handlers;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class RemoveXmiIdsHandler extends AbstractXmiIdUpdatingHandler {

	@Override
	protected void updateXmiIds(EObject eObject, XMLResource xmlResource) {
		xmlResource.setID(eObject, null);
	}
}
