package org.sidiff.common.emf.input.adapter.ui;

import java.util.Objects;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.sidiff.common.emf.input.adapter.ModelAdapterJob;

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
		new ModelAdapterJob(selection,
			modelAdapterWizardPage.getModelAdapter(),
			modelAdapterWizardPage.getModelAdapterDirection(),
			modelAdapterWizardPage.getOutputFolder()).schedule();
		return true;
	}
}
