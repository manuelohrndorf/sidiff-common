package org.sidiff.common.emf.ui.handlers;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class GenerateXmiIdsHandler extends AbstractXmiIdUpdatingHandler {

	@Override
	protected void updateXmiIds(EObject eObject, XMLResource xmlResource) {
		xmlResource.setID(eObject, EcoreUtil.generateUUID());
	}
}
