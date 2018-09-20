package org.sidiff.common.extension.internal;

import java.util.Objects;

import org.sidiff.common.extension.IExtension;

/**
 * Basic, immutable extension description.
 * @author Robert Müller
 * @param <T> the extension type
 */
public class ExtensionDescription<T extends IExtension> implements IExtension.Description<T> {

	private final Class<T> extensionClass;
	private final String extensionPointId;
	private final String elementName;
	private final String classAttribute;

	public ExtensionDescription(final Class<T> extensionClass, final String extensionPointId,
			final String elementName, final String classAttribute) {
		this.extensionClass = Objects.requireNonNull(extensionClass, "extensionClass is null");
		this.extensionPointId = Objects.requireNonNull(extensionPointId, "extensionPointId is null");
		this.elementName = Objects.requireNonNull(elementName, "elementName is null");
		this.classAttribute = Objects.requireNonNull(classAttribute, "classAttribute is null");
	}

	@Override
	public Class<T> getExtensionClass() {
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
}
