package org.sidiff.common.emf.input.adapter.ui;

import org.sidiff.common.emf.input.adapter.IModelAdapter;
import org.sidiff.common.extension.ui.widgets.ConfigurableExtensionWidget;
import org.sidiff.common.ui.pages.AbstractWizardPage;

/**
 * @author rmueller
 */
public class ModelAdapterWizardPage extends AbstractWizardPage {

	private ModelAdapterSelectionWidget modelAdapterWidget;
	private ModelAdapterDirectionWidget directionWidget;

	public ModelAdapterWizardPage() {
		super("ModelAdapterWizardPage", "Use a Model Adapter");
	}

	@Override
	protected void createWidgets() {
		modelAdapterWidget = new ModelAdapterSelectionWidget();
		modelAdapterWidget.setLowerUpperBounds(1, 1);
		addWidget(container, modelAdapterWidget);

		ConfigurableExtensionWidget.addAllForWidget(container, modelAdapterWidget, this::addWidget);

		directionWidget = new ModelAdapterDirectionWidget();
		addWidget(container, directionWidget);
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
}
