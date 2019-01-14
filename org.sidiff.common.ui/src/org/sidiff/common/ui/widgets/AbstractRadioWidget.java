package org.sidiff.common.ui.widgets;

import org.eclipse.swt.SWT;

/**
 * An abstract modifiable widget which shows for each selectable
 * value a radio button, one of which can be selected at a time.
 * @author Robert Müller
 */
public abstract class AbstractRadioWidget<T> extends AbstractButtonWidget<T> {

	public AbstractRadioWidget() {
		super(SWT.RADIO);
	}
}
