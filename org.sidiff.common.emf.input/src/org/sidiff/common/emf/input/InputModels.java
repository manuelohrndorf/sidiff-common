package org.sidiff.common.emf.input;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.sidiff.common.emf.doctype.util.EMFDocumentTypeUtil;
import org.sidiff.common.emf.modelstorage.EMFStorage;
import org.sidiff.common.emf.modelstorage.SiDiffResourceSet;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;

/**
 * A list of input models represented by {@link IFile} and {@link Resource}.
 */
public class InputModels {

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
			documentTypes.add(new HashSet<String>(EMFDocumentTypeUtil.resolve(resource)));
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
		List<IContainer> parents = new ArrayList<IContainer>(files.size());
		for (IFile file : files) {
			labels.add(file.getName());
			parents.add(file.getParent());
		}

		// by creating a set, we can check for duplicate labels
		while (new HashSet<String>(labels).size() < labels.size()) {
			// update each label by prepending the parent's name
			boolean noParents = true;
			for (int i = 0; i < parents.size(); i++) {
				IContainer parent = parents.get(i);
				if (parent != null) {
					noParents = false;
					labels.set(i, parent.getName() + File.pathSeparator + labels.get(i));
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
		return new Builder<T>(factory);
	}

	public static class Builder<T extends InputModels> {

		private final Factory<T> factory;
		private SiDiffResourceSet resourceSet;
		private List<Object> models = new ArrayList<>(); // may be URI, Resource, IFile or File
		private int assertedNumModels = -1;
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
		 * When building, asserts that exactly the given number of input models have been added.
		 * Any negative number will be ignored.
		 * @param assertedNumModels the asserted number of models, negative if no assertion
		 * @return this builder
		 */
		public Builder<T> assertNumModels(int assertedNumModels) {
			this.assertedNumModels = assertedNumModels;
			return this;
		}
		
		protected int getAssertedNumModels() {
			return assertedNumModels;
		}
		
		/**
		 * Sets that when building, it is asserted that alle input models have the same document type.
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

		public Builder<T> addModels(ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				Stream.of(((IStructuredSelection)selection).toArray())
					.filter(e -> e instanceof Resource || e instanceof URI || e instanceof IFile || e instanceof File)
					.forEach(models::add);
			}
			return this;
		}

		public T build() {
			initDefaults();
			assertValidInput();

			List<Resource> resources = models.stream().map(this::deriveResource).collect(Collectors.toList());
			T inputModels = factory.createInputModels(resourceSet, resources);

			assertValidResult(inputModels);
			return inputModels;
		}

		protected void initDefaults() {
			if (resourceSet == null) {
				resourceSet = SiDiffResourceSet.create();
			}
		}

		protected void assertValidInput() {
			if(assertedNumModels >= 0 && models.size() != assertedNumModels) {
				throw new IllegalArgumentException("Number of inputs models but be " + assertedNumModels + ", but is " + models.size());
			}
		}
		
		protected void assertValidResult(InputModels inputModels) {
			if(assertSameDocumentType) {
				if(!inputModels.haveSameDocumentType()) {
					throw new IllegalArgumentException("The input models must have the same document types.");
				}
			}
		}

		protected Resource deriveResource(Object model) {
			if (model instanceof Resource) {
				Resource resource = (Resource)model;
				if(!resourceSet.getResources().contains(model)) {
					resourceSet.getResources().add(resource);
				}
				return resource;
			} else if (model instanceof URI) {
				return resourceSet.getResource((URI)model, true);
			} else if (model instanceof IFile) {
				return deriveResource(EMFStorage.toPlatformURI((IFile)model));
			} else if (model instanceof File) {
				return deriveResource(EMFStorage.toFileURI((File)model));
			}
			throw new IllegalArgumentException("Model is neither URI nor Resource nor IFile");
		}
	}
	
	@FunctionalInterface
	public interface Factory<T extends InputModels> {
		T createInputModels(SiDiffResourceSet resourceSet, List<Resource> resources);
	}
}
