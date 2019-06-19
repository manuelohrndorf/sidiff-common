package org.sidiff.common.extension.ui.labelprovider;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.sidiff.common.extension.IExtension;
import org.sidiff.common.extension.ui.internal.ExtensionsUiPlugin;

/**
 * A {@link ColumnLabelProvider} for {@link IExtension}s,
 * using the extension's name as text, description as tooltip text,
 * and an some image.
 * @author Robert Müller
 */
public class ExtensionLabelProvider extends ColumnLabelProvider {

	private Image extensionImage;

	@Override
	public String getText(Object element) {
		if(element instanceof IExtension) {
			return ((IExtension)element).getName();
		}
		return super.getText(element);
	}

	@Override
	public String getToolTipText(Object element) {
		if(element instanceof IExtension) {
			return ((IExtension)element).getDescription().orElse(null);
		}
		return super.getToolTipText(element);
	}

	@Override
	public Image getImage(Object element) {
		if(element instanceof IExtension) {
			if(extensionImage == null) {
				extensionImage = ExtensionsUiPlugin.getExtensionImageDescriptor().createImage();
			}
			return extensionImage;
		}
		return super.getImage(element);
	}

	@Override
	public void dispose() {
		super.dispose();
		if(extensionImage != null) {
			extensionImage.dispose();
			extensionImage = null;
		}
	}
}
