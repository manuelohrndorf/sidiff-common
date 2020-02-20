package org.sidiff.common.ui.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.sidiff.common.collections.UniqueQueue;
import org.sidiff.common.ui.widgets.IWidgetValidation.ValidationMessage.ValidationType;

public abstract class AbstractTreeSelectionWidget<T> extends AbstractModifiableWidget<T> {

	private ContainerCheckedTreeViewer treeViewer;
	private Composite buttonBar;

	private int lowerBound = 1;
	private int upperBound = Integer.MAX_VALUE;
	
	private int heightHint = 300;

	private final Class<T> selectableElementType;
	private final ITreeContentProvider contentProvider;

	private boolean userChangedSelection = false;

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
			userChangedSelection = true;
			setSelection(
				Stream.of(treeViewer.getCheckedElements())
					.filter(selectableElementType::isInstance)
					.map(selectableElementType::cast)
					.collect(Collectors.toList()));
			userChangedSelection = false;
		});
		treeViewer.setComparator(new ViewerComparator());
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, heightHint).applyTo(treeViewer.getControl());

		buttonBar = new Composite(container, SWT.NONE);
		RowLayoutFactory.fillDefaults().type(SWT.HORIZONTAL).applyTo(buttonBar);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonBar);

		refresh();

		return container;
	}

	private void recreateSelectionButtons() {
		if(buttonBar == null) {
			return;
		}
		for(Control control : buttonBar.getChildren()) {
	        control.dispose();
	    }
		addSelectionButtons((label, handler) -> {
			Button button = new Button(buttonBar, SWT.PUSH);
			button.setText(label);
			button.addSelectionListener(SelectionListener.widgetSelectedAdapter(
					event -> setSelection(handler.apply(getSelectableValues(), getSelection()).collect(Collectors.toList()))));
		});
		buttonBar.requestLayout();
	}

	/**
	 * Returns the root objects displayed in this widget.
	 * The input for the content provider will be a <code>Supplier&lt;Collection&lt;?>></code>
	 * which returns a collection of these root objects.
	 * @return collection of root objects
	 */
	protected abstract Collection<?> getRootObjects();

	/**
	 * <p>Adds button specifications to the given acceptor.
	 * The buttons are shown in a button bar and allow quick selection/deselection
	 * of all or specific items.<p>
	 * <p>The default implementation adds buttons to select and deselect all items.
	 * Call super first, if a superclass should contribute its buttons.</p>
	 * @param acceptor acceptor for button specifications
	 */
	protected void addSelectionButtons(ISelectionButtonAcceptor<T> acceptor) {
		acceptor.addButton("None", (selectable, selection) -> Stream.empty());
		acceptor.addButton("All", (selectable, selection) -> selectable.stream());
	}

	@Override
	public List<T> getSelectableValues() {
		List<T> values = new ArrayList<>();
		UniqueQueue<Object> objectsQueue = new UniqueQueue<>(getRootObjects());
		for(Object object : objectsQueue) {
			if(selectableElementType.isInstance(object)) {
				values.add(selectableElementType.cast(object));
			}
			Object children[] = contentProvider.getChildren(object);
			for(Object child : children) {
				objectsQueue.offer(child);
			}
		}
		return Collections.unmodifiableList(values);
	}

	@Override
	protected void hookSetSelection() {
		super.hookSetSelection();
		if(!userChangedSelection) {
			updateTreeViewerSelection();
		}
		getWidgetCallback().requestValidation();
	}

	private void updateTreeViewerSelection() {
		if(treeViewer == null) {
			return;			
		}
		// first expand everything, otherwise checking the elements may not work
		treeViewer.expandAll();
		List<T> selectable = getSelectableValues();
		treeViewer.setCheckedElements(getSelection().stream()
				.map(item -> selectable.stream()
						.filter(selectableItem -> item == selectableItem || getEqualityDelegate().test(item, selectableItem))
						.findFirst().orElse(item))
				.toArray());

		// now collapse everything and only expand the checked elements
		treeViewer.collapseAll();
		for(Object item : treeViewer.getCheckedElements()) {
			treeViewer.expandToLevel(item, 1);
		}
		treeViewer.refresh();
	}

	public void refresh() {
		updateTreeViewerSelection();
		recreateSelectionButtons();
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
	
	public void setHeightHint(int heightHint) {
		this.heightHint = heightHint;
	}
	
	public int getHeightHint() {
		return heightHint;
	}

	protected interface ISelectionButtonAcceptor<T> {

		/**
		 * Adds a buttons specification to the acceptor.
		 * @param label the label of the button
		 * @param handler handler which will be called when the button is pressed,
		 * takes as argument the selectable values and the current selection and
		 * returns the new selection, e.g. to select all: <code>(selectable, selection) -> selectable</code>
		 */
		void addButton(String label, BiFunction<List<T>,List<T>,Stream<T>> handler);
	}
}