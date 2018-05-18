package org.sidiff.common.ui.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.sidiff.common.ui.widgets.IWidget;
import org.sidiff.common.ui.widgets.IWidgetDependence;
import org.sidiff.common.ui.widgets.IWidgetSelection;
import org.sidiff.common.ui.widgets.IWidgetValidation;
import org.sidiff.common.ui.widgets.IWidgetValidation.ValidationMessage.ValidationType;

/**
 * 
 * @author cpietsch
 *
 */
public abstract class AbstractWizardPage extends WizardPage implements
		IPageChangedListener {

	/**
	 * The {@link SelectionAdapter} in order to listen to widget selections
	 */
	protected SelectionAdapter validationListener;

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
	protected List<IWidget> widgets;

	// ---------- Constructor ----------

	public AbstractWizardPage(String pageName, String title) {
		super(pageName);
		
		this.setTitle(title);
		
		this.widgets = new ArrayList<IWidget>();
		
		this.validationListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validate();

				// FIXME/TEMPORARY: update scrolled composite size to react to shown/hidden widgets
				scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
			}
		};
	}
	
	public AbstractWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		this(pageName, title);
		this.setImageDescriptor(titleImage);
	}

	// ---------- IDialogPage ----------

	@Override
	public void createControl(Composite parent) {
		// Add scrolling to this page
		Composite wrapper = new Composite(parent, SWT.NONE);
		{
			GridLayout gl_wrapper = new GridLayout(1, false);
			gl_wrapper.marginWidth = 0;
			gl_wrapper.marginHeight = 0;
			wrapper.setLayout(gl_wrapper);
		}

		scrolledComposite = new ScrolledComposite(wrapper, SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		container = new Composite(scrolledComposite, SWT.NONE);
		{
			GridLayout gl_container = new GridLayout(1, false);
			gl_container.marginWidth = 10;
			gl_container.marginHeight = 10;
			container.setLayout(gl_container);
		}
		scrolledComposite.setContent(container);

		// Create widgets for this page:
		createWidgets();

		scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));

		setControl(wrapper);

		validate();
	}

	// ---------- IPageChangedListener ----------

	@Override
	public void pageChanged(PageChangedEvent event) {
		validate();
	}

	// ---------- Private/Protected Methods ----------

	/**
	 * This methods is called by {@link #createControl(Composite)} and must be
	 * implemented by a subclass using the method
	 * {@link #addWidget(Composite, IWidget)}
	 */
	protected abstract void createWidgets();

	/**
	 * This methods adds a widget to the list {@link #widgets}, adds a listener
	 * to the widget and sets the layout of the respective widget.
	 * 
	 * @param parent
	 *            the {@link Composite} to that the widget is added
	 * @param widget
	 *            the {@link IWidget} that is added
	 */
	protected void addWidget(Composite parent, IWidget widget) {
		// Create controls:
		widget.createControl(parent);
		{
			// Set layout data and minimum size
			GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
			widget.setLayoutData(gridData);
			Point widgetSize = widget.getWidget().computeSize(SWT.DEFAULT, SWT.DEFAULT);
			gridData.minimumWidth = widgetSize.x;
			gridData.minimumHeight = widgetSize.y;
		}

		// Add widget:
		widgets.add(widget);

		// Add validation:
		if (widget instanceof IWidgetSelection) {
			((IWidgetSelection) widget)
					.addSelectionListener(validationListener);
		}

		// Update the enabled state of the widget now that the control has been created:
		if (widget instanceof IWidgetDependence) {
			final IWidgetDependence widgetDependence = (IWidgetDependence) widget;
			widgetDependence.setEnabled(widgetDependence.isEnabled());
		}
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
		for (int i = widgets.size()-1; i >= 0 ; i--) {
			if (widgets.get(i) instanceof IWidgetValidation) {
				validateWidget((IWidgetValidation) widgets.get(i));
			}
		}
	}

	/**
	 * This method validates a widget and sets the respective page message.
	 * 
	 * @param widget
	 * @return
	 */
	protected boolean validateWidget(IWidgetValidation widget) {
		boolean isValid = widget.validate();
		if (!isValid) {
			if (widget.getValidationMessage().getType()
					.equals(ValidationType.ERROR)) {
				setErrorMessage(widget.getValidationMessage().getMessage());
				setPageComplete(false);
			} else if (widget.getValidationMessage().getType()
					.equals((ValidationType.WARNING))) {
				setMessage(widget.getValidationMessage().getMessage(),
						IMessageProvider.WARNING);
			}
		}
		return isValid;
	}
	
	protected abstract String getDefaultMessage();
}
