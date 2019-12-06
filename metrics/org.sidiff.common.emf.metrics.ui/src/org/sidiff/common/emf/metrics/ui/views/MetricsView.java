package org.sidiff.common.emf.metrics.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.sidiff.common.emf.metrics.MetricHandle;
import org.sidiff.common.emf.metrics.MetricsFacade;
import org.sidiff.common.emf.metrics.MetricsList;
import org.sidiff.common.emf.metrics.MetricsScope;
import org.sidiff.common.emf.metrics.MetricsUtil;
import org.sidiff.common.emf.metrics.jobs.RecomputeMetricsJob;
import org.sidiff.common.emf.metrics.ui.internal.MetricsUiPlugin;
import org.sidiff.common.file.CSVWriter;

/**
 * @author cpietsch
 * @author rmueller
 */
public class MetricsView extends ViewPart implements ISelectionListener {

	public static final String ID = "org.sidiff.common.emf.metrics.ui.views.MetricsView";

	private Label label;
	private TreeViewer treeViewer;
	private TreeViewerColumn metricNameColumn;
	private TreeViewerColumn metricContextColumn;
	private TreeViewerColumn metricValueColumn;

	private Clipboard clipboard;

	private Action recomputeAction;
	private Action copyValueAction;
	private Action copyAsCsvAction;
	private Action recomputeAllAction;
	private Action expandSelectionAction;

	private MetricsList metrics;
	private Notifier selectedNotifier;

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		createLabel(composite);
		createTableViewer(composite);

		clipboard = new Clipboard(Display.getCurrent());

		recomputeAction = createRecomputeAction();
		copyValueAction = createCopyValueAction();
		copyAsCsvAction = createCopyAsCsvAction();
		recomputeAllAction = createRecomputeAllAction();
		expandSelectionAction = createExpandSelectionAction();
		createToolBarMenu();
		createContextMenu();

