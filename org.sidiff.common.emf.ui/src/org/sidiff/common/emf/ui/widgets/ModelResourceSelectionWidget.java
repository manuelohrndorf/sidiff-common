package org.sidiff.common.emf.ui.widgets;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.provider.EcoreEditPlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.sidiff.common.emf.modelstorage.EMFStorage;
import org.sidiff.common.emf.modelstorage.SiDiffResourceSet;

/**
 * A {@link FileSelectionWidget} which loads the file as a {@link Resource}.
 * @author rmueller
 */
public class ModelResourceSelectionWidget extends FileSelectionWidget<Resource> {

	public ModelResourceSelectionWidget() {
		super(new ResourceModelLoader());
		setTitle("Model resource");
		setLowerUpperBounds(1, 1);
		setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Resource)element).getURI().toString();
			}
			@Override
			public Image getImage(Object element) {
				return ExtendedImageRegistry.getInstance().getImage(
						EcoreEditPlugin.INSTANCE.getImage("full/obj16/EObject"));
			}
		});
	}

	protected static class ResourceModelLoader implements IModelLoader<Resource> {

		private SiDiffResourceSet resourceSet = SiDiffResourceSet.create();

		@Override
		public Resource load(IFile file) throws Exception {
			return resourceSet.getResource(EMFStorage.toPlatformURI(file), true);
		}
	}
}
