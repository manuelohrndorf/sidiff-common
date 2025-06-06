package org.sidiff.common.ui.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.sidiff.common.ui.widgets.IWidget;
import org.sidiff.common.ui.widgets.IWidgetCallback;
import org.sidiff.common.ui.widgets.IWidgetDependence;
import org.sidiff.common.ui.widgets.IWidgetDisposable;
import org.sidiff.common.ui.widgets.IWidgetValidation;
import org.sidiff.common.ui.widgets.IWidgetValidation.ValidationMessage;
import org.sidiff.common.ui.widgets.IWidgetValidation.ValidationMessage.ValidationType;

/**
 * <p>Abstract wizard class containing {@link IWidget}s.</p>
 * <p>Override {@link #createWidgets()} and add them
 * using {@link #addWidget(Composite, IWidget)}.</p>
 * @author cpietsch
 * @author rmueller
 */
public abstract class AbstractWizardPage extends WizardPage implements
		IPageChangedListener, IWidgetCallback.Callback {

	/**
	 * The number of columns that the layout containing the widgets has.
	 * The value is 12, as it has a many divisors: 1, 2, 3, 4, 6, 12.
	 */
	protected static final int NUM_COLUMNS = 12;

	/**
	 * The current {@link ValidationMessage} being shown, may be <code>null</code>.
	 */
	private ValidationMessage validationMessage;

	/**
	 * The {@link SelectionListener} in order to listen to widget selections
	 */
	protected SelectionListener validationListener;

	// ---------- UI Elements ----------

	/**
	 * The {@link Composite} containing all widgets
	 */
	protected Composite container;

	/**
	 * The {@link ScrolledComposite} containing the {@link #container}
	 */
	private ScrolledComposite scrolledComposite;

	/**
	 * A list of {@link IWidget} which are contained by {@link #container}
	 */
	private List<IWidget> widgets;

	// ---------- Constructor ----------

	public AbstractWizardPage(String pageName, String title) {
		super(pageName);
		this.setTitle(title);
		this.widgets = new ArrayList<>();
		this.validationListener = SelectionListener.widgetSelectedAdapter(e -> requestValidation());
	}

	public AbstractWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		this(pageName, title);
		this.setImageDescriptor(titleImage);
	}

	// ---------- IDialogPage ----------

	@Override
	public void createControl(Composite parent) {
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		GridDataFactory.fillDefaults().applyTo(scrolledComposite);

		container = new Composite(scrolledComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(NUM_COLUMNS).margins(10, 10).applyTo(container);
		scrolledComposite.setContent(container);

		// Create widgets for this page:
		createWidgets();

		requestLayout();

		setControl(scrolledComposite);

		requestValidation();

		IWizard wizard = getWizard();
		if(wizard != null) {
			if(wizard.getContainer() instanceof IPageChangeProvider) {
				((IPageChangeProvider)wizard.getContainer()).addPageChangedListener(this);
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();

		// dispose of all disposable widgets
		for(IWidget widget : widgets) {
			if(widget instanceof IWidgetDisposable) {
				((IWidgetDisposable)widget).dispose();
			}
		}
		widgets.clear();
	}

	// ---------- IPageChangedListener ----------

	@Override
	public void pageChanged(PageChangedEvent event) {
		requestValidation();
		requestLayout();
	}

	// ---------- IWidgetCallback.Callback ----------

	@Override
	public void requestValidation() {
		validate();
	}

	@Override
	public void requestLayout() {
		scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
	}

	// ---------- Private/Protected Methods ----------

	/**
	 * This methods is called by {@link #createControl(Composite)} and must be
	 * implemented by a subclass using the method
	 * {@link #addWidget(Composite, IWidget)}
	 */
	protected abstract void createWidgets();

	/**
	 * <p>This methods adds a widget to the list {@link #widgets}, adds a listener
	 * to the widget and sets the layout of the respective widget.</p>
	 * <p>The widget will take up the whole row.</p>
	 * @param parent the {@link Composite} to that the widget is added
	 * @param widget the {@link IWidget} that is added
	 */
	protected final void addWidget(Composite parent, IWidget widget) {
		addWidget(parent, widget, NUM_COLUMNS);
	}

	/**
	 * <p>This methods adds a widget to the list {@link #widgets}, adds a listener
	 * to the widget and sets the layout of the respective widget.</p>
	 *
	 * @param parent the {@link Composite} to that the widget is added
	 * @param widget the {@link IWidget} that is added
	 * @param numColumns how many columns of the row this widget takes up, the total are {@link #NUM_COLUMNS}
	 */
	protected final void addWidget(Composite parent, IWidget widget, int numColumns) {
		// Set callbacks:
		if (widget instanceof IWidgetCallback) {
			((IWidgetCallback) widget).setWidgetCallback(this);
		}

		// Create controls:
		Composite widgetControl = widget.createControl(parent);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = numColumns;
		widgetControl.setLayoutData(gridData);

		// Add widget:
		widgets.add(widget);

		// Update the enabled state of the widget now that the control has been created:
		if (widget instanceof IWidgetDependence) {
			final IWidgetDependence widgetDependence = (IWidgetDependence) widget;
			widgetDependence.setEnabled(widgetDependence.isEnabled());
		}
	}

	protected final Composite addExpandableComposite(Composite parent, String title) {
		return addExpandableComposite(parent, title, NUM_COLUMNS);
	}

	protected final Composite addExpandableComposite(Composite parent, String title, int numColumns) {
		ExpandableComposite expandable = new ExpandableComposite(parent, SWT.BORDER);
		GridLayoutFactory.fillDefaults().margins(2, 2).applyTo(expandable);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = numColumns;
		expandable.setLayoutData(gridData);
		expandable.setText(title);
		Composite child = new Composite(expandable, SWT.NONE);
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(NUM_COLUMNS).margins(2, 2).applyTo(child);
		expandable.setClient(child);
		expandable.addExpansionListener(new IExpansionListener() {
			@Override
			public void expansionStateChanging(ExpansionEvent e) {
				//
			}
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				requestLayout();
			}
		});
		return child;
	}

	/**
	 * This method validates the page by calling
	 * {@link #validateWidget(IWidgetValidation)} for each widget contained by
	 * {@link #widgets}
	 */
	protected void validate() {
		setErrorMessage(null);
		setMessage(getDefaultMessage());
		setPageComplete(true);
		validationMessage = null;
		for (int i = widgets.size()-1; i >= 0 ; i--) {
			IWidget widget = widgets.get(i);
			if(widget instanceof IWidgetValidation) {
				validateWidget((IWidgetValidation) widgets.get(i));
			}
		}
	}

	/**
	 * This method validates a widget and sets the respective page message.
	 *
	 * @param widget the widget
	 */
	protected void validateWidget(IWidgetValidation widget) {
		ValidationMessage message = widget.validate();
		if(message.getType() == ValidationType.ERROR) {
			setPageComplete(false);
		}
		setValidationMessage(message);
	}

	protected void setValidationMessage(ValidationMessage message) {
		if(message.getType() == ValidationType.OK
				|| validationMessage != null && validationMessage.getType().ordinal() > message.getType().ordinal()) {
			// we have a more important message to show
			return;
		}
		setMessage(message.getMessage(), message.getType().getCode());
		this.validationMessage = message;
	}

	/**
	 * Returns the default message that is shown below the title when no other validation message overrides it.
	 * @return default message
	 */
	protected abstract String getDefaultMessage();
}
