package org.sidiff.core.annotation.impl;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.common.emf.EMFAdapter;
import org.sidiff.common.emf.access.tree.TreeVisitor;
import org.sidiff.common.emf.annotation.AnnotateableElement;

public class RemoveAnnotationVisitor implements TreeVisitor {

	private Collection<String> revokedKeys = null;  
	
	public RemoveAnnotationVisitor(Collection<String> revokedKeys) {
		this.revokedKeys = revokedKeys;
	}
	
	@Override
	public void postExecute(EObject object) {
		
		AnnotateableElement annotated = EMFAdapter.INSTANCE.adapt(object, AnnotateableElement.class);

		for(String revokedKey : revokedKeys){
			annotated.removeAnnotation(revokedKey);
		}

	}

	@Override
	public boolean preExecute(EObject object) {
		
		return true;
	}

}
