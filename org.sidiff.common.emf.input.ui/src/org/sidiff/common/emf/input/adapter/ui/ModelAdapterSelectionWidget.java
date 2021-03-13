package org.sidiff.common.emf.input.adapter.ui;

import java.util.*;

import org.sidiff.common.emf.input.adapter.IModelAdapter;
import org.sidiff.common.extension.ui.labelprovider.ExtensionLabelProvider;
import org.sidiff.common.ui.widgets.AbstractListWidget;

/**
 * @author rmueller
 */
public class ModelAdapterSelectionWidget extends AbstractListWidget<IModelAdapter> {

	private List<IModelAdapter> selectablesValues;

	public ModelAdapterSelectionWidget(Collection<String> documentTypes) {
		super(IModelAdapter.class);
		selectablesValues = new ArrayList<>(IModelAdapter.MANAGER.getExtensions(documentTypes, true));
		initDefaults();
	}

	public ModelAdapterSelectionWidget() {
		super(IModelAdapter.class);
		selectablesValues = new ArrayList<>(IModelAdapter.MANAGER.getExtensions());
		initDefaults();
	}

	private void initDefaults() {
		setTitle("Model Adapter");
		setLowerUpperBounds(0, 1);
		setLabelProvider(new ExtensionLabelProvider());
		setEqualityDelegate(IModelAdapter.MANAGER.getEquality());
	}

	@Override
	public List<IModelAdapter> getSelectableValues() {
		return selectablesValues;
	}
}
