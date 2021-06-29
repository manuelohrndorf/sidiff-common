package org.sidiff.common.emf.input.adapter.ui;

import java.util.Arrays;
import java.util.List;

import org.sidiff.common.emf.input.adapter.ModelAdapterJob.Direction;
import org.sidiff.common.ui.widgets.AbstractRadioWidget;

/**
 * @author rmueller
 */
public class ModelAdapterDirectionWidget extends AbstractRadioWidget<Direction> {

	public ModelAdapterDirectionWidget() {
		setTitle("Model Adapter Direction");
		setSelection(Direction.ModelToProprietary);
	}

	@Override
	public List<Direction> getSelectableValues() {
		return Arrays.asList(Direction.values());
	}
}
