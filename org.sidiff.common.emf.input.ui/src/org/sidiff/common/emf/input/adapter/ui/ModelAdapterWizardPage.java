package org.sidiff.common.emf.input.adapter.ui;

import org.eclipse.core.resources.IFolder;
import org.sidiff.common.emf.input.adapter.IModelAdapter;
import org.sidiff.common.emf.ui.widgets.FolderSelectionWidget;
import org.sidiff.common.extension.ui.widgets.ConfigurableExtensionWidget;
import org.sidiff.common.ui.pages.AbstractWizardPage;

/**
 * @author rmueller
 */
public class ModelAdapterWizardPage extends AbstractWizardPage {

	private ModelAdapterSelectionWidget modelAdapterWidget;
	private ModelAdapterDirectionWidget directionWidget;
	private FolderSelectionWidget outputFolderWidget;

	public ModelAdapterWizardPage() {
		super("ModelAdapterWizardPage", "Use a Model Adapter");
	}

	@Override
	protected void createWidgets() {
		modelAdapterWidget = new ModelAdapterSelectionWidget();
		modelAdapterWidget.setLowerUpperBounds(1, 1);
		modelAdapterWidget.getSelectableValues().stream().findFirst().ifPresent(modelAdapterWidget::setSelection);
		addWidget(container, modelAdapterWidget);

		ConfigurableExtensionWidget.addAllForWidget(container, modelAdapterWidget, this::addWidget);

		directionWidget = new ModelAdapterDirectionWidget();
		addWidget(container, directionWidget);

		outputFolderWidget = new FolderSelectionWidget();
		outputFolderWidget.setTitle("Output folder (empty to use same folder as inputs)");
		outputFolderWidget.setLowerUpperBounds(0, 1);
		addWidget(container, outputFolderWidget);
	}

	@Override
	protected String getDefaultMessage() {
		return "Use a model adapter to convert a proprietary file to a model file or vice versa.";
	}

	public IModelAdapter getModelAdapter() {
		return modelAdapterWidget.getSingleSelection();
	}

	public ModelAdapterDirectionWidget.Direction getModelAdapterDirection() {
		return directionWidget.getSingleSelection();
	}

	public IFolder getOutputFolder() {
		return outputFolderWidget.getSingleSelection();
	}
}
