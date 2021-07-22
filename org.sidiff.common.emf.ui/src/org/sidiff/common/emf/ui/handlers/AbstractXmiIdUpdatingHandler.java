package org.sidiff.common.emf.ui.handlers;

import java.io.IOException;

import org.eclipse.core.commands.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author rmueller
 */
public abstract class AbstractXmiIdUpdatingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		Assert.isLegal(selection.size() == 1);
		Assert.isLegal(selection.getFirstElement() instanceof XMLResource, "The selected file is no XMLResource");
		XMLResource xmlResource = (XMLResource)selection.getFirstElement();
		xmlResource.getAllContents()
			.forEachRemaining(eObject -> updateXmiIds(eObject, xmlResource));
		try {
			xmlResource.save(null);
		} catch (IOException e) {
			throw new ExecutionException("Could not save resource", e);
		}
		return null;
	}

	protected abstract void updateXmiIds(EObject eObject, XMLResource xmlResource);
}
