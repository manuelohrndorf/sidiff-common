package org.sidiff.common.emf.modelstorage;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class EMFHandlerUtil {
	
	public static Resource getSelection(ExecutionEvent event, ResourceSet rss, int selectionIndex) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			if (selectionIndex < ((IStructuredSelection) selection).size()) {
				Object selected = ((IStructuredSelection) selection).toArray()[selectionIndex];
				
				if ((selected != null) && (selected instanceof IResource)) {
					URI uri = getURI((IResource) selected);
					return rss.getResource(uri, true);
				}
			}
		}
		
		return null;
	}

	public static Resource getSelection(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			Object selected = ((IStructuredSelection) selection).getFirstElement();
			
			if ((selected != null) && (selected instanceof IResource)) {
				ResourceSet rss = new ResourceSetImpl();
				URI uri = getURI((IResource) selected);
				return rss.getResource(uri, true);
			}
		}
		
		return null;
	}
	
	public static URI getSelectionURI(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			Object selected = ((IStructuredSelection) selection).getFirstElement();
			
			if ((selected != null) && (selected instanceof IResource)) {
				return getURI((IResource) selected);
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends EObject> E getSelection(ExecutionEvent event, Class<E> type, ResourceSet rss, int selectionIndex) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			if (selectionIndex < ((IStructuredSelection) selection).size()) {
				Object selected = ((IStructuredSelection) selection).toArray()[selectionIndex];

				if ((selected != null) && (selected instanceof IResource)) {
					URI uri = getURI((IResource) selected);
					Resource resource = rss.getResource(uri, true);

					if ((resource != null) && !resource.getContents().isEmpty() 
							&& (type.isInstance(resource.getContents().get(0)))) {

						return (E) resource.getContents().get(0);
					}
				}
			}
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends EObject> E getSelection(ExecutionEvent event, Class<E> type) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			Object selected = ((IStructuredSelection) selection).getFirstElement();
			
			if ((selected != null) && (selected instanceof IResource)) {
				ResourceSet rss = new ResourceSetImpl();
				URI uri = getURI((IResource) selected);
				Resource resource = rss.getResource(uri, true);
				
				if ((resource != null) && !resource.getContents().isEmpty() 
						&& (type.isInstance(resource.getContents().get(0)))) {
					
					return (E) resource.getContents().get(0);
				}
			}
		}
		
		return null;
	}
	
	public static URI getURI(IResource workbenchResource) {

		String projectName = workbenchResource.getProject().getName();
		String filePath = workbenchResource.getProjectRelativePath().toOSString();
		String platformPath = projectName + "/" + filePath;

		return URI.createPlatformResourceURI(platformPath, true);
	}
}
