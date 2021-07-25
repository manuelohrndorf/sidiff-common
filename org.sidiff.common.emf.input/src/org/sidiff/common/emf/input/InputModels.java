package org.sidiff.common.emf.input;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.sidiff.common.emf.EMFValidate;
import org.sidiff.common.emf.doctype.util.EMFDocumentTypeUtil;
import org.sidiff.common.emf.exceptions.InvalidModelException;
import org.sidiff.common.emf.input.adapter.IModelAdapter;
import org.sidiff.common.emf.modelstorage.EMFStorage;
import org.sidiff.common.emf.modelstorage.SiDiffResourceSet;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;

/**
 * A list of input models represented by corresponding {@link IFile} and {@link Resource}.
 * Use the {@link #builder()} to create instances of this class. Use the specific builder
 * of subclasses to create instances of subclasses when available, or use {@link #builder(Factory)}.
 */
public class InputModels {

	public static final String PLUGIN_ID = "org.sidiff.common.emf.input";

	private List<Resource> resources;
	private List<IFile> files;
	private List<String> labels;
	private SiDiffResourceSet resourceSet;
	private Set<String> documentTypes;
	private IProject project;

	/**
	 * Constructor for internal use. Use the {@link #builder()} to create
	 * InputModels.
	 *
	 * @param resourceSet
	 * @param resources
	 */
	protected InputModels(SiDiffResourceSet resourceSet, List<Resource> resources) {
		this.resources = resources;
		this.resourceSet = resourceSet;
		this.files = new ArrayList<>();
		for(Resource resource : resources) {
			Assert.isLegal(resource.getResourceSet() == resourceSet, "Resource must be contained in given resource set");
			files.add(Objects.requireNonNull(
					EMFStorage.toIFile(EMFStorage.getNormalizedURI(resource)),
					() -> "Could not derive IFile for Resource " + resource));
		}
	}

	/**
	 * Returns whether the resources have the same document types.
	 *
	 * @return <code>true</code> if all resources have the same set of document
	 *         types, <code>false</code> otherwise
	 */
	public boolean haveSameDocumentType() {
		Set<Set<String>> documentTypes = new HashSet<>();
		for (Resource resource : resources) {
			documentTypes.add(new HashSet<>(EMFDocumentTypeUtil.resolve(resource)));
		}
		return documentTypes.size() == 1;
	}

	/**
	 * Returns all document types between input models. This is the union of all
	 * resource document types.
	 *
	 * @return unmodifiable set of the document types of all input models
	 */
	public Set<String> getDocumentTypes() {
		if (documentTypes == null) {
			documentTypes = new HashSet<>(EMFDocumentTypeUtil.resolve(resources));
		}
		return Collections.unmodifiableSet(documentTypes);
	}

	public boolean haveSameFileExtension() {
		Set<String> extensions = new HashSet<>();
		for(IFile file : files) {
			if(!extensions.add(file.getFileExtension()) && extensions.size() > 1) {
				return false;
			}
		}
		return true;
	}

	public String getFileExtension() {
		return files.stream().map(IFile::getFileExtension).findFirst().orElse(null);
	}

	public int getNumModels() {
		return resources.size();
	}

	/**
	 * @return unmodifiable list of the resources
	 */
	public List<Resource> getResources() {
		return Collections.unmodifiableList(resources);
	}

	/**
	 * @return unmodifiable list of the files
	 */
	public List<IFile> getFiles() {
		return Collections.unmodifiableList(files);
	}

	/**
	 * Returns labels to be displayed for all files. The label of a file is it's
	 * name. If multiple labels are equal, they are made unique by prepending the
	 * file's parent's name.
	 *
	 * @return labels for the files
	 */
	public List<String> getLabels() {
		if (labels == null) {
			initLabels();
		}
		return Collections.unmodifiableList(labels);
	}

	/**
	 * Returns the {@link IProject} that contains the input files. If the files are
	 * not in the same project, this the project of the first file is returned.
	 *
	 * @return project, <code>null</code> if resolution failed
	 */
	public IProject getProject() {
		if (project == null) {
			project = getProject(files);
		}
		return project;
	}

	public SiDiffResourceSet getResourceSet() {
		return resourceSet;
	}

	/**
	 * Swaps the two input models, asserts that there are exactly two input models.
	 */
	public void swap() {
		Assert.isLegal(resources.size() == 2, "There must be exactly two input models to swap them.");
		swap(0, 1);
	}

	public void swap(int index1, int index2) {
		Collections.swap(resources, index1, index2);
		Collections.swap(files, index1, index2);
		Collections.swap(labels, index1, index2);
	}

