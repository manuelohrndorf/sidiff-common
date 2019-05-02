package org.sidiff.common.emf.metrics.ui.views;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.sidiff.common.emf.metrics.MetricHandle;
import org.sidiff.common.emf.metrics.MetricsFacade;
import org.sidiff.common.emf.metrics.MetricsList;
import org.sidiff.common.emf.metrics.ui.internal.MetricsUiPlugin;
import org.sidiff.common.emf.metrics.ui.jobs.RecomputeMetricsJob;
import org.sidiff.common.file.CSVWriter;

/**
 * @author cpietsch
 * @author rmueller
 */
public class MetricsView extends ViewPart implements ISelectionListener {

	public static final String ID = "org.sidiff.common.emf.metrics.ui.views.MetricsView";

	private Label label;
	private TableViewer tableViewer;
	
	private Clipboard clipboard;

	private Action recomputeAction;
	private Action copyValueAction;
	private Action copyAsCsvAction;
	private Action recomputeAllAction;

	private MetricsList metrics;
	private Resource resource;

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		createLabel(composite);
		createTableViewer(composite);

		clipboard = new Clipboard(Display.getCurrent());

		createRecomputeAction();
		createCopyAsCsvAction();
		createCopyValueAction();
		createRecomputeAllAction();
		createToolBarMenu();
		createContextMenu();

