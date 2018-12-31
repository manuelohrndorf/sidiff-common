package org.sidiff.common.extension.ui.widgets;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.sidiff.common.extension.configuration.ConfigurationOption;
import org.sidiff.common.extension.configuration.IConfigurableExtension;
import org.sidiff.common.ui.widgets.AbstractWidget;
import org.sidiff.common.ui.widgets.IWidget;
import org.sidiff.common.ui.widgets.IWidgetDependence;
import org.sidiff.common.ui.widgets.IWidgetModification;

/**
 * <p>A widget which provides suitable inputs for the various options of the
 * {@link org.sidiff.common.extension.configuration.IExtensionConfiguration configuration}
 * of a {@link IConfigurableExtension}.</p>
 * <p>Use {@link #addAllForWidget} to create and add multiple widgets for all extensions
 * managed by a widget and automatically handle toggling of widget visibility.</p>
 * @author Robert Müller
 */
public class ConfigurableExtensionWidget extends AbstractWidget {

	private IConfigurableExtension extension;
	private ExpandableComposite expandableParent;

	public ConfigurableExtensionWidget(IConfigurableExtension extension) {
		this.extension = Objects.requireNonNull(extension);
	}

	@Override
	public Composite createControl(Composite parent) {
		expandableParent = new ExpandableComposite(parent, SWT.BORDER);
		GridLayoutFactory.fillDefaults().applyTo(expandableParent);
		expandableParent.setText("Configuration Options: " + extension.getName());
		expandableParent.setClient(createConfigurationControl(expandableParent));
		expandableParent.addExpansionListener(new IExpansionListener() {
			@Override
			public void expansionStateChanging(ExpansionEvent e) {
			}
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				getWidgetCallback().requestLayout();
			}
		});
		return expandableParent;
	}

	protected Control createConfigurationControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(composite);
		for(ConfigurationOption<?> option : extension.getConfiguration().getConfigurationOptions()) {
			createConfigurationOptionControl(composite, option);
		}
		return composite;
	}

	@SuppressWarnings("unchecked")
	protected Control createConfigurationOptionControl(Composite parent, ConfigurationOption<?> option) {
		final Class<?> type = option.getType();
		if(type == Boolean.class) {
			return createCheckboxControl(parent, (ConfigurationOption<Boolean>)option);
		} else if(type == Short.class || type == Byte.class || type == Integer.class) {
			return createNumberControl(parent, (ConfigurationOption<? extends Number>)option);
		} else if(type == String.class || type == Float.class || type == Double.class || type == Long.class) {
			return createTextControl(parent, option);
		}
		return null;
	}

	protected Control createCheckboxControl(Composite parent, ConfigurationOption<Boolean> option) {
		Button check = new Button(parent, SWT.CHECK);
		check.setText(option.getName());
		check.setToolTipText("Option '" + option.getKey() + "' of '" + extension.getKey() + "'");
		check.setSelection(option.getValue());
		check.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> option.setValue(check.getSelection())));
		return check;
	}

	protected Control createNumberControl(Composite parent, ConfigurationOption<? extends Number> option) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(option.getName());
		group.setLayout(new GridLayout(1, true));
		
		Spinner spinner = new Spinner(group, SWT.NONE);
		spinner.setToolTipText("Option '" + option.getKey() + "' (" + option.getType().getSimpleName() + ") of '" + extension.getKey() + "'");
		spinner.setSelection(option.getValue().intValue());
		spinner.addSelectionListener(SelectionListener.widgetSelectedAdapter(
				e -> option.setValueUnsafe(String.valueOf(spinner.getSelection()))));
		return group;
	}

	protected Control createTextControl(Composite parent, ConfigurationOption<?> option) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(option.getName());
		group.setLayout(new GridLayout(1, true));
		
		Text text = new Text(group, SWT.NONE);
		text.setText(String.valueOf(option.getValue()));
		text.setToolTipText("Option '" + option.getKey() + "' (" + option.getType().getSimpleName() + ") of '" + extension.getKey() + "'");
		text.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> option.setValueUnsafe(text.getText())));
		return group;
	}

	protected static boolean isTypeSupported(Class<?> type) {
		return type == Boolean.class || type == Short.class || type == Byte.class || type == Integer.class
				|| type == String.class || type == Float.class || type == Double.class || type == Long.class;
	}

	@Override
	public Composite getWidget() {
		return expandableParent;
	}

	/**
	 * <p>Adds new {@link ConfigurableExtensionWidget}s to the given parent for every
	 * {@link IConfigurableExtension} that the given widget manages.</p>
	 * <p>The visibility of the widgets is updated based on the selection in the main widget.</p>
	 * <p>To satisfy the type argument, the widget should generally extend
	 * {@link org.sidiff.common.ui.widgets.AbstractModifiableWidget AbstractModifiableWidget}.
	 * Usage with {@link org.sidiff.common.ui.pages.AbstractWizardPage AbstractWizardPage}:</p>
	 * <pre>ConfigurableExtensionWidget.addAllForWidget(composite, widget, this::addWidget);</pre>
	 * @param parent the parent composite
	 * @param widget the widget for which to create new configuration widgets
	 * @param addWidget the function to add a widget to wizard page
	 */
	public static <W extends IWidgetDependence & IWidgetModification<? extends IConfigurableExtension>>
		void addAllForWidget(Composite parent, W widget, BiConsumer<Composite,IWidget> addWidget) {

		Map<IConfigurableExtension,ConfigurableExtensionWidget> extensionWidgets = new HashMap<>();
		for(IConfigurableExtension extension : widget.getSelectableValues()) {
			if(extension.getConfiguration().getConfigurationOptions().stream()
					.map(ConfigurationOption::getType).anyMatch(ConfigurableExtensionWidget::isTypeSupported)) {
				ConfigurableExtensionWidget extensionWidget = new ConfigurableExtensionWidget(extension);
				extensionWidget.setDependency(widget);
				extensionWidgets.put(extension, extensionWidget);
				addWidget.accept(parent, extensionWidget);
				extensionWidget.setVisible(widget.getSelection().contains(extension)); // after adding the widget
			}
		}

		widget.addModificationListener((oldValues, newValues) -> {
			extensionWidgets.entrySet().forEach(entry -> {
				entry.getValue().setVisible(newValues.contains(entry.getKey()));
			});
		});
	}
}
