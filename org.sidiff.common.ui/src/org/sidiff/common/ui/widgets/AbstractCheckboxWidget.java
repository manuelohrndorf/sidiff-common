package org.sidiff.common.ui.widgets;

import org.eclipse.swt.SWT;

/**
 * An abstract modifiable widget which shows for each selectable
 * value a checkbox which can be individually selected.
 * @author rmueller
 */
public abstract class AbstractCheckboxWidget<T> extends AbstractButtonWidget<T> {

	public AbstractCheckboxWidget() {
		super(SWT.CHECK);
	}
}
