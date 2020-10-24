package org.sidiff.common.ui.widgets;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

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
		text.setText(getSelection().isEmpty() ? "" : getSelection().get(0));
		text.addModifyListener(event -> setSelection(
				text.getText().isEmpty() ? Collections.emptyList() : Collections.singletonList(text.getText())));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
		return container;
	}

	@Override
	protected void hookSetSelection() {
		super.hookSetSelection();
		if (text != null) {
			List<String> selection = getSelection();
			String newText = selection.isEmpty() ? "" : selection.get(0);
			if(!text.getText().equals(newText)) {
				text.setText(newText);
			}
			getWidgetCallback().requestValidation();
		}
	}
}