	protected void initLabels() {
		labels = new ArrayList<>();
		List<IContainer> parents = new ArrayList<>(files.size());
		for (IFile file : files) {
			labels.add(file.getName());
			parents.add(file.getParent());
		}

		// by creating a set, we can check for duplicate labels
		while (new HashSet<>(labels).size() < labels.size()) {
			// update each label by prepending the parent's name
			boolean noParents = true;
			for (int i = 0; i < parents.size(); i++) {
				IContainer parent = parents.get(i);
				if (parent != null) {
					noParents = false;
					labels.set(i, parent.getName() + File.separator + labels.get(i));
					parents.set(i, parent.getParent());
				}
			}

			// if no more parents are found, some labels might
			// still be duplicates, but there is nothing left to do
			if (noParents) {
				break;
			}
		}
	}

	@Override
	public String toString() {
		return "InputModels" + getLabels().toString();
	}

	protected static IProject getProject(Collection<IFile> files) {
		IProject project = null;
		for (IFile file : files) {
			if (file == null) {
				LogUtil.log(LogEvent.WARNING, "File of input model was not resolved.");
			} else if (project == null) {
				project = file.getProject();
			} else if (!project.equals(file.getProject())) {
				LogUtil.log(LogEvent.WARNING, "Input models are not in the same project. Using project of first one.");
				break;
			}
		}
		return project;
	}


	public static Builder<InputModels> builder() {
		return builder(InputModels::new);
	}

	public static <T extends InputModels> Builder<T> builder(Factory<T> factory) {
		return new Builder<>(factory);
	}

	public static class Builder<T extends InputModels> {

		private final Factory<T> factory;
		private final List<Object> models = new ArrayList<>(); // may be URI, Resource, IFile or File
		private SiDiffResourceSet resourceSet;

		private IModelAdapter modelAdapter;
		private IFolder outputFolder; // null if adapted models should not be saved

		private int minValidateSeverity = Diagnostic.CANCEL;
		private int assertedMinNumModels = 0;
		private int assertedMaxNumModels = Integer.MAX_VALUE;
		private boolean assertSameDocumentType = false;

		protected Builder(Factory<T> factory) {
			this.factory = Objects.requireNonNull(factory);
		}

		public Builder<T> setResourceSet(SiDiffResourceSet resourceSet) {
			this.resourceSet = Objects.requireNonNull(resourceSet);
			return this;
		}

		protected SiDiffResourceSet getResourceSet() {
			return resourceSet;
		}

		/**
		 * Sets the {@link IModelAdapter} for this builder.
		 * @param modelAdapter the model adapter, <code>null</code> to disable (default)
		 * @param outputFolder folder for model adapter output, <code>null</code> to disable saving adapted models
		 * @return this builder
		 */
		public Builder<T> setModelAdapter(IModelAdapter modelAdapter, IFolder outputFolder) {
			this.modelAdapter = modelAdapter; // null allowed for convenience
			this.outputFolder = modelAdapter == null ? null : outputFolder;
			return this;
		}

		protected IModelAdapter getModelAdapter() {
			return modelAdapter;
		}

		/**
		 * When loading models, validate them using {@link EMFValidate} with the given minimum severity.
		 * The value {@link Diagnostic#CANCEL} represents disabled validation.
		 * @param minValidateSeverity minimum severity
		 * @return this builder
		 */
		public Builder<T> validate(int minValidateSeverity) {
			this.minValidateSeverity = minValidateSeverity;
			return this;
		}

		protected int getMinValidateSeverity() {
			return minValidateSeverity;
		}

		/**
		 * When building, asserts that exactly the given number of input models have been added.
		 * @param exactNum the asserted number of models
		 * @return this builder
		 */
		public Builder<T> assertNumModels(int exactNum) {
			return assertNumModels(exactNum, exactNum);
		}

		/**
		 * When building, asserts that exactly the given number of input models have been added.
		 * @param min minimum number of models (inclusive)
		 * @param max maximum number of models (inclusive)
		 * @return this builder
		 */
		public Builder<T> assertNumModels(int min, int max) {
			this.assertedMinNumModels = min;
			this.assertedMaxNumModels = max;
			return this;
		}

		protected int getAssertedMinNumModels() {
			return assertedMinNumModels;
		}

		protected int getAssertedMaxNumModels() {
			return assertedMaxNumModels;
		}

		/**
		 * Sets that when building, it is asserted that all input models have the same document type.
		 * @param assertSameDocumentType <code>true</code> to assert, <code>false</code> otherwise
		 * @return this builder
		 */
		public Builder<T> assertSameDocumentType(boolean assertSameDocumentType) {
			this.assertSameDocumentType = assertSameDocumentType;
			return this;
		}

		protected boolean isAssertSameDocumentType() {
			return assertSameDocumentType;
		}

		public Builder<T> addModel(URI modelUri) {
			models.add(Objects.requireNonNull(modelUri));
			return this;
		}

		public Builder<T> addModel(Resource resource) {
			models.add(Objects.requireNonNull(resource));
			return this;
		}

