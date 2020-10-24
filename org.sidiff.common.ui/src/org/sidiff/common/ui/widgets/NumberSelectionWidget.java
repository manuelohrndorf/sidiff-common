package org.sidiff.common.ui.widgets;

import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.*;
import org.sidiff.common.ui.widgets.IWidgetValidation.ValidationMessage.ValidationType;

/**
 * Generic number selection widget.
 * @author rmueller
 */
public class NumberSelectionWidget extends AbstractModifiableWidget<Integer> {

	private final int min;
	private final int max;
	private final int increment;

	private Scale scale;
	private Spinner spinner;

	public NumberSelectionWidget(String title, int min, int max, int increment) {
		setTitle(Objects.requireNonNull(title));
		this.min = min;
		this.max = max;
		this.increment = increment;
		Assert.isLegal(min >= 0 && max >= 0, "This widget only supports positive numbers");
		Assert.isLegal(min < max, "This minimum must be less than the maximum");
		Assert.isLegal(increment > 0, "This increment must be greater than zero");
		Assert.isLegal((max-min)/increment < 1000, "Number of segments too large. OS specific SWT implementation may freeze.");
	}

	@Override
	public List<Integer> getSelectableValues() {
		return null;
	}

	@Override
	protected Composite createContents(Composite container) {
		Composite gridContainer = new Composite(container, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(gridContainer);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(gridContainer);

		scale = new Scale(gridContainer, SWT.HORIZONTAL);
		scale.setMinimum(min);
		scale.setMaximum(max);
		scale.setIncrement(increment);
		scale.setSelection(getSingleSelection());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(scale);
		scale.addSelectionListener(SelectionListener.widgetSelectedAdapter(
				e -> setSelection(scale.getSelection())));

		spinner = new Spinner(gridContainer, SWT.NONE);
		spinner.setMinimum(min);
		spinner.setMaximum(max);
		spinner.setIncrement(increment);
		spinner.setSelection(getSingleSelection());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(spinner);
		spinner.addSelectionListener(SelectionListener.widgetSelectedAdapter(
				e -> setSelection(spinner.getSelection())));

		return container;
	}

	@Override
	protected void hookSetSelection() {
		super.hookSetSelection();
		if (spinner != null && scale != null) {
			int value = getSingleSelection();
			if (spinner.getSelection() != value) {
				spinner.setSelection(value);
			}
			if (scale.getSelection() != value) {
				scale.setSelection(value);
			}
		}
	}

	@Override
	protected ValidationMessage doValidate() {
		int value = getSingleSelection();
		if (value < min) {
			return new ValidationMessage(ValidationType.ERROR, "The minimum value for '" + getTitle() + "' is " + min);
		}
		if (value > max) {
			return new ValidationMessage(ValidationType.ERROR, "The maximum value for '" + getTitle() + "' is " + max);
		}
		return super.doValidate();
	}
}