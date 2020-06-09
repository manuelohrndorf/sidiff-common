package org.sidiff.common.emf.input.adapter;

import org.eclipse.core.resources.IFile;
import org.sidiff.common.extension.IExtension.Description;
import org.sidiff.common.extension.storage.NoExtensionManagerStorage;
import org.sidiff.common.extension.TypedExtensionManager;

public class ModelAdapterManager extends TypedExtensionManager<IModelAdapter> {

	public ModelAdapterManager(Description<? extends IModelAdapter> description) {
		super(new NoExtensionManagerStorage<>(description));
	}

	public boolean isAdaptableProprietaryFile(IFile file) {
		return getExtensions().stream()
					.anyMatch(adapter -> adapter.getProprietaryFileExtensions().contains(file.getFileExtension()));
	}

	public boolean isAdaptableModelFile(IFile file) {
		return getExtensions().stream()
					.anyMatch(adapter -> adapter.getModelFileExtensions().contains(file.getFileExtension()));
	}
}
