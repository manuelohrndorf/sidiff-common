package org.sidiff.common.emf.ui.labelprovider;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.provider.EcoreEditPlugin;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

public class EPackageLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		if(element instanceof EPackage) {
			EPackage ePackage = (EPackage)element;
			return ePackage.getName() + " [" + ePackage.getNsURI() + "]";
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if(element instanceof EPackage) {
			return ExtendedImageRegistry.getInstance().getImage(
					EcoreEditPlugin.INSTANCE.getImage("full/obj16/EPackage"));
		}
		return super.getImage(element);
	}
}
