package org.sidiff.common.emf.input.adapter;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.sidiff.common.extension.AbstractTypedExtension;

public abstract class AbstractModelAdapter extends AbstractTypedExtension implements IModelAdapter {

	private static final String ELEMENT_PROPRIETARY_FILE_EXTENSION = "proprietaryFileExtension";
	private static final String ELEMENT_MODEL_FILE_EXTENSION = "modelFileExtension";

	private Set<String> proprietaryFileExtensions;
	private Set<String> modelFileExtensions;

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {

		super.setInitializationData(config, propertyName, data);
		proprietaryFileExtensions = doGetChildElements(config, ELEMENT_PROPRIETARY_FILE_EXTENSION);
		modelFileExtensions = doGetChildElements(config, ELEMENT_MODEL_FILE_EXTENSION);
	}

	@Override
	public Set<String> getProprietaryFileExtensions() {
		return Collections.unmodifiableSet(proprietaryFileExtensions);
	}

	@Override
	public Set<String> getModelFileExtensions() {
		return Collections.unmodifiableSet(modelFileExtensions);
	}

	protected String getDefaultProprietaryFileExtension() {
		return proprietaryFileExtensions.stream().findFirst().orElse(null);
	}

	protected String getDefaultModelFileExtension() {
		return modelFileExtensions.stream().findFirst().orElse(null);
	}

	@Override
	public IFile toProprietary(Resource inputModel, IFolder outputFolder) throws CoreException {
		String name = inputModel.getURI().trimFileExtension().appendFileExtension(getDefaultProprietaryFileExtension()).lastSegment();
		IFile outputFile = outputFolder.getFile(name);
		toProprietary(inputModel, outputFile);
		return outputFile;
	}

	@Override
	public Resource toModel(IFile inputFile, ResourceSet outputResourceSet, URI outputFolder) throws CoreException {
		URI uri = outputFolder.appendSegment(inputFile.getName())
				.trimFileExtension().appendFileExtension(getDefaultModelFileExtension());
		Resource outputModel = outputResourceSet.createResource(uri);
		toModel(inputFile, outputModel);
		return outputModel;
	}
}
