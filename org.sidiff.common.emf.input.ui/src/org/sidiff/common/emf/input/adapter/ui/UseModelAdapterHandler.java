package org.sidiff.common.emf.input.adapter.ui;

import org.eclipse.core.commands.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author rmueller
 */
public class UseModelAdapterHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		new WizardDialog(HandlerUtil.getActiveShell(event),
				new ModelAdapterWizard(HandlerUtil.getCurrentStructuredSelection(event))).open();
		return null;
	}
}