		getSite().getPage().addSelectionListener(this);
		handleSelectedNotifierChanged();
	}

	private Action createRecomputeAction() {
		Action action = new Action() {
			@Override
			public void run() {
				Set<MetricHandle> handles = getSelectedHandles();
				if(!handles.isEmpty()) {
					new RecomputeMetricsJob(handles,
							() -> Display.getDefault().asyncExec(() -> handles.forEach(treeViewer::refresh))).schedule();					
				}
			}
		};
		action.setText("Recompute");
		action.setImageDescriptor(MetricsUiPlugin.getImageDescriptor("recompute.gif"));
		action.setToolTipText("Recompute the value of the selected metrics");
		return action;
	}

	private Action createCopyAsCsvAction() {
		Action action = new Action() {
			@Override
			public void run() {
				Set<MetricHandle> handles = getSelectedHandles();
				if(!handles.isEmpty()) {
					String csv = CSVWriter.writeToString(csvWriter -> {
						for(MetricHandle handle : handles) {
							csvWriter.write(handle.getMetric().getKey(), handle.getContextLabel(), MetricsUtil.getLabel(handle.getValues()));							
						}
					});
					if(!csv.isEmpty()) {
						clipboard.setContents(new Object[] { csv }, new Transfer[] { TextTransfer.getInstance() });					
					}
				}
			}
		};
		action.setText("Copy as CSV");
		action.setImageDescriptor(MetricsUiPlugin.getImageDescriptor("clipboard.gif"));
		action.setToolTipText("Copy the selected metrics to the system clipboard in CSV format");
		return action;
	}

	private Action createCopyValueAction() {
		Action action = new Action() {
			@Override
			public void run() {
				String values = getSelectedHandles().stream()
					.map(MetricHandle::getValues)
					.map(MetricsUtil::getLabel)
					.collect(Collectors.joining(" "));
				if(!values.isEmpty()) {
					clipboard.setContents(new Object[] { values }, new Transfer[] { TextTransfer.getInstance() });					
				}
			}
		};
		action.setText("Copy value");
		action.setImageDescriptor(MetricsUiPlugin.getImageDescriptor("clipboard.gif"));
		action.setToolTipText("Copy the values of the selected metrics to the system clipboard");
		return action;
	}

	private Action createRecomputeAllAction() {
		Action action = new Action() {
			@Override
			public void run() {
				if(metrics != null) {
					new RecomputeMetricsJob(metrics,
						() -> Display.getDefault().asyncExec(() -> metrics.forEach(treeViewer::refresh))).schedule();
				}
			}
		};
		action.setText("Recompute All");
		action.setImageDescriptor(MetricsUiPlugin.getImageDescriptor("recompute.gif"));
		action.setToolTipText("Recompute the values of all metrics");
		return action;
	}
	
	private Action createExpandSelectionAction() {
		Action action = new Action("Expand Selection to Resource", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				// nothing to do, checked state is already changed
			}
		};
		action.setToolTipText("Expand selection to containing Resource when selecting an EObject");
		action.setImageDescriptor(MetricsUiPlugin.getImageDescriptor("arrow_out.png"));
		action.setChecked(true);
		return action;
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
	    Menu menu = contextMenu.createContextMenu(treeViewer.getControl());
	    treeViewer.getControl().setMenu(menu);
	}

	protected void fillToolBar(IToolBarManager toolBar) {
		toolBar.add(recomputeAllAction);
		toolBar.add(expandSelectionAction);
	}

	protected void fillDropDownMenu(IMenuManager dropDownMenu) {
		dropDownMenu.add(recomputeAllAction);
		dropDownMenu.add(expandSelectionAction);
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
		treeViewer = new TreeViewer(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.getTree().setHeaderBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		treeViewer.getTree().setHeaderForeground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
		treeViewer.getTree().setLinesVisible(true);
		treeViewer.setContentProvider(new ContentProvider());
		createNameColumn();
		createContextColumn();
		createValueColumn();

		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);

		treeViewer.addDoubleClickListener(event -> recomputeAction.run());
	}

	private void createNameColumn() {
		metricNameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		metricNameColumn.getColumn().setText("Name");
		metricNameColumn.getColumn().setWidth(200);
		metricNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getToolTipText(Object element) {
				if(element instanceof MetricHandle) {
					return ((MetricHandle)element).getMetric().getDescription().orElse(null);					
				}
				return null;
			}
			@Override
			public String getText(Object element) {
				if(element instanceof MetricHandle) {
					return ((MetricHandle)element).getMetric().getName();
				} else if(element instanceof MetricHandleKeyValue) {
					return MetricsUtil.getLabel(((MetricHandleKeyValue)element).keys);
				}
				return null;
			}
		});
		metricNameColumn.getColumn().addSelectionListener(SelectionListener.widgetSelectedAdapter(this::handleColumnSelection));
	}

	private void createContextColumn() {
		metricContextColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		metricContextColumn.getColumn().setText("Context");
		metricContextColumn.getColumn().setWidth(200);
		metricContextColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getToolTipText(Object element) {
				if(element instanceof MetricHandle) {
					return Objects.toString(((MetricHandle)element).getContext());
				}
				return null;
			}
			@Override
			public String getText(Object element) {
				if(element instanceof MetricHandle) {
					return ((MetricHandle)element).getContextLabel();
				}
				return null;
			}
		});
		metricContextColumn.getColumn().addSelectionListener(SelectionListener.widgetSelectedAdapter(this::handleColumnSelection));
	}

	private void createValueColumn() {
		metricValueColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		metricValueColumn.getColumn().setText("Value");
		metricValueColumn.getColumn().setWidth(125);
		metricValueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if(element instanceof MetricHandle) {
					MetricHandle handle = (MetricHandle)element;
					if(handle.isUncategorized()) {
						return MetricsUtil.getLabel(handle.getUncategorizedValues());
					}
					return "<categorized value>";					
				} else if(element instanceof MetricHandleKeyValue) {
					return MetricsUtil.getLabel(((MetricHandleKeyValue)element).values);
				}
				return null;
			}
			@Override
			public String getToolTipText(Object element) {
				return "Double click to recompute.";
			}
		});
		metricValueColumn.getColumn().addSelectionListener(SelectionListener.widgetSelectedAdapter(this::handleColumnSelection));
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			if (structuredSelection.size() == 1) {
				Notifier notifier = Adapters.adapt(structuredSelection.getFirstElement(), Notifier.class);
				setSelectedNotifier(notifier);
			}
		}
	}

	private void handleColumnSelection(SelectionEvent event) {
		if(event.widget == metricNameColumn.getColumn()) {
			sortColumn(metricNameColumn.getColumn(), MetricHandle::getByNameComparator);
		} else if(event.widget == metricContextColumn.getColumn()) {
			sortColumn(metricContextColumn.getColumn(), MetricHandle::getByContextComparator);
		} else if(event.widget == metricValueColumn.getColumn()) {
			sortColumn(metricValueColumn.getColumn(), MetricHandle::getByValueComparator);
		}
		treeViewer.refresh();
	}

	private void sortColumn(TreeColumn column, Supplier<Comparator<MetricHandle>> comparatorFactory) {
		if(treeViewer.getTree().getSortColumn() == column && treeViewer.getTree().getSortDirection() == SWT.DOWN) {
			metrics.sort(comparatorFactory.get().reversed());
			treeViewer.getTree().setSortDirection(SWT.UP);
		} else {
			metrics.sort(comparatorFactory.get());				
			treeViewer.getTree().setSortDirection(SWT.DOWN);
		}
		treeViewer.getTree().setSortColumn(column);
	}

	public void setSelectedNotifier(Notifier selectedNotifier) {
		if(!Objects.equals(selectedNotifier, this.selectedNotifier)) {
			this.selectedNotifier = selectedNotifier;
			handleSelectedNotifierChanged();
		}
	}

	public Notifier getSelectedNotifier() {
		return selectedNotifier;
	}

	private void handleSelectedNotifierChanged() {
		if(selectedNotifier == null) {
			metrics = null;
		} else {
			metrics = MetricsFacade.getMetrics(createMetricsScope());
		}
		label.setText("Metrics: " + MetricsUtil.getLabelForNotifier(selectedNotifier));
		treeViewer.setInput(metrics);
		treeViewer.getTree().setSortDirection(SWT.NONE);
		treeViewer.getTree().setSortColumn(null);
		recomputeAllAction.setEnabled(metrics != null);
		label.requestLayout(); // needs layout because label may wrap
	}

	private MetricsScope createMetricsScope() {
		MetricsScope scope = new MetricsScope(selectedNotifier);
		scope.includeParentResource = expandSelectionAction.isChecked();
		scope.includeParentResourceSet = expandSelectionAction.isChecked();
		return scope;
	}
	
	private Set<MetricHandle> getSelectedHandles() {
		Set<MetricHandle> handles = new HashSet<>();
		for(Object selected : treeViewer.getStructuredSelection().toList()) {
			if(selected instanceof MetricHandle) {
				handles.add((MetricHandle)selected);
			} else if(selected instanceof MetricHandleKeyValue) {
				handles.add(((MetricHandleKeyValue)selected).handle);
			}
		}
		return handles;
	}

	@Override
	public void setFocus() {
		if(treeViewer != null) {
			treeViewer.getTree().setFocus();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		getSite().getPage().removeSelectionListener(this);
	}

	private static class ContentProvider implements ITreeContentProvider {

		private static final Object[] EMPTY_ARRAY = new Object[0];

		@Override
		public Object[] getElements(Object inputElement) {
			MetricsList metricsList = (MetricsList)inputElement;
			return metricsList.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof MetricHandle) {
				MetricHandle handle = (MetricHandle)parentElement;
				if(!handle.isUncategorized()) {
					return handle.getValues().entrySet().stream()
								.map(entry -> new MetricHandleKeyValue(handle, entry.getKey(), entry.getValue()))
								.toArray();
				}
			}
			return EMPTY_ARRAY;
		}

		@Override
		public Object getParent(Object element) {
			if(element instanceof MetricHandleKeyValue) {
				return ((MetricHandleKeyValue)element).handle;
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if(element instanceof MetricHandle) {
				MetricHandle handle = (MetricHandle)element;
				return !handle.isUncategorized();
			}
			return false;
		}
	}

	private static class MetricHandleKeyValue {

		public final MetricHandle handle;
		public final Set<Object> keys;
		public final List<Object> values;

		public MetricHandleKeyValue(MetricHandle handle, Set<Object> keys, List<Object> values) {
			this.handle = handle;
			this.keys = Collections.unmodifiableSet(new HashSet<>(keys));
			this.values = Collections.unmodifiableList(new ArrayList<>(values));
		}
	}
}
