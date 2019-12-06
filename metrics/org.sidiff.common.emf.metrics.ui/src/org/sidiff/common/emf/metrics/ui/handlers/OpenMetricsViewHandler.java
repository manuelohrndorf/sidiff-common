package org.sidiff.common.emf.metrics.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.sidiff.common.emf.metrics.ui.internal.MetricsUiPlugin;
import org.sidiff.common.emf.metrics.ui.views.MetricsView;
import org.sidiff.common.ui.util.UIUtil;

public class OpenMetricsViewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Display.getDefault().asyncExec(() -> {
			try {
				MetricsView metricsView = UIUtil.showView(MetricsView.class, MetricsView.ID);
				metricsView.selectionChanged(
					HandlerUtil.getActivePart(event),
					HandlerUtil.getCurrentSelection(event));
			} catch (PartInitException e) {
				StatusManager.getManager().handle(e, MetricsUiPlugin.ID);
			}
		});
		return null;
	}
}
