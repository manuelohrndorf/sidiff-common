package org.sidiff.common.emf.input.adapter;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.statushandlers.StatusManager;
import org.sidiff.common.emf.input.InputModels;
import org.sidiff.common.emf.modelstorage.EMFStorage;
import org.sidiff.common.emf.modelstorage.SiDiffResourceSet;

/**
 * @author rmueller
 */
public class ModelAdapterJob extends Job {

	public enum Direction {
		ModelToProprietary {
			@Override
			public String toString() {
				return "Model → Proprietary";
			}
		},
		ProprietaryToModel {
			@Override
			public String toString() {
				return "Proprietary → Model";
			}
		}
	}

	private final IStructuredSelection selection;
	private final Direction adapterDirection;
	private final IFolder outputFolder;
	private final IModelAdapter modelAdapter;

	public ModelAdapterJob(IStructuredSelection selection, IModelAdapter modelAdapter, Direction adapterDirection, IFolder outputFolder) {
		super("Adapting: " + adapterDirection);
		this.selection = Objects.requireNonNull(selection);
		this.adapterDirection = Objects.requireNonNull(adapterDirection);
		this.outputFolder = outputFolder; // null to use same folder as input
		this.modelAdapter = Objects.requireNonNull(modelAdapter);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Set<String> fileExtToAdapt = adapterDirection == Direction.ModelToProprietary
				? modelAdapter.getModelFileExtensions() : modelAdapter.getProprietaryFileExtensions();
		Set<IFile> filesToAdapt = findFilesToAdapt(selection, fileExtToAdapt);

		if(filesToAdapt.isEmpty()) {
			return new Status(IStatus.ERROR, InputModels.PLUGIN_ID,
				"No files to adapt have been found in the selection. "
				+ "The " + modelAdapter.getName() + " adapts files with the following extensions "
				+ "in direction " + adapterDirection + ": " + fileExtToAdapt.stream().collect(Collectors.joining(", ")) + ".");
		}

		MultiStatus result = new MultiStatus(InputModels.PLUGIN_ID, 0, "See details below.");
		SubMonitor progress = SubMonitor.convert(monitor, filesToAdapt.size());
		for(IFile file : filesToAdapt) {
			progress.subTask(file.getFullPath().toString());
			try {
				if(adapterDirection == Direction.ModelToProprietary) {
					modelAdapter.toProprietary(
						SiDiffResourceSet.create().getResource(EMFStorage.toPlatformURI(file), true),
						outputFolder == null
							? (IFolder)file.getParent()
							: outputFolder);
				} else {
					SiDiffResourceSet resourceSet = SiDiffResourceSet.create();
					resourceSet.saveResource(
						modelAdapter.toModel(
							file,
							resourceSet,
							outputFolder == null
								? EMFStorage.toPlatformURI(file).trimSegments(1)
								: EMFStorage.toPlatformURI(outputFolder)));
				}
			} catch (Exception e) {
				result.add(new Status(IStatus.ERROR, InputModels.PLUGIN_ID,
						"Failed to adapt " + adapterDirection + ": " + file.getFullPath(), e));
			}
			progress.worked(1);
		}
		return result;
	}

	private static Set<IFile> findFilesToAdapt(IStructuredSelection selection, Set<String> fileExtToAdapt) {
		Set<IFile> filesToAdapt = new LinkedHashSet<>();

		for(Object selected : selection.toArray()) {
			if(selected instanceof IFile) {
				IFile file = (IFile)selected;
				if(fileExtToAdapt.contains(file.getFileExtension())) {
					filesToAdapt.add(file);
				}
			} else if(selected instanceof IFolder) {
				IFolder folder = (IFolder)selected;
				try {
					folder.accept(element -> {
						if(element instanceof IFolder) {
							return true;
						} else if(element instanceof IFile) {
							IFile file = (IFile)element;
							if(fileExtToAdapt.contains(file.getFileExtension())) {
								filesToAdapt.add(file);
							}
						}
						return false;
					});
				} catch (CoreException e) {
					StatusManager.getManager().handle(e, InputModels.PLUGIN_ID);
				}
			}
		}
		return filesToAdapt;
	}
}