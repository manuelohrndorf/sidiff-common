package org.sidiff.common.ui.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PatternFilter;
import org.sidiff.common.ui.widgets.IWidgetValidation.ValidationMessage.ValidationType;

/**
 * <p>An abstract list widget is an {@link AbstractModifiableWidget} which
 * creates two list controls showing the selected and the selectable values.</p>
 * <p>In addition to the hook method of the super class, {@link #hookSetSelection()},
 * this class add {@link #hookInitSelection()}.</p>
 * <p>The widget has lower and upper bounds, which are validated. The widget may
 * be ordered, in which case Up/Down buttons to move the selection will be created.
 * The widget may also be filterable, in which case a pattern text input is provided
 * to filter the choice of values.</p>
 * <p>Default values:</p>
 * <ul>
 * <li>Title: Name of Element Type Class</li>
 * <li>Ordered: true</li>
 * <li>Filterable: false</li>
 * <li>Bounds: 1 - <code>Integer.MAX_VALUE</code></li>
 * <li>Table width: 150</li>
 * <li>Table height: 70</li>
 * </ul>
 * @author Robert Müller
 * @param <T> the type of the input elements
 */
public abstract class AbstractListWidget<T> extends AbstractModifiableWidget<T> implements IWidgetValidation {

	protected Composite contents;
	protected TableViewer featureTableViewer;
	protected TableViewer choiceTableViewer;
	protected Button addButton;
	protected Button removeButton;
	protected Button upButton;
	protected Button downButton;
	protected Text patternText;

	private final Class<T> elementType;
	private String title;
	private boolean ordered = true;
	private boolean filterable = false;
	private int lowerBound = 1;
	private int upperBound = Integer.MAX_VALUE;
	private int tableWidth = 150;
	private int tableHeight = 70;

	private ValidationMessage validationMessage;

	public AbstractListWidget(Class<T> elementType) {
		this.elementType = Objects.requireNonNull(elementType);
		setTitle(elementType.getSimpleName());
	}

	@Override
	protected Composite createContents(Composite container) {
		contents = new Composite(container, SWT.NONE);
		contents.setLayout(new GridLayout(3, false));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(contents);

		if(isFilterable()) {
			createFilterText(contents);			
		}

		createFeatureTable(contents);
		createControlButtons(contents);
		createChoiceTable(contents);
		
		if(isFilterable()) {
			attachFilterListener();			
		}

		hookInitSelection();
		return contents;
	}

