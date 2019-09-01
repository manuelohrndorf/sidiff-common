package org.sidiff.common.ui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.sidiff.common.ui.widgets.IWidgetValidation.ValidationMessage.ValidationType;

public abstract class AbstractEditableListWidget<T> extends AbstractModifiableWidget<T> implements IWidgetValidation {

	private TableViewer tableViewer;
	private Button buttonAddNew;
	private Button buttonRemove;

	private int lowerBound = 1;
	private int upperBound = Integer.MAX_VALUE;
	private String addNewLabel = "Add new";

	public AbstractEditableListWidget() {
	}

	@Override
	protected Composite createContents(Composite container) {
		tableViewer = new TableViewer(container);
		tableViewer.setLabelProvider(getLabelProvider());
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				return ((List<?>)((Supplier<?>)inputElement).get()).toArray();
			}
		});
		tableViewer.setInput((Supplier<List<?>>)this::getSelection);
		tableViewer.addSelectionChangedListener(event -> refreshButtonStates());
		tableViewer.setComparator(new ViewerComparator());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableViewer.getControl());

		Composite buttonBar = new Composite(container, SWT.NONE);
		buttonBar.setLayout(new GridLayout(2, true));

		buttonAddNew = new Button(buttonBar, SWT.PUSH);
		buttonAddNew.setText(getAddNewLabel());
		buttonAddNew.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			List<T> newSelection = new ArrayList<>(getSelection());
			newSelection.addAll(selectNewElements());
			setSelection(newSelection);
		}));

		buttonRemove = new Button(buttonBar, SWT.PUSH);
		buttonRemove.setText("Remove");
		buttonRemove.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			IStructuredSelection selection = tableViewer.getStructuredSelection();
			List<T> newSelection = new ArrayList<>(getSelection());
			newSelection.removeAll(selection.toList());
			setSelection(newSelection);
		}));
		
		refreshButtonStates();

		return container;
	}

	protected abstract List<T> selectNewElements();

	private void refreshButtonStates() {
		buttonRemove.setEnabled(!tableViewer.getSelection().isEmpty());
	}

	@Override
	protected void hookSetSelection() {
		super.hookSetSelection();
		
		tableViewer.refresh();
		getWidgetCallback().requestValidation();
	}

	@Override
	protected ValidationMessage doValidate() {
		if(getSelection().size() < getLowerBound()) {
			if(getLowerBound() == 1) {
				return new ValidationMessage(ValidationType.ERROR,
						getTitle() + " requires a value.");
			}
			return new ValidationMessage(ValidationType.ERROR,
					getTitle() + " requires at least " + getLowerBound() + " values.");
		} else if(getSelection().size() > getUpperBound()) {
			if(getUpperBound() == 1) {
				return new ValidationMessage(ValidationType.ERROR,
						getTitle() + " cannot have multiple values.");
			}
			return new ValidationMessage(ValidationType.ERROR,
					getTitle() + " must have less than " + getUpperBound() + " values.");
		}
		return ValidationMessage.OK;
	}

	@Override
	public List<T> getSelectableValues() {
		return null; // null = all values are selectable
	}

	public void setLowerUpperBounds(int lowerBound, int upperBound) {
		Assert.isLegal(lowerBound >= 0 && upperBound > 0 && lowerBound <= upperBound,
				"Invalid bounds [lower: " + lowerBound + ", upper: " + upperBound + "]");
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
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
	
	public void setAddNewLabel(String addNewLabel) {
		this.addNewLabel = Objects.requireNonNull(addNewLabel);
	}
	
	public String getAddNewLabel() {
		return addNewLabel;
	}
}