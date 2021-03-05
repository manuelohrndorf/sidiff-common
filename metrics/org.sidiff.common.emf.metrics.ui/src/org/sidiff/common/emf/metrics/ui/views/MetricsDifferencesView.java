package org.sidiff.common.emf.metrics.ui.views;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.sidiff.common.emf.metrics.MetricHandleDifference;
import org.sidiff.common.emf.metrics.MetricValueComparisonResult;
import org.sidiff.common.emf.metrics.MetricsFacade;
import org.sidiff.common.emf.metrics.MetricsListDifference;
import org.sidiff.common.emf.metrics.MetricsUtil;
import org.sidiff.common.emf.metrics.jobs.RecomputeMetricsDifferencesJob;
import org.sidiff.common.emf.metrics.ui.internal.MetricsUiPlugin;
import org.sidiff.common.emf.metrics.ui.views.MetricsView.Tab;

/**
 * @author rmueller
 */
public class MetricsDifferencesView extends ViewPart {

	public static final String ID = "org.sidiff.common.emf.metrics.ui.views.MetricsDifferencesView";

	private Combo originMetricsCombo;
	private Combo changedMetricsCombo;
	private TreeViewer treeViewer;
	private TreeViewerColumn metricNameColumn;
	private TreeViewerColumn metricContextColumn;
	private TreeViewerColumn metricComparisonColumn;

	private Image imageChangeNeutral;
	private Image imageChangeDownBad;
	private Image imageChangeDownGood;
	private Image imageChangeUpBad;
	private Image imageChangeUpGood;

	private Action recomputeAction;
	private Action recomputeAllAction;

	private Map<String,Tab> tabs = Collections.emptyMap();
	private MetricsListDifference metricsDifference;

	@Override
	public void createPartControl(Composite parent) {
		loadImages();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		createOriginChangedCombos(composite);
		createTableViewer(composite);

		recomputeAction = createRecomputeAction();
		recomputeAllAction = createRecomputeAllAction();
		createToolBarMenu();
		createContextMenu();

		updateMetricsDifference();

		// Normally, the metrics view pushes updates to the difference view.
		// Request push manually when first opening, as metrics view may already be open.
		MetricsView metricsView = (MetricsView)getSite().getPage().findView(MetricsView.ID);
		if(metricsView != null) {
			metricsView.handleTabsChanged();
		}
	}

	private void loadImages() {
		imageChangeNeutral = MetricsUiPlugin.getImageDescriptor("change_neutral.png").createImage();
		imageChangeDownBad = MetricsUiPlugin.getImageDescriptor("change_down_bad.png").createImage();
		imageChangeDownGood = MetricsUiPlugin.getImageDescriptor("change_down_good.png").createImage();
		imageChangeUpBad = MetricsUiPlugin.getImageDescriptor("change_up_bad.png").createImage();
		imageChangeUpGood = MetricsUiPlugin.getImageDescriptor("change_up_good.png").createImage();
	}

	private Action createRecomputeAction() {
		Action action = new Action() {
			@Override
			public void run() {
				Set<MetricHandleDifference> differences = getSelectedHandlesDifferences();
				if(!differences.isEmpty()) {
					new RecomputeMetricsDifferencesJob(differences,
							() -> Display.getDefault().asyncExec(() -> differences.forEach(treeViewer::refresh))).schedule();
				}
			}
		};
		action.setText("Recompute");
		action.setImageDescriptor(MetricsUiPlugin.getImageDescriptor("recompute.gif"));
		action.setToolTipText("Recompute selected metric difference");
		return action;
	}

	private Action createRecomputeAllAction() {
		Action action = new Action() {
			@Override
			public void run() {
				if(metricsDifference != null) {
					new RecomputeMetricsDifferencesJob(metricsDifference,
						() -> Display.getDefault().asyncExec(() -> metricsDifference.forEach(treeViewer::refresh))).schedule();
				}
			}
		};
		action.setText("Recompute All");
		action.setImageDescriptor(MetricsUiPlugin.getImageDescriptor("recompute.gif"));
		action.setToolTipText("Recompute all metric differences");
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
	    contextMenu.addMenuListener(menuManager -> fillContextMenu(menuManager));
	    Menu menu = contextMenu.createContextMenu(treeViewer.getControl());
	    treeViewer.getControl().setMenu(menu);
	}

	protected void fillToolBar(IToolBarManager toolBar) {
		toolBar.add(recomputeAllAction);
	}

	protected void fillDropDownMenu(IMenuManager dropDownMenu) {
		dropDownMenu.add(recomputeAllAction);
	}

	protected void fillContextMenu(IMenuManager menuManager) {
		menuManager.add(recomputeAction);
	}