	protected void createFilterText(Composite contents) {
		Group filterGroupComposite = new Group(contents, SWT.NONE);
		filterGroupComposite.setText("Filter");
		filterGroupComposite.setLayout(new GridLayout(2, false));
		filterGroupComposite.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));

		Label label = new Label(filterGroupComposite, SWT.NONE);
		label.setText("Pattern");

		patternText = new Text(filterGroupComposite, SWT.BORDER);
		patternText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	protected void attachFilterListener() {
		final PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isParentMatch(Viewer viewer, Object element) {
				return viewer instanceof AbstractTreeViewer && super.isParentMatch(viewer, element);
			}
		};
		choiceTableViewer.addFilter(filter);
		patternText.addModifyListener(e -> {
			filter.setPattern(((Text)e.widget).getText());
			choiceTableViewer.refresh();
		});
	}

	protected void createChoiceTable(Composite contents) {
		Composite choiceComposite = new Composite(contents, SWT.NONE);
		{
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.horizontalAlignment = SWT.END;
			choiceComposite.setLayoutData(data);

			GridLayout layout = new GridLayout();
			data.horizontalAlignment = SWT.FILL;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.numColumns = 1;
			choiceComposite.setLayout(layout);
		}

		Label choiceLabel = new Label(choiceComposite, SWT.NONE);
		choiceLabel.setText("Choice of Values");
		GridData choiceLabelGridData = new GridData();
		choiceLabelGridData.verticalAlignment = SWT.FILL;
		choiceLabelGridData.horizontalAlignment = SWT.FILL;
		choiceLabel.setLayoutData(choiceLabelGridData);

		final Table choiceTable = new Table(choiceComposite, SWT.MULTI | SWT.BORDER);
		{
			GridData choiceTableGridData = new GridData();
			choiceTableGridData.verticalAlignment = SWT.FILL;
			choiceTableGridData.horizontalAlignment = SWT.FILL;
			choiceTableGridData.widthHint = getTableWidth();
			choiceTableGridData.heightHint = getTableHeight();
			choiceTableGridData.grabExcessHorizontalSpace = true;
			choiceTableGridData.grabExcessVerticalSpace = true;
			choiceTable.setLayoutData(choiceTableGridData);
		}

		choiceTableViewer = new TableViewer(choiceTable);
		choiceTableViewer.setContentProvider(new ArrayContentProvider());
		choiceTableViewer.setLabelProvider(getLabelProvider());
		choiceTableViewer.setInput(getSelectableValues().toArray());
		choiceTableViewer.setComparator(new ViewerComparator());
		
		choiceTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (addButton.isEnabled()) {
					addButton.notifyListeners(SWT.Selection, null);
				}
			}
		});
		choiceTableViewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return !getSelection().contains(element);
			}
		});
	}

	protected void createFeatureTable(Composite contents) {
		Composite featureComposite = new Composite(contents, SWT.NONE);
		{
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.horizontalAlignment = SWT.END;
			featureComposite.setLayoutData(data);

			GridLayout layout = new GridLayout();
			data.horizontalAlignment = SWT.FILL;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.numColumns = 1;
			featureComposite.setLayout(layout);
		}

		Label featureLabel = new Label(featureComposite, SWT.NONE);
		featureLabel.setText("Selected Values");
		GridData featureLabelGridData = new GridData();
		featureLabelGridData.horizontalSpan = 2;
		featureLabelGridData.horizontalAlignment = SWT.FILL;
		featureLabelGridData.verticalAlignment = SWT.FILL;
		featureLabel.setLayoutData(featureLabelGridData);

		final Table featureTable = new Table(featureComposite, SWT.MULTI | SWT.BORDER);
		GridData featureTableGridData = new GridData();
		featureTableGridData.verticalAlignment = SWT.FILL;
		featureTableGridData.horizontalAlignment = SWT.FILL;
		featureTableGridData.widthHint = getTableWidth();
		featureTableGridData.heightHint = getTableHeight();
		featureTableGridData.grabExcessHorizontalSpace = true;
		featureTableGridData.grabExcessVerticalSpace = true;
		featureTable.setLayoutData(featureTableGridData);

		featureTableViewer = new TableViewer(featureTable);
		featureTableViewer.setContentProvider(new ArrayContentProvider());
		featureTableViewer.setLabelProvider(getLabelProvider());
		featureTableViewer.setInput(getSelection().toArray());
		if(!isOrdered() && isMulti()) {
			// If the order of the values is not significant, we sort them according to their labels.
			// Note that this would break the move up/down buttons, which are only enabled if isOrdered() is true.
			featureTableViewer.setComparator(new ViewerComparator());
		}

		featureTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (removeButton.isEnabled()) {
					removeButton.notifyListeners(SWT.Selection, null);
				}
			}
		});
	}

	protected void createControlButtons(Composite contents) {
		Composite controlButtons = new Composite(contents, SWT.NONE);
		GridData controlButtonsGridData = new GridData();
		controlButtonsGridData.verticalAlignment = SWT.FILL;
		controlButtonsGridData.horizontalAlignment = SWT.FILL;
		controlButtons.setLayoutData(controlButtonsGridData);
		controlButtons.setLayout(new GridLayout());

		new Label(controlButtons, SWT.NONE);

		createAddButton(controlButtons);
		createRemoveButton(controlButtons);

		if(isOrdered() && isMulti()) {
			Label spaceLabel = new Label(controlButtons, SWT.NONE);
			GridData spaceLabelGridData = new GridData();
			spaceLabelGridData.verticalSpan = 2;
			spaceLabel.setLayoutData(spaceLabelGridData);

			createUpButton(controlButtons);
			createDownButton(controlButtons);
		}
	}

	protected void createAddButton(Composite controlButtons) {
		addButton = new Button(controlButtons, SWT.PUSH);
		addButton.setText("Add");

		GridData addButtonGridData = new GridData();
		addButtonGridData.verticalAlignment = SWT.FILL;
		addButtonGridData.horizontalAlignment = SWT.FILL;
		addButton.setLayoutData(addButtonGridData);

		addButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			// event is null when choiceTableViewer is double clicked
			if(getUpperBound() == 1) {
				Object selection = choiceTableViewer.getStructuredSelection().getFirstElement();
				if(elementType.isInstance(selection)) {
					setSelection(Collections.singletonList(elementType.cast(selection)));
				}
			} else {
				List<T> selection = new ArrayList<>(getSelection());
				Stream.of(choiceTableViewer.getStructuredSelection().toArray())
					.filter(elementType::isInstance)
					.map(elementType::cast)
					.forEach(selection::add);
				setSelection(selection);				
			}
		}));
	}

	protected void createRemoveButton(Composite controlButtons) {
		removeButton = new Button(controlButtons, SWT.PUSH);
		removeButton.setText("Remove");

		GridData removeButtonGridData = new GridData();
		removeButtonGridData.verticalAlignment = SWT.FILL;
		removeButtonGridData.horizontalAlignment = SWT.FILL;
		removeButton.setLayoutData(removeButtonGridData);

		removeButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			// event is null when featureTableViewer is double clicked
			List<T> selection = new ArrayList<>(getSelection());
			Stream.of(featureTableViewer.getStructuredSelection().toArray())
				.filter(elementType::isInstance)
				.map(elementType::cast)
				.forEach(selection::remove);
			setSelection(selection);
		}));
	}

	protected void createUpButton(Composite controlButtons) {
		upButton = new Button(controlButtons, SWT.PUSH);
		upButton.setText("Up");

		GridData upButtonGridData = new GridData();
		upButtonGridData.verticalAlignment = SWT.FILL;
		upButtonGridData.horizontalAlignment = SWT.FILL;
		upButton.setLayoutData(upButtonGridData);

		upButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			List<T> selection = new ArrayList<>(getSelection());
			if(selection.size() > 1) {
				for (int index : featureTableViewer.getTable().getSelectionIndices()) {
					Collections.swap(selection, index, Math.max(index - 1, 0));
				}
				setSelection(selection);				
			}
		}));
	}

	protected void createDownButton(Composite controlButtons) {
		downButton = new Button(controlButtons, SWT.PUSH);
		downButton.setText("Down");

		GridData downButtonGridData = new GridData();
		downButtonGridData.verticalAlignment = SWT.FILL;
		downButtonGridData.horizontalAlignment = SWT.FILL;
		downButton.setLayoutData(downButtonGridData);

		downButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			List<T> selection = new ArrayList<>(getSelection());
			if(selection.size() > 1) {
				int indices[] = featureTableViewer.getTable().getSelectionIndices();
				// iterate in reverse order for correct down movement
				for (int i = indices.length-1; i >= 0; i--) {
					int index = indices[i];
					Collections.swap(selection, index, Math.min(index + 1, selection.size() - 1));
				}
				setSelection(selection);				
			}
		}));
	}

	/**
	 * Updates the table viewer. Subclasses must call the super implementation first
	 * when overriding this method.
	 */
	@Override
	protected void hookSetSelection() {
		if(featureTableViewer != null) {
			featureTableViewer.setInput(getSelection().toArray());
			featureTableViewer.refresh();			
		}
		if(choiceTableViewer != null) {
			choiceTableViewer.refresh(); // update because filter depends on selection			
		}
		getWidgetCallback().requestValidation();
	}
	
	/**
	 * Hook method called after the widget's controls have been created
	 * to initialize the selection with default values.
	 */
	protected void hookInitSelection() {
		// default implementation does nothing
	}
	
	/**
	 * Validates the lower and upper bounds.
	 * When overriding this, the super implementation must be called
	 * and the validation message must be set using {@link #setValidationMessage(ValidationMessage)}:
	 * <pre>
	 * if(...) {
	 *   setValidationMessage(...);
	 *   return false;
	 * }
	 * return super.validate();
	 * </pre>
	 */
	@Override
	public boolean validate() {
		if(getSelectableValues().isEmpty()) {
			setValidationMessage(new ValidationMessage(ValidationType.ERROR, "No " + getTitle() + " is available."));
			return false;
		} else if(getSelection().size() < getLowerBound()) {
			if(getLowerBound() == 1) {
				setValidationMessage(new ValidationMessage(ValidationType.ERROR,
						getTitle() + " requires a value."));
			} else {
				setValidationMessage(new ValidationMessage(ValidationType.ERROR,
						getTitle() + " requires at least " + getLowerBound() + " values."));
			}
			return false;
		} else if(getSelection().size() > getUpperBound()) {
			if(getUpperBound() == 1) {
				setValidationMessage(new ValidationMessage(ValidationType.ERROR,
						getTitle() + " cannot have multiple values."));
			} else {
				setValidationMessage(new ValidationMessage(ValidationType.ERROR,
						getTitle() + " must have less than " + getUpperBound() + " values."));
			}
			return false;
		}
		setValidationMessage(ValidationMessage.OK);
		return true;
	}
	
	@Override
	public ValidationMessage getValidationMessage() {
		return validationMessage;
	}
	
	protected void setValidationMessage(ValidationMessage validationMessage) {
		this.validationMessage = validationMessage;
	}

	public Class<T> getElementType() {
		return elementType;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = Objects.requireNonNull(title);
	}

	public boolean isOrdered() {
		return ordered;
	}

	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

	public boolean isFilterable() {
		return filterable;
	}

	public void setFilterable(boolean filterable) {
		this.filterable = filterable;
	}

	public int getLowerBound() {
		return lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}
	
	public boolean isMulti() {
		return upperBound > 1;
	}

	public void setLowerUpperBounds(int lowerBound, int upperBound) {
		Assert.isLegal(lowerBound >= 0 && upperBound > 0 && lowerBound <= upperBound,
				"Invalid bounds [lower: " + lowerBound + ", upper: " + upperBound + "]");
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	public int getTableWidth() {
		return tableWidth;
	}
	
	public void setTableWidth(int tableWidth) {
		this.tableWidth = tableWidth;
	}
	
	public int getTableHeight() {
		return tableHeight;
	}
	
	public void setTableHeight(int tableHeight) {
		this.tableHeight = tableHeight;
	}
}
