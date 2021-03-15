package org.sidiff.common.emf.input.adapter.ui;

import java.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.statushandlers.StatusManager;
import org.sidiff.common.emf.input.adapter.IModelAdapter;
import org.sidiff.common.emf.input.adapter.ui.ModelAdapterDirectionWidget.Direction;
import org.sidiff.common.emf.input.ui.internal.EmfInputUiPlugin;
import org.sidiff.common.emf.modelstorage.EMFStorage;
import org.sidiff.common.emf.modelstorage.SiDiffResourceSet;
import org.sidiff.common.ui.util.MessageDialogUtil;

/**
 * @author rmueller
 */
public class ModelAdapterWizard extends Wizard {

	private IStructuredSelection selection;
	private ModelAdapterWizardPage modelAdapterWizardPage;

	public ModelAdapterWizard(IStructuredSelection selection) {
		setWindowTitle("Model Adapter Wizard");
		this.selection = Objects.requireNonNull(selection);
	}

	@Override
	public void addPages() {
		modelAdapterWizardPage = new ModelAdapterWizardPage();
		addPage(modelAdapterWizardPage);
	}

	@Override
	public boolean performFinish() {
		final IModelAdapter modelAdapter = modelAdapterWizardPage.getModelAdapter();
		Set<String> fileExtToAdapt = modelAdapterWizardPage.getModelAdapterDirection() == Direction.ModelToProprietary
				? modelAdapter.getModelFileExtensions() : modelAdapter.getProprietaryFileExtensions();
		Set<IFile> filesToAdapt = findFilesToAdapt(fileExtToAdapt);

		if(filesToAdapt.isEmpty()) {
			MessageDialogUtil.showErrorDialog("Model Adapter found no files to adapt",
				"No files to adapt have been found in the selection. "
				+ "The " + modelAdapter.getName() + " adapts files with the extensions " + fileExtToAdapt
				+ " in direction " + modelAdapterWizardPage.getModelAdapterDirection() + ".");
			return false;
		}

		new Job("Adapting: " + modelAdapterWizardPage.getModelAdapterDirection()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				MultiStatus result = new MultiStatus(EmfInputUiPlugin.ID, 0, "See details below.");
				SubMonitor progress = SubMonitor.convert(monitor, filesToAdapt.size());
				for(IFile file : filesToAdapt) {
					progress.subTask(file.getFullPath().toString());
					if(modelAdapterWizardPage.getModelAdapterDirection() == Direction.ModelToProprietary) {
						try {
							modelAdapter.toProprietary(
								SiDiffResourceSet.create().getResource(EMFStorage.toPlatformURI(file), true),
								(IFolder)file.getParent());
						} catch (Exception e) {
							result.add(new Status(IStatus.ERROR, EmfInputUiPlugin.ID,
									"Failed to adapt model to proprietary", e));
						}
					} else {
						try {
							SiDiffResourceSet resourceSet = SiDiffResourceSet.create();
							resourceSet.saveResource(
								modelAdapter.toModel(
									file,
									resourceSet,
									EMFStorage.toPlatformURI(file).trimSegments(1)));
						} catch (Exception e) {
							result.add(new Status(IStatus.ERROR, EmfInputUiPlugin.ID,
									"Failed to adapt proprietary to model", e));
						}
					}
					progress.worked(1);
				}
				return result;
			}
		}.schedule();
		return true;
	}

	private Set<IFile> findFilesToAdapt(Set<String> fileExtToAdapt) {
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
					StatusManager.getManager().handle(e, EmfInputUiPlugin.ID);
				}
			}
		}
		return filesToAdapt;
	}
}