	private void createOriginChangedCombos(Composite composite) {
		Composite rowComposite = new Composite(composite, SWT.NONE);
		rowComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		rowComposite.setLayout(new GridLayout(2, false));

		Label originLabel = new Label(rowComposite, SWT.WRAP);
		originLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		originLabel.setText("Origin");

		originMetricsCombo = new Combo(rowComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		originMetricsCombo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		originMetricsCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> updateMetricsDifference()));

		Label changedLabel = new Label(rowComposite, SWT.WRAP);
		changedLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		changedLabel.setText("Changed");

		changedMetricsCombo = new Combo(rowComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		changedMetricsCombo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		changedMetricsCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> updateMetricsDifference()));
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
		createComparisonColumn();

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
				if(element instanceof MetricHandleDifference) {
					return ((MetricHandleDifference)element).getMetric().getDescription().orElse(null);
				}
				return null;
			}
			@Override
			public String getText(Object element) {
				if(element instanceof MetricHandleDifference) {
					return ((MetricHandleDifference)element).getMetric().getName();
				} else if(element instanceof MetricHandleDifferenceKeyValue) {
					return MetricsUtil.getLabel(((MetricHandleDifferenceKeyValue)element).keys);
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
				if(element instanceof MetricHandleDifference) {
					return Objects.toString(((MetricHandleDifference)element).getOrigin().getContext());
				}
				return null;
			}
			@Override
			public String getText(Object element) {
				if(element instanceof MetricHandleDifference) {
					return ((MetricHandleDifference)element).getOrigin().getContextLabel();
				}
				return null;
			}
		});
		metricContextColumn.getColumn().addSelectionListener(SelectionListener.widgetSelectedAdapter(this::handleColumnSelection));
	}

	private void createComparisonColumn() {
		Function<MetricValueComparisonResult,Color> resultToColor = result -> {
			switch(result.getChangeJudgement()) {
				case NONE:
				case UNCHANGED:
					return treeViewer.getTree().getDisplay().getSystemColor(SWT.COLOR_BLACK);
				case GOOD:
					return treeViewer.getTree().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
				case BAD:
					return treeViewer.getTree().getDisplay().getSystemColor(SWT.COLOR_RED);
			}
			throw new AssertionError();
		};
		Function<MetricValueComparisonResult,Image> resultToImage = result -> {
			switch(result.getChangeJudgement()) {
				case NONE:
				case UNCHANGED:
					return imageChangeNeutral;
				case GOOD:
					return result.getNumericOffset() > 0 ? imageChangeUpGood : imageChangeDownGood;
				case BAD:
					return result.getNumericOffset() > 0 ? imageChangeUpBad : imageChangeDownBad;
			}
			throw new AssertionError();
		};

		metricComparisonColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		metricComparisonColumn.getColumn().setText("Comparison");
		metricComparisonColumn.getColumn().setWidth(125);
		metricComparisonColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if(element instanceof MetricHandleDifference) {
					MetricHandleDifference handle = (MetricHandleDifference)element;
					if(!handle.hasResults()) {
						return "<not computed>";
					}
					if(handle.isUncategorized()) {
						return handle.getUncategorizedResults().getNumericOffsetAsString();
					}
					return "<categorized result>";
				} else if(element instanceof MetricHandleDifferenceKeyValue) {
					return ((MetricHandleDifferenceKeyValue)element).result.getNumericOffsetAsString();
				}
				return super.getText(element);
			}
			@Override
			public Color getForeground(Object element) {
				if(element instanceof MetricHandleDifference) {
					MetricHandleDifference handle = (MetricHandleDifference)element;
					if(handle.hasResults() && handle.isUncategorized()) {
						return resultToColor.apply(handle.getUncategorizedResults());
					}
				} else if(element instanceof MetricHandleDifferenceKeyValue) {
					return resultToColor.apply(((MetricHandleDifferenceKeyValue)element).result);
				}
				return super.getForeground(element);
			}
			@Override
			public Image getImage(Object element) {
				if(element instanceof MetricHandleDifference) {
					MetricHandleDifference handle = (MetricHandleDifference)element;
					if(handle.hasResults() && handle.isUncategorized()) {
						return resultToImage.apply(handle.getUncategorizedResults());
					}
				} else if(element instanceof MetricHandleDifferenceKeyValue) {
					return resultToImage.apply(((MetricHandleDifferenceKeyValue)element).result);
				}
				return null;
			}
			@Override
			public String getToolTipText(Object element) {
				return "Double click to recompute.";
			}
		});
	}

	private void handleColumnSelection(SelectionEvent event) {
		if(event.widget == metricNameColumn.getColumn()) {
			sortColumn(metricNameColumn.getColumn(), MetricHandleDifference::getByNameComparator);
		} else if(event.widget == metricContextColumn.getColumn()) {
			sortColumn(metricContextColumn.getColumn(), MetricHandleDifference::getByContextComparator);
		}
		treeViewer.refresh();
	}

	private void sortColumn(TreeColumn column, Supplier<Comparator<MetricHandleDifference>> comparatorFactory) {
		if(metricsDifference == null) {
			return;
		}
		if(treeViewer.getTree().getSortColumn() == column && treeViewer.getTree().getSortDirection() == SWT.DOWN) {
			metricsDifference.sort(comparatorFactory.get().reversed());
			treeViewer.getTree().setSortDirection(SWT.UP);
		} else {
			metricsDifference.sort(comparatorFactory.get());
			treeViewer.getTree().setSortDirection(SWT.DOWN);
		}
		treeViewer.getTree().setSortColumn(column);
	}

	private Set<MetricHandleDifference> getSelectedHandlesDifferences() {
		Set<MetricHandleDifference> handles = new HashSet<>();
		for(Object selected : treeViewer.getStructuredSelection().toList()) {
			if(selected instanceof MetricHandleDifference) {
				handles.add((MetricHandleDifference)selected);
			} else if(selected instanceof MetricHandleDifferenceKeyValue) {
				handles.add(((MetricHandleDifferenceKeyValue)selected).handle);
			}
		}
		return handles;
	}

	private void updateMetricsDifference() {
		if(originMetricsCombo.getSelectionIndex() == -1
				|| changedMetricsCombo.getSelectionIndex() == -1) {
			metricsDifference = null;
		} else {
			Tab originTab = tabs.get(originMetricsCombo.getText());
			Tab changedTab = tabs.get(changedMetricsCombo.getText());
			if(originTab == null || changedTab == null
					|| originTab.getMetrics() == null || changedTab.getMetrics() == null) {
				metricsDifference = null;
			} else {
				metricsDifference = MetricsFacade.calculateDifference(originTab.getMetrics(), changedTab.getMetrics());
			}
		}
		handleMetricsDifferenceChanged();
	}

	private void handleMetricsDifferenceChanged() {
		treeViewer.setInput(metricsDifference);
		treeViewer.getTree().setSortDirection(SWT.NONE);
		treeViewer.getTree().setSortColumn(null);
		recomputeAllAction.setEnabled(metricsDifference != null);
	}

	@Override
	public void setFocus() {
		if(treeViewer != null) {
			treeViewer.getTree().setFocus();
		}
	}

	void setMetricsTabs(List<Tab> tabs) {
		this.tabs = tabs.stream().collect(Collectors.toMap(
				tab -> tab.getTitle() + " - " + MetricsUtil.getLabel(tab.getSelectedNotifier()),
				Function.identity()));
		handleTabsChanged();
	}

	protected void handleTabsChanged() {
		originMetricsCombo.setItems(tabs.keySet().toArray(new String[0]));
		changedMetricsCombo.setItems(tabs.keySet().toArray(new String[0]));
	}

	@Override
	public void dispose() {
		super.dispose();

		imageChangeNeutral.dispose();
		imageChangeDownBad.dispose();
		imageChangeDownGood.dispose();
		imageChangeUpBad.dispose();
		imageChangeUpGood.dispose();
	}

	private static class ContentProvider implements ITreeContentProvider {

		private static final Object[] EMPTY_ARRAY = new Object[0];

		@Override
		public Object[] getElements(Object inputElement) {
			return ((MetricsListDifference)inputElement).toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof MetricHandleDifference) {
				MetricHandleDifference handleDiff = (MetricHandleDifference)parentElement;
				if(handleDiff.hasResults() && !handleDiff.isUncategorized()) {
					return handleDiff.getResults().entrySet().stream()
								.map(entry -> new MetricHandleDifferenceKeyValue(handleDiff, entry.getKey(), entry.getValue()))
								.toArray();
				}
			}
			return EMPTY_ARRAY;
		}

		@Override
		public Object getParent(Object element) {
			if(element instanceof MetricHandleDifferenceKeyValue) {
				return ((MetricHandleDifferenceKeyValue)element).handle;
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if(element instanceof MetricHandleDifference) {
				MetricHandleDifference handleDiff = (MetricHandleDifference)element;
				return handleDiff.hasResults() && !handleDiff.isUncategorized();
			}
			return false;
		}
	}

	private static class MetricHandleDifferenceKeyValue {

		public final MetricHandleDifference handle;
		public final Set<Object> keys;
		public final MetricValueComparisonResult result;

		public MetricHandleDifferenceKeyValue(MetricHandleDifference handle, Set<Object> keys, MetricValueComparisonResult result) {
			this.handle = Objects.requireNonNull(handle);
			this.keys = Collections.unmodifiableSet(new HashSet<>(keys));
			this.result = Objects.requireNonNull(result);
		}
	}
}
