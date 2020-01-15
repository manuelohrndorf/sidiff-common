package org.sidiff.common.extension.ui.widgets;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.sidiff.common.extension.configuration.ConfigurationOption;
import org.sidiff.common.extension.configuration.IConfigurableExtension;
import org.sidiff.common.extension.configuration.IExtensionConfiguration;
import org.sidiff.common.ui.widgets.AbstractContainerWidget;
import org.sidiff.common.ui.widgets.IWidget;
import org.sidiff.common.ui.widgets.IWidgetDependence;
import org.sidiff.common.ui.widgets.IWidgetModification;

/**
 * <p>A widget which provides suitable inputs for the various options of the
 * {@link org.sidiff.common.extension.configuration.IExtensionConfiguration configuration}
 * of a {@link IConfigurableExtension}.</p>
 * <p>Use {@link #addAllForWidget} to create and add multiple widgets for all extensions
 * managed by a widget and automatically handle toggling of widget visibility.</p>
 * @author rmueller
 */
public class ConfigurableExtensionWidget extends AbstractContainerWidget {

	private IConfigurableExtension extension;

	public ConfigurableExtensionWidget(IConfigurableExtension extension) {
		super(DefaultContainerFactory.EXPANDABLE);
		this.extension = Objects.requireNonNull(extension);
		setTitle("Configuration Options: " + extension.getName());
	}

	@Override
	protected Composite createContents(Composite container) {
		return createOptionControls(container, extension.getConfiguration());
	}