		getSite().getPage().addSelectionListener(this);
		handleResourceChanged();
	}

	private void createRecomputeAction() {
		recomputeAction = new Action() {
			@Override
			public void run() {
				List<MetricHandle> handles =
					Stream.of(tableViewer.getStructuredSelection().toArray())
						.map(MetricHandle.class::cast)
						.collect(Collectors.toList());
				new RecomputeMetricsJob(handles,
					() -> Display.getDefault().asyncExec(() -> tableViewer.update(handles.toArray(), null))).schedule();
			}
		};
		recomputeAction.setText("Recompute");
		recomputeAction.setImageDescriptor(MetricsUiPlugin.getImageDescriptor("recompute.gif"));
		recomputeAction.setToolTipText("Recompute the value of the selected metrics");
	}

	private void createCopyAsCsvAction() {
		copyAsCsvAction = new Action() {
			@Override
			public void run() {
				String csv = CSVWriter.writeToString(csvWriter -> {
					for(Object selectedObject : tableViewer.getStructuredSelection().toArray()) {
						MetricHandle handle = (MetricHandle)selectedObject;
						csvWriter.write(handle.getMetric().getKey(), handle.getValue());
					}
				});
				clipboard.setContents(new Object[] { csv }, new Transfer[] { TextTransfer.getInstance() });
			}
		};
		copyAsCsvAction.setText("Copy as CSV");
		copyAsCsvAction.setImageDescriptor(MetricsUiPlugin.getImageDescriptor("clipboard.gif"));
		copyAsCsvAction.setToolTipText("Copy the selected metrics to the system clipboard in CSV format");
	}

	private void createCopyValueAction() {
		copyValueAction = new Action() {
			@Override
			public void run() {
				String values = Stream.of(tableViewer.getStructuredSelection().toArray())
					.map(MetricHandle.class::cast)
					.map(MetricHandle::getValue)
					.map(Object::toString)
					.collect(Collectors.joining(" "));
				clipboard.setContents(new Object[] { values }, new Transfer[] { TextTransfer.getInstance() });
			}
		};
		copyValueAction.setText("Copy value");
		copyValueAction.setImageDescriptor(MetricsUiPlugin.getImageDescriptor("clipboard.gif"));
		copyValueAction.setToolTipText("Copy the values of the selected metrics to the system clipboard");
	}

	private void createRecomputeAllAction() {
		recomputeAllAction = new Action() {
			@Override
			public void run() {
				if(metrics != null) {
					new RecomputeMetricsJob(metrics,
						() -> Display.getDefault().asyncExec(() -> tableViewer.update(metrics.toArray(), null))).schedule();
				}
			}
		};
		recomputeAllAction.setText("Recompute All");
		recomputeAllAction.setImageDescriptor(MetricsUiPlugin.getImageDescriptor("recompute.gif"));
		recomputeAllAction.setToolTipText("Recompute the values of all metrics");
	}

	private void createToolBarMenu() {
		IActionBars actionBars = getViewSite().getActionBars();
		fillDropDownMenu(actionBars.getMenuManager());
		fillToolBar(actionBars.getToolBarManager());
	}

	private void createContextMenu() {
		MenuManager contextMenu = new MenuManager("#ViewerMenu");
	    contextMenu.setRemoveAllWhenShown(true);
	    contextMenu.addMenuListener(new IMenuListener() {
	        @Override
	        public void menuAboutToShow(IMenuManager menuManager) {
	            fillContextMenu(menuManager);
	        }
	    });
	    Menu menu = contextMenu.createContextMenu(tableViewer.getControl());
	    tableViewer.getControl().setMenu(menu);
	}

	protected void fillToolBar(IToolBarManager toolBar) {
		toolBar.add(recomputeAllAction);
	}

	protected void fillDropDownMenu(IMenuManager dropDownMenu) {
		dropDownMenu.add(recomputeAllAction);
	}

	protected void fillContextMenu(IMenuManager menuManager) {
		menuManager.add(recomputeAction);
		menuManager.add(copyValueAction);
		menuManager.add(copyAsCsvAction);
	}

	private void createLabel(Composite composite) {
		label = new Label(composite, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
	}

	private void createTableViewer(Composite composite) {
		tableViewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setHeaderBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		tableViewer.getTable().setHeaderForeground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(new ArrayContentProvider());
		createNameColumn();
		createValueColumn();

		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);

		tableViewer.addDoubleClickListener(event -> recomputeAction.run());
	}

	private void createNameColumn() {
		TableViewerColumn metricNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		metricNameColumn.getColumn().setText("Name");
		metricNameColumn.getColumn().setWidth(200);
		metricNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getToolTipText(Object element) {
				return ((MetricHandle)element).getMetric().getDescription().orElse(null);
			}
			@Override
			public String getText(Object element) {
				return ((MetricHandle)element).getMetric().getName();
			}
		});
	}

	private void createValueColumn() {
		TableViewerColumn metricValueColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		metricValueColumn.getColumn().setText("Value");
		metricValueColumn.getColumn().setWidth(125);
		metricValueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				MetricHandle handle = (MetricHandle)element;
				return handle.getValue().toString();
			}
			@Override
			public String getToolTipText(Object element) {
				return "Double click to recompute.";
			}
		});
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		 if (selection instanceof IStructuredSelection) {
			 IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			 if(structuredSelection.size() == 1) {
				 Object element = structuredSelection.getFirstElement();
				 if(element instanceof Resource) {
					 setResource((Resource)element);
				 } else {
					 EObject eObject = Adapters.adapt(element, EObject.class);
					 if(eObject != null) {
						 setResource(eObject.eResource());					 
					 }					 
				 }
			 }
		 }
	}

	public void setResource(Resource resource) {
		if(!Objects.equals(resource, this.resource)) {
			this.resource = resource;
			handleResourceChanged();
		}
	}

	public Resource getResource() {
		return resource;
	}

	private void handleResourceChanged() {
		if(resource == null) {
			label.setText("Metrics: no model selected");
			metrics = null;
		} else {
			label.setText("Metrics: " + resource.getURI());
			metrics = MetricsFacade.getMetrics(resource);		
		}
		tableViewer.setInput(metrics);
		recomputeAllAction.setEnabled(metrics != null);
		label.requestLayout(); // needs layout because label may wrap
	}

	@Override
	public void setFocus() {
		if(tableViewer != null) {
			tableViewer.getTable().setFocus();
		}
	}

	public void dispose() {
		super.dispose();
		getSite().getPage().removeSelectionListener(this);
	}
}
