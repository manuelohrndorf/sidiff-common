package org.sidiff.common.extension.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.sidiff.common.extension.IExtension;

/**
 * Basic, immutable extension description.
 * @author rmueller
 * @param <T> the extension type
 */
public class ExtensionDescription<T extends IExtension> implements IExtension.Description<T> {

	private final Class<? extends T> extensionClass;
	private final String extensionPointId;
	private final String elementName;
	private final String classAttribute;

	public ExtensionDescription(final Class<? extends T> extensionClass, final String extensionPointId,
			final String elementName, final String classAttribute) {
		this.extensionClass = Objects.requireNonNull(extensionClass, "extensionClass is null");
		this.extensionPointId = Objects.requireNonNull(extensionPointId, "extensionPointId is null");
		this.elementName = Objects.requireNonNull(elementName, "elementName is null");
		this.classAttribute = Objects.requireNonNull(classAttribute, "classAttribute is null");
	}

	@Override
	public Class<? extends T> getExtensionClass() {
		return extensionClass;
	}

	@Override
	public String getExtensionPointId() {
		return extensionPointId;
	}

	@Override
	public String getElementName() {
		return elementName;
	}

	@Override
	public String getClassAttribute() {
		return classAttribute;
	}

	@Override
	public Stream<IConfigurationElement> getRegisteredExtensions() {
		return Stream.of(RegistryFactory.getRegistry().getConfigurationElementsFor(getExtensionPointId()))
				.filter(element -> element.getName().equals(getElementName()));
	}

	@Override
	public Optional<T> createExecutableExtension(IConfigurationElement element) {
		try {
			final Object rawExtension = element.createExecutableExtension(getClassAttribute());
			final T extension = getExtensionClass().cast(rawExtension);
			ExtensionsPlugin.logInfo("Created executable extension " + extension.getKey()
				+ " of " + element.getDeclaringExtension().getContributor().getName());
			return Optional.of(extension);
		} catch (Exception | LinkageError e) {
			// We also catch LinkageError because it may be thrown if the executable
			// extension class is not found or incompatible with the environment.
			ExtensionsPlugin.logError("Failed to create executable extension contributed by "
					+ element.getDeclaringExtension().getContributor().getName()
					+ " for extension point " + getExtensionPointId(), e);
			return Optional.empty();
		}
	}
}
