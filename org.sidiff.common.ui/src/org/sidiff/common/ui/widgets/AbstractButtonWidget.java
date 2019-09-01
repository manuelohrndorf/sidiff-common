package org.sidiff.common.ui.widgets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * An abstract modifiable widget which shows, for each selectable
 * value, a check/radio button.
 * Use {@link AbstractCheckboxWidget} or {@link AbstractRadioWidget} instead.
 * @author Robert Müller
 */
public abstract class AbstractButtonWidget<T> extends AbstractModifiableWidget<T> {

	private int buttonStyle;

	private Map<T,Button> buttons;
	private Map<Button,T> values;

	private final SelectionListener selectionListener = SelectionListener.widgetSelectedAdapter(event -> {
		setSelection(buttons.entrySet().stream()
				.filter(e -> e.getValue().getSelection())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList()));
	});

	/**
	 * @param buttonStyle the {@link Button} style 
	 */
	public AbstractButtonWidget(int buttonStyle) {
		this.buttonStyle = buttonStyle;
	}

	@Override
	protected Composite createContents(Composite container) {
		Composite composite = new Composite(container, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(composite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		buttons = new HashMap<>();
		values = new HashMap<>();
		for(T value : getSelectableValues()) {
			Button button = new Button(composite, buttonStyle);
			button.setText(getLabelProvider().getText(value));
			GridDataFactory.fillDefaults().grab(true, true).applyTo(button);
			button.addSelectionListener(selectionListener);
			button.setSelection(getSelection().contains(value));
			buttons.put(value, button);
			values.put(button, value);
		}

		hookInitButtons();
		return composite;
	}

	protected void hookInitButtons() {
		// default implementation does nothing
	}

	@Override
	protected void hookSetSelection() {
		if(values != null && buttons != null) {
			Set<Button> remainingButtons = new HashSet<>(values.keySet());
			for(T value : getSelection()) {
				Button button = buttons.get(value);
				if(button != null) {
					button.setSelection(true);
					remainingButtons.remove(button);
				}
			}
			for(Button button : remainingButtons) {
				button.setSelection(false);
			}			
		}
	}

	protected Map<T, Button> getButtons() {
		return buttons;
	}

	protected Map<Button, T> getValues() {
		return values;
	}
}