		public Builder<T> addModel(IFile modelFile) {
			models.add(Objects.requireNonNull(modelFile));
			return this;
		}

		public Builder<T> addModel(File modelFile) {
			models.add(Objects.requireNonNull(modelFile));
			return this;
		}

		public Builder<T> addModels(IContainer container, Predicate<IResource> resourceSelector) throws InputModelsException {
			if(!container.exists()) {
				throw new InputModelsException("The container does not exist: " + container);
			}
			try {
				for(IResource member : container.members()) {
					if(resourceSelector.test(member)) {
						if(member instanceof IFolder) {
							addModels((IFolder)member, resourceSelector);
						} else if(member instanceof IFile) {
							addModel((IFile)member);
						}
					}
				}
				return this;
			} catch (CoreException e) {
				throw new InputModelsException("Failed to list members of container: " + container, e);
			}
		}

		public Builder<T> addModels(ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				Stream.of(((IStructuredSelection)selection).toArray())
					.filter(e -> e instanceof Resource || e instanceof URI || e instanceof IFile || e instanceof File)
					.forEach(models::add);
			}
			return this;
		}

		public T build(IProgressMonitor monitor) throws InputModelsException {
			SubMonitor progress = SubMonitor.convert(monitor, models.size()+1+1);

			initDefaults();
			assertValidInput();
			progress.worked(1);

			List<Resource> resources = new ArrayList<>(models.size());
			for (Object model : models) {
				Resource resource = deriveResource(model);
				if (resource != null) {
					validateResource(resource);
					resources.add(resource);
				}
				progress.worked(1);
			}

			T inputModels = factory.createInputModels(resourceSet, resources);
			assertValidResult(inputModels);
			progress.worked(1);
			return inputModels;
		}

		public T build() throws InputModelsException {
			return build(new NullProgressMonitor());
		}

		protected void initDefaults() {
			if (resourceSet == null) {
				resourceSet = SiDiffResourceSet.create();
			}
		}

		protected void assertValidInput() throws InputModelsException {
			int numModels = models.size();
			if(numModels < assertedMinNumModels) {
				throw new InputModelsException("At least " + assertedMinNumModels
						+ " input models are required, but " + numModels + " given.");
			}
			if(numModels > assertedMaxNumModels) {
				throw new InputModelsException("At most " + assertedMaxNumModels
						+ " input models are supported, but " + numModels + " given.");
			}
		}

		protected void assertValidResult(InputModels inputModels) throws InputModelsException {
			if(assertSameDocumentType) {
				if(!inputModels.haveSameDocumentType()) {
					throw new InputModelsException("The input models must have the same document types.");
				}
			}
		}

		protected Resource deriveResource(Object model) throws InputModelsException {
			if (model instanceof Resource) {
				Resource resource = (Resource)model;
				if(!resourceSet.getResources().contains(model)) {
					resourceSet.getResources().add(resource);
				}
				return resource;
			} else if (model instanceof URI) {
				URI uri = (URI)model;
				if(modelAdapter != null) {
					if(modelAdapter.getProprietaryFileExtensions().contains(uri.fileExtension())) {
						try {
							Resource adaptedModel =
								modelAdapter.toModel(
									EMFStorage.toIFile(uri),
									resourceSet,
									outputFolder == null ? uri.trimSegments(1) : EMFStorage.toPlatformURI(outputFolder));
							if(outputFolder != null) {
								resourceSet.saveResource(adaptedModel);
							}
							return adaptedModel;
						} catch(Exception e) {
							throw new InputModelsException("Failed to load model '" + model + "' using " + modelAdapter.getName(), e);
						}
					}
					if(modelAdapter.getModelFileExtensions().contains(uri.fileExtension())) {
						return null; // ignore derived model files here
					}
				}
				try {
					return resourceSet.getResource(uri, true);
				} catch(Exception e) {
					throw new InputModelsException("Failed to load model '" + model + "'", e);
				}
			} else if (model instanceof IFile) {
				return deriveResource(EMFStorage.toPlatformURI((IFile)model));
			} else if (model instanceof File) {
				return deriveResource(EMFStorage.toFileURI((File)model));
			}
			throw new InputModelsException("Model type not supported: " + model);
		}

		protected void validateResource(Resource model) throws InputModelsException {
			if(minValidateSeverity >= Diagnostic.WARNING && minValidateSeverity <= Diagnostic.ERROR) {
				try {
					new EMFValidate(minValidateSeverity).validateModel(model);
				} catch (InvalidModelException e) {
					throw new InputModelsException("The model is invalid: " + model, e);
				}
			}
		}
	}

	@FunctionalInterface
	public interface Factory<T extends InputModels> {
		T createInputModels(SiDiffResourceSet resourceSet, List<Resource> resources);
	}
}
