package org.sidiff.common.emf.input.adapter;

import java.util.Set;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.sidiff.common.extension.ITypedExtension;
import org.sidiff.common.extension.configuration.IConfigurableExtension;

/**
 * A model adapter converts between a proprietary file format and an EMF model based one.
 * @author rmueller
 */
public interface IModelAdapter extends ITypedExtension, IConfigurableExtension {

	Description<IModelAdapter> DESCRIPTION = Description.of(IModelAdapter.class,
			"org.sidiff.common.emf.input.modelAdapters", "modelAdapter", "class");
	ModelAdapterManager MANAGER = new ModelAdapterManager(DESCRIPTION);

	/**
	 * Converts from a model file format to a proprietary format.
	 * @param inputModel the input model
	 * @param outputFile file to write proprietary output to
	 * @throws CoreException if the adapter failed for any reason
	 */
	void toProprietary(Resource inputModel, IFile outputFile) throws CoreException;

	/**
	 * Converts from a model file format to a proprietary format.
	 * @param inputModel the input model
	 * @return the proprietary output as a string
	 * @throws CoreException if the adapter failed for any reason
	 */
	String toProprietary(Resource inputModel) throws CoreException;

	/**
	 * Converts from a model file format to a proprietary format.
	 * The file name of the output is based on the file name of the input.
	 * @param inputModel the input model
	 * @param outputFolder folder to write proprietary output to
	 * @throws CoreException if the adapter failed for any reason
	 */
	IFile toProprietary(Resource inputModel, IContainer outputFolder) throws CoreException;

	/**
	 * Converts from a proprietary format to a model file format.
	 * @param inputFile the proprietary input file
	 * @param outputModel resource to write model output to
	 * @throws CoreException if the adapter failed for any reason
	 */
	void toModel(IFile inputFile, Resource outputModel) throws CoreException;

	/**
	 * Converts from a proprietary format to a model file format.
	 * The file name of the output is based on the file name of the input.
	 * @param inputFile the proprietary input file
	 * @param outputResourceSet resource set in which to save the resource
	 * @param outputFolder folder to save resource in
	 * @return the resource in which the model was saved
	 * @throws CoreException if the adapter failed for any reason
	 */
	Resource toModel(IFile inputFile, ResourceSet outputResourceSet, URI outputFolder) throws CoreException;

	/**
	 * @return the file extensions of <i>proprietary</i> files, which this adapter can handle
	 */
	Set<String> getProprietaryFileExtensions();

	/**
	 * @return the file extensions of <i>model</i> files, which this adapter can handle
	 */
	Set<String> getModelFileExtensions();

	/**
	 * {@inheritDoc}
	 * <p>These are document types of <i>model</i> files, which this adapter can handle.</p>
	 */
	@Override
	Set<String> getDocumentTypes();
}
