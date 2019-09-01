package org.sidiff.common.ui.widgets;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.sidiff.common.ui.widgets.IWidgetValidation.ValidationMessage.ValidationType;

public abstract class AbstractTreeSelectionWidget<T> extends AbstractModifiableWidget<T> implements IWidgetValidation {

	private ContainerCheckedTreeViewer treeViewer;

	private int lowerBound = 1;
	private int upperBound = Integer.MAX_VALUE;

	private final Class<T> selectableElementType;
	private final ITreeContentProvider contentProvider;

	public AbstractTreeSelectionWidget(Class<T> selectableElementType, ITreeContentProvider contentProvider) {
		this.selectableElementType = Objects.requireNonNull(selectableElementType);
		this.contentProvider = Objects.requireNonNull(contentProvider);
	}

	@Override
	protected Composite createContents(Composite container) {
		treeViewer = new ContainerCheckedTreeViewer(container);
		treeViewer.setLabelProvider(getLabelProvider());
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setInput((Supplier<Collection<?>>)this::getRootObjects);
		treeViewer.addCheckStateListener(event -> {
			Object element = event.getElement();
			if(event.getChecked()) {
				treeViewer.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);				
			}
			treeViewer.setSubtreeChecked(element, event.getChecked());
			if(!event.getChecked()) {
				treeViewer.collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
			}
			setSelection(Stream.of(treeViewer.getCheckedElements())
					.filter(selectableElementType::isInstance)
					.map(selectableElementType::cast)
					.collect(Collectors.toList()));
		});
		treeViewer.setComparator(new ViewerComparator());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(treeViewer.getControl());

		return container;
	}

	protected abstract Collection<?> getRootObjects();
	
	@Override
	public List<T> getSelectableValues() {
		return null;
	}

	@Override
	protected void hookSetSelection() {
		super.hookSetSelection();
		refresh();
	}

	public void refresh() {
		treeViewer.refresh();
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
}