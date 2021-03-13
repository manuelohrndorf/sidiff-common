package org.sidiff.common.emf.input.adapter.ui;

import java.util.Arrays;
import java.util.List;

import org.sidiff.common.ui.widgets.AbstractRadioWidget;

/**
 * @author rmueller
 */
public class ModelAdapterDirectionWidget extends AbstractRadioWidget<ModelAdapterDirectionWidget.Direction> {

	enum Direction {
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
		};
	}

	public ModelAdapterDirectionWidget() {
		setTitle("Model Adapter Direction");
		setSelection(Direction.ModelToProprietary);
	}

	@Override
	public List<Direction> getSelectableValues() {
		return Arrays.asList(Direction.values());
	}
}
