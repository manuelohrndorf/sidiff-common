package org.sidiff.common.emf.input.adapter.ui;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.sidiff.common.emf.input.adapter.IModelAdapter;
import org.sidiff.common.extension.ui.labelprovider.ExtensionLabelProvider;
import org.sidiff.common.ui.widgets.AbstractListWidget;

public class ModelAdapterSelectionWidget extends AbstractListWidget<IModelAdapter> {

	private List<IModelAdapter> selectablesValues;

	public ModelAdapterSelectionWidget(Collection<String> documentTypes,
			Supplier<IModelAdapter> getter, Consumer<IModelAdapter> setter) {
		super(IModelAdapter.class);
		selectablesValues = new ArrayList<>(IModelAdapter.MANAGER.getExtensions(documentTypes, true));
		setTitle("Model Adapter");
		setLowerUpperBounds(0, 1);
		setLabelProvider(new ExtensionLabelProvider());
		setEqualityDelegate(IModelAdapter.MANAGER.getEquality());
		setSelection(getter.get());
		addModificationListener((oldValues, newValues) -> setter.accept(newValues.isEmpty() ? null : newValues.get(0)));
	}

	@Override
	public List<IModelAdapter> getSelectableValues() {
		return selectablesValues;
	}
}
