package org.sidiff.common.extension.ui.widgets;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.sidiff.common.extension.IExtension;
import org.sidiff.common.extension.configuration.*;
import org.sidiff.common.ui.widgets.*;

/**
 * <p>A widget which provides suitable inputs for the various options of the
 * {@link org.sidiff.common.extension.configuration.IExtensionConfiguration configuration}
 * of a {@link IConfigurableExtension}.</p>
 * <p>Use {@link #addAllForWidget} to create and add multiple widgets for all extensions
 * managed by a widget and automatically handle toggling of widget visibility.</p>
 * @author rmueller
 */
public class ConfigurableExtensionWidget extends AbstractContainerWidget {

	private IConfigurableExtension rootExtension;

	ConfigurableExtensionWidget(IConfigurableExtension extension) {
		super(DefaultContainerFactory.EXPANDABLE);
		this.rootExtension = Objects.requireNonNull(extension);
		setTitle(getGroupTitle(extension));
	}

	@Override
	protected Composite createContents(Composite container) {
		return createOptionControls(container, rootExtension);
	}

	protected Composite createOptionControls(Composite container, IConfigurableExtension extension) {
		Composite composite = new Composite(container, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(composite);
		for(ConfigurationOption<?> option : extension.getConfiguration().getConfigurationOptions()) {
			createConfigurationOptionControl(composite, option, extension);
		}
		return composite;
	}

	// also creates expandable composite around createOptionControls, returns empty optional if no configuration option has UI support
	protected Optional<Composite> createNestedOptionControls(Composite container, IConfigurableExtension extension) {
		if(!extension.getConfiguration().getConfigurationOptions().stream()
				.anyMatch(ConfigurableExtensionWidget::isOptionSupported)) {
			return Optional.empty();
		}
		return Optional.of(
				DefaultContainerFactory.EXPANDABLE.createContainer(
					container,
					getGroupTitle(extension),
					getWidgetCallback(),
					composite -> createOptionControls(composite, extension)));
	}

	@SuppressWarnings("unchecked") // we explicitly check below
	protected <T> Control createConfigurationOptionControl(Composite parent, ConfigurationOption<T> option, IConfigurableExtension extension) {
		if(option.getSelectableValues() != null) {
			if(option.isMulti()) {
				return createMultipleChoiceControl(parent, option, extension);
			}
			return createSingleChoiceControl(parent, option, extension);
		}
		if(option.isMulti()) {
			// The widget only supports multi-options with fixed selectable values
			return null;
		}

		final Class<T> type = option.getType();
		if(type == Boolean.class) {
			return createCheckboxControl(parent, (ConfigurationOption<Boolean>)option, extension);
		} else if(type == Short.class || type == Byte.class || type == Integer.class) {
			return createNumberControl(parent, (ConfigurationOption<? extends Number>)option, extension);
		} else if(type == String.class || type == Float.class || type == Double.class || type == Long.class) {
			return createTextControl(parent, option, extension);
		}
		return null;
	}

	protected Control createCheckboxControl(Composite parent, ConfigurationOption<Boolean> option, IConfigurableExtension extension) {
		Button check = new Button(parent, SWT.CHECK);
		check.setText(option.getName());
		check.setToolTipText(getOptionToolTipText(option, extension));
		if(option.isSet()) {
			check.setSelection(option.getValue());
		}
		check.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> option.setValue(check.getSelection())));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(check);
		return check;
	}

	protected Control createNumberControl(Composite parent, ConfigurationOption<? extends Number> option, IConfigurableExtension extension) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(option.getName());
		group.setLayout(new GridLayout(1, true));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);

		Spinner spinner = new Spinner(group, SWT.NONE);
		spinner.setToolTipText(getOptionToolTipText(option, extension));
		if(option.isSet()) {
			spinner.setSelection(option.getValue().intValue());
		}
		spinner.addSelectionListener(SelectionListener.widgetSelectedAdapter(
				e -> option.setValueUnsafe(String.valueOf(spinner.getSelection()))));
		option.getMinValue().ifPresent(minValue -> spinner.setMinimum(minValue.intValue()));
		option.getMaxValue().ifPresent(maxValue -> spinner.setMaximum(maxValue.intValue()));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(spinner);
		return group;
	}

	protected Control createTextControl(Composite parent, ConfigurationOption<?> option, IConfigurableExtension extension) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(option.getName());
		group.setLayout(new GridLayout(1, true));
		GridDataFactory.fillDefaults().grab(true,  false).applyTo(group);

		Text text = new Text(group, SWT.NONE);
		if(option.isSet()) {
			text.setText(String.valueOf(option.getValue()));
		}
		text.setToolTipText(getOptionToolTipText(option, extension));
		text.addModifyListener(e -> option.setValueUnsafe(text.getText()));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
		return group;
	}

	protected <T> Control createSingleChoiceControl(Composite parent, ConfigurationOption<T> option, IConfigurableExtension extension) {
		if(option.getValue() == null) {
			option.resetToDefault();
			if(option.getValue() == null) {
				// Preselect first value as default if no default value set
				option.getSelectableValues().stream().findFirst().ifPresent(option::setValue);
			}
		}

		Group group = new Group(parent, SWT.NONE);
		group.setText(option.getName());
		group.setToolTipText(getOptionToolTipText(option, extension));
		group.setLayout(new GridLayout(1, true));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);

		Map<Button,Composite> nestedOptions = new HashMap<>();
		Runnable updateVisibilities = () -> {
			nestedOptions.forEach((button, composite) -> {
				composite.setVisible(button.getSelection());
				((GridData)composite.getLayoutData()).exclude = !button.getSelection();
				composite.requestLayout();
				getWidgetCallback().requestLayout();
			});
		};

		for(T value : option.getSelectableValues()) {
			Button button = new Button(group, SWT.RADIO);
			button.setText(option.getLabel(value));
			button.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
				option.setValue(value);
				updateVisibilities.run();
			}));
			if(option.getLabel(value).equals(option.getLabel(option.getValue()))) {
				button.setSelection(true);
			}
			GridDataFactory.fillDefaults().grab(true, false).applyTo(button);
			if(value instanceof IExtension) {
				((IExtension)value).getDescription()
					.ifPresent(description -> button.setToolTipText(description));
				if(value instanceof IConfigurableExtension) {
					createNestedOptionControls(group, (IConfigurableExtension)value)
						.ifPresent(subComposite -> nestedOptions.put(button, subComposite));
				}
			}
		}
		updateVisibilities.run();
		return group;
	}

	protected <T> Control createMultipleChoiceControl(Composite parent, ConfigurationOption<T> option, IConfigurableExtension extension) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(option.getName());
		group.setToolTipText(getOptionToolTipText(option, extension));
		group.setLayout(new GridLayout(1, true));

		Map<Button,T> buttonToValue = new LinkedHashMap<>();
		Map<Button,Composite> nestedOptions = new HashMap<>();
		Runnable updateVisibilities = () -> {
			nestedOptions.forEach((button, composite) -> {
				composite.setVisible(button.getSelection());
				((GridData)composite.getLayoutData()).exclude = !button.getSelection();
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
			button.setText(option.getLabel(value));
			button.addSelectionListener(selectionListener);
			button.setSelection(option.getValues().stream()
					.anyMatch(v -> option.getLabel(v).equals(option.getLabel(value))));
			buttonToValue.put(button, value);

			if(value instanceof IExtension) {
				((IExtension)value).getDescription()
					.ifPresent(description -> button.setToolTipText(description));
				if(value instanceof IConfigurableExtension) {
					createNestedOptionControls(group, (IConfigurableExtension)value)
						.ifPresent(subComposite -> nestedOptions.put(button, subComposite));
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
	 * @param <W> The type of the widget for which to create new configuration widgets
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

	private static String getGroupTitle(IConfigurableExtension extension) {
		return "Configuration Options: " + extension.getName();
	}

	private static <T> String getOptionToolTipText(ConfigurationOption<T> option, IConfigurableExtension extension) {
		return option.getDescription()
				.orElseGet(() -> "Option '" + option.getKey() + "' "
					+ (option.getType() == Boolean.class ? "" : "(" + option.getType().getSimpleName() + ") ")
					+ "of '" + extension.getKey() + "'");
	}
}
