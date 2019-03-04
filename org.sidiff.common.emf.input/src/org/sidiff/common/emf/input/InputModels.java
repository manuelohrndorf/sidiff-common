package org.sidiff.common.emf.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.doctype.util.EMFDocumentTypeUtil;
import org.sidiff.common.emf.modelstorage.EMFStorage;
import org.sidiff.common.emf.modelstorage.SiDiffResourceSet;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;

/**
 * A list of input models represented by {@link IFile} and {@link Resource}.
 */
public class InputModels {

	private List<IFile> files;
	private List<Resource> resources;
	protected SiDiffResourceSet resourceSet;

	private List<String> labels;
	private Set<String> documentTypes;
	private IProject project;

	private InputModels() {
		this.files = new ArrayList<IFile>();
		this.resources = new ArrayList<Resource>();
		this.resourceSet = SiDiffResourceSet.create();
	}

	/**
	 * Creates a new container for the given IFiles and loads the corresponding Resources
	 * using the resource set.
	 * @param files the files
	 */
	public InputModels(IFile ...files) {
		this();
		for(int i = 0; i < files.length; i++) {
			this.files.add(i, files[i]);
			this.resources.add(i, getResource(files[i]));
		}
	}

	protected Resource getResource(IFile file) {
		return resourceSet.getResource(EMFStorage.toPlatformURI(file), true);
	}

	/**
	 * Creates a new container for the given Resources and derives the corresponding IFiles.
	 * @param resources the resources
	 */
	public InputModels(Resource ...resources) {
		this();
		for(int i = 0; i < resources.length; i++) {
			this.resources.add(i, resources[i]);
			this.resourceSet.getResources().add(resources[i]);
			this.files.add(i, EMFStorage.toIFile(EMFStorage.getNormalizedURI(resources[i])));
		}
	}

	/**
	 * Returns whether the resources have the same document types.
	 * @return <code>true</code> if all resources have the same set of document types, <code>false</code> otherwise
	 */
	public boolean haveSameDocumentType() {
		Set<Set<String>> documentTypes = new HashSet<Set<String>>();
		for(Resource resource : resources) {
			documentTypes.add(new HashSet<String>(EMFDocumentTypeUtil.resolve(resource)));
		}
		return documentTypes.size() == 1;
	}

	/**
	 * Returns all document types between input models. This is
	 * the union of all resource document types.
	 * @return unmodifiable set of the document types of all input models
	 */
	public Set<String> getDocumentTypes() {
		if(documentTypes == null) {
			documentTypes = new HashSet<String>(EMFDocumentTypeUtil.resolve(resources));
		}
		return Collections.unmodifiableSet(documentTypes);
	}

	/**
	 * @return unmodifiable list of the resources
	 */
	public List<Resource> getResources() {
		return Collections.unmodifiableList(resources);
	}

	protected List<Resource> internalGetResources() {
		return resources;
	}

	/**
	 * @return unmodifiable list of the files
	 */
	public List<IFile> getFiles() {
		return Collections.unmodifiableList(files);
	}

	protected List<IFile> internalGetFiles() {
		return files;
	}

	/**
	 * Returns labels to be displayed for all files. 
	 * The label of a file is it's name. If multiple labels are equal,
	 * they are made unique by prepending the file's parent's name.
	 * @return labels for the files
	 */
	public List<String> getLabels() {
		if(labels == null) {
			initLabels();
		}
		return Collections.unmodifiableList(labels);
	}

	/**
	 * Returns the {@link IProject} that contains the input files.
	 * If the files are not in the same project,
	 * this the project of the first file is returned.
	 * @return project, <code>null</code> if resolution failed
	 */
	public IProject getProject() {
		if(project == null) {
			project = getProject(files);
		}
		return project;
	}

	public SiDiffResourceSet getResourceSet() {
		return resourceSet;
	}

	/**
	 * Reverses the order of the input models.
	 */
	public void swap() {
		Collections.reverse(files);
		Collections.reverse(resources);
	}

	protected void initLabels() {
		labels = new ArrayList<String>(files.size());
		List<IContainer> parents = new ArrayList<IContainer>(files.size());
		for(IFile file : files) {
			labels.add(file.getName());
			parents.add(file.getParent());
		}

		// by creating a set, we can check for duplicate labels
		while((new HashSet<String>(labels).size()) < labels.size()) {
			// update each label by prepending the parent's name
			boolean noParents = true;
			for(int i = 0; i < parents.size(); i++) {
				IContainer parent = parents.get(i);
				if(parent != null) {
					noParents = false;
					labels.set(i, parent.getName() + "/" + labels.get(i));
					parents.set(i, parent.getParent());
				}
			}

			// if no more parents are found, some labels might
			// still be duplicates, but there is nothing left to do
			if(noParents) {
				break;
			}
		}
	}

	protected static IProject getProject(List<IFile> files) {
		IProject project = null;
		for(IFile file : files) {
			if(file == null) {
				LogUtil.log(LogEvent.WARNING, "File of input model was not resolved.");
			} else if(project == null) {
				project = file.getProject();
			} else if(!project.equals(file.getProject())) {
				LogUtil.log(LogEvent.WARNING, "Input models are not in the same project. Using project of first one.");
				break;
			}
		}
		return project;
	}
}
