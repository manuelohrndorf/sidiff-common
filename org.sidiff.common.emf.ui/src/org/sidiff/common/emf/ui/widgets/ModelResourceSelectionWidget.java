package org.sidiff.common.emf.ui.widgets;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.sidiff.common.emf.modelstorage.EMFStorage;
import org.sidiff.common.emf.modelstorage.SiDiffResourceSet;

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