	protected Composite createOptionControls(Composite container, IExtensionConfiguration configuration) {
		Composite composite = new Composite(container, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(composite);
		for(ConfigurationOption<?> option : configuration.getConfigurationOptions()) {
			createConfigurationOptionControl(composite, option);
		}
		return composite;
	}

	@SuppressWarnings("unchecked") // we explicitly check below
	protected <T> Control createConfigurationOptionControl(Composite parent, ConfigurationOption<T> option) {
		if(option.getSelectableValues() != null) {
			if(option.isMulti()) {
				return createMultipleChoiceControl(parent, option);
			}
			return createSingleChoiceControl(parent, option);
		}
		if(option.isMulti()) {
			// The widget only supports multi-options with fixed selectable values
			return null;
		}

		final Class<T> type = option.getType();
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
		if(option.isSet()) {
			check.setSelection(option.getValue());
		}
		check.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> option.setValue(check.getSelection())));
		return check;
	}

	protected Control createNumberControl(Composite parent, ConfigurationOption<? extends Number> option) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(option.getName());
		group.setLayout(new GridLayout(1, true));
		
		Spinner spinner = new Spinner(group, SWT.NONE);
		spinner.setToolTipText("Option '" + option.getKey() + "' (" + option.getType().getSimpleName() + ") of '" + extension.getKey() + "'");
		if(option.isSet()) {
			spinner.setSelection(option.getValue().intValue());
		}
		spinner.addSelectionListener(SelectionListener.widgetSelectedAdapter(
				e -> option.setValueUnsafe(String.valueOf(spinner.getSelection()))));
		if(option.getMinValue() != null) {
			spinner.setMinimum(option.getMinValue().intValue());			
		}
		if(option.getMaxValue() != null) {
			spinner.setMaximum(option.getMaxValue().intValue());
		}
		return group;
	}

	protected Control createTextControl(Composite parent, ConfigurationOption<?> option) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(option.getName());
		group.setLayout(new GridLayout(1, true));
		
		Text text = new Text(group, SWT.NONE);
		if(option.isSet()) {
			text.setText(String.valueOf(option.getValue()));
		}
		text.setToolTipText("Option '" + option.getKey() + "' (" + option.getType().getSimpleName() + ") of '" + extension.getKey() + "'");
		text.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> option.setValueUnsafe(text.getText())));
		return group;
	}

	protected <T> Control createSingleChoiceControl(Composite parent, ConfigurationOption<T> option) {
		if(option.getValue() == null) {
			option.resetToDefault();
			if(option.getValue() == null) {
				// Preselect first value as default if no default value set
				option.setValue(option.getSelectableValues().stream().findFirst().orElse(null));
			}
		}

		Group group = new Group(parent, SWT.NONE);
		group.setText(option.getName());
		group.setToolTipText("Option '" + option.getKey() + "' (" + option.getType().getSimpleName() + ") of '" + extension.getKey() + "'");
		group.setLayout(new GridLayout(1, true));

		Map<T,Composite> nestedOptions = new HashMap<>();
		Runnable updateVisibilities = () -> {
			nestedOptions.forEach((value, composite) -> {
				boolean visible = option.getLabelForValue(value).equals(option.getLabelForValue(option.getValue()));
				composite.setVisible(visible);
				((GridData)composite.getLayoutData()).exclude = !visible;
				composite.requestLayout();
				getWidgetCallback().requestLayout();
			});
		};

		for(T value : option.getSelectableValues()) {
			Button button = new Button(group, SWT.RADIO);
			button.setText(option.getLabelForValue(value));
			button.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
				option.setValueUnsafe(value);
				updateVisibilities.run();
			}));
			if(option.getLabelForValue(value).equals(option.getLabelForValue(option.getValue()))) {
				button.setSelection(true);
			}

			if(value instanceof IConfigurableExtension) {
				IExtensionConfiguration configuration = ((IConfigurableExtension)value).getConfiguration();
				if(configuration.getConfigurationOptions().stream().anyMatch(ConfigurableExtensionWidget::isOptionSupported)) {
					Group subGroup = new Group(group, SWT.NONE);
					subGroup.setLayout(new GridLayout());
					subGroup.setLayoutData(new GridData());
					createOptionControls(subGroup, configuration);
					nestedOptions.put(value, subGroup);
				}
			}
		}
		updateVisibilities.run();
		return group;
	}

	protected <T> Control createMultipleChoiceControl(Composite parent, ConfigurationOption<T> option) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(option.getName());
		group.setToolTipText("Option '" + option.getKey() + "' (" + option.getType().getSimpleName() + ") of '" + extension.getKey() + "'");
		group.setLayout(new GridLayout(1, true));

		Map<Button,T> buttonToValue = new LinkedHashMap<>();
		Map<T,Composite> nestedOptions = new HashMap<>();
		Runnable updateVisibilities = () -> {
			nestedOptions.forEach((value, composite) -> {
				boolean visible = option.getValues().stream()
						.map(option::getLabelForValue).anyMatch(v -> v.equals(option.getLabelForValue(value)));
				composite.setVisible(visible);
				((GridData)composite.getLayoutData()).exclude = !visible;
				composite.requestLayout();
				getWidgetCallback().requestLayout();
			});
		};
		SelectionListener selectionListener = SelectionListener.widgetSelectedAdapter(event -> {
			option.setValues(buttonToValue.entrySet().stream()
				.filter(entry -> entry.getKey().getSelection())
				.map(Map.Entry::getValue)
				.collect(Collectors.toList()));
			updateVisibilities.run();
		});

		for(T value : option.getSelectableValues()) {
			Button button = new Button(group, SWT.CHECK);
			button.setText(option.getLabelForValue(value));
			button.addSelectionListener(selectionListener);
			button.setSelection(option.getValues().stream()
					.anyMatch(v -> option.getLabelForValue(v).equals(option.getLabelForValue(value))));
			buttonToValue.put(button, value);

			if(value instanceof IConfigurableExtension) {
				IExtensionConfiguration configuration = ((IConfigurableExtension)value).getConfiguration();
				if(configuration.getConfigurationOptions().stream().anyMatch(ConfigurableExtensionWidget::isOptionSupported)) {
					Group subGroup = new Group(group, SWT.NONE);
					subGroup.setLayout(new GridLayout());
					subGroup.setLayoutData(new GridData());
					createOptionControls(subGroup, configuration);
					nestedOptions.put(value, subGroup);
				}
			}
		}
		updateVisibilities.run();
		return group;
	}

	protected static <T> boolean isOptionSupported(ConfigurationOption<T> option) {
		Class<T> type = option.getType();
		return option.getSelectableValues() != null
				|| type == Boolean.class || type == Short.class || type == Byte.class || type == Integer.class
				|| type == String.class || type == Float.class || type == Double.class || type == Long.class || type.isEnum();
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
	 * @param addWidget the function to add a widget to the wizard page
	 */
	public static <W extends IWidgetDependence & IWidgetModification<? extends IConfigurableExtension>>
		void addAllForWidget(Composite parent, W widget, BiConsumer<Composite,IWidget> addWidget) {

		Map<IConfigurableExtension,ConfigurableExtensionWidget> extensionWidgets = new HashMap<>();
		for(IConfigurableExtension extension : widget.getSelectableValues()) {
			if(extension.getConfiguration().getConfigurationOptions().stream().anyMatch(ConfigurableExtensionWidget::isOptionSupported)) {
				ConfigurableExtensionWidget extensionWidget = new ConfigurableExtensionWidget(extension);
				extensionWidget.setDependency(widget);
				extensionWidgets.put(extension, extensionWidget);
				addWidget.accept(parent, extensionWidget);
				extensionWidget.setVisible(widget.getSelection().contains(extension)); // after adding the widget
			}
		}

		widget.addModificationListener((oldValues, newValues) ->
			extensionWidgets.forEach((key, value) -> value.setVisible(newValues.contains(key))));
	}
}
