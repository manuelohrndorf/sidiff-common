package org.sidiff.common.emf.ui.handlers;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;

/**
 * @author rmueller
 */
public class AddXmiIdsHandler extends AbstractXmiIdUpdatingHandler {

	@Override
	protected void updateXmiIds(EObject eObject, XMLResource xmlResource) {
		if (xmlResource.getID(eObject) == null) {
			xmlResource.setID(eObject, EcoreUtil.generateUUID());
		}
	}
}
