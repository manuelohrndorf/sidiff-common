package org.sidiff.common.ui.widgets.impl;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.sidiff.common.ui.widgets.AbstractModifiableWidget;

/**
 * Generic text selection widget.
 * You must call {@link #setTitle(String)} before the control are created.
 * @author rmueller
 */
public class TextSelectionWidget extends AbstractModifiableWidget<String> {

	private Text text;

	@Override
	public List<String> getSelectableValues() {
		return null;
	}

	@Override
	protected Composite createContents(Composite container) {
		text = new Text(container, SWT.SINGLE);
		text.addModifyListener(event -> {
			if(text.getText().isEmpty()) {
				setSelection(Collections.emptyList());
			} else {
				setSelection(text.getText());
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
		return container;
	}

	@Override
	protected void hookSetSelection() {
		super.hookSetSelection();
		List<String> selection = getSelection();
		String newText = selection.isEmpty() ? "" : selection.get(0);
		if(!text.getText().equals(newText)) {
			text.setText(newText);
		}
		getWidgetCallback().requestValidation();
	}
}
