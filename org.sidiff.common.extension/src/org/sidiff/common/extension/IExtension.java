package org.sidiff.common.extension;

import java.util.Optional;

import org.sidiff.common.extension.internal.ExtensionDescription;
import org.sidiff.common.util.RegExUtil;

/**
 * <p>An extension class, which can be managed using an {@link ExtensionManager}.</p>
 * <p>An extension may specify a key for indexing, as well as a human-readable
 * name and description. Default implementations are provided for convenience.</p>
 * <p>An extension is usually associated with an eclipse extension point,
 * for which a description can be define by using {@link Description#of}.</p>
 * <p>Extensions may also implement {@link ITypedExtension}.</p>
 * <p>Parameter and return values should never be <code>null</code>.</p>
 * @author Robert Müller
 */
public interface IExtension {

	/**
	 * <p>Returns a key that uniquely identifies this extension.</p>
	 * <p>The default implementation returns <code>getClass().getName()</code>.</p>
	 * @return unique key / identifier
	 */
	default String getKey() {
		return getClass().getName();
	}

	/**
	 * <p>Returns a human-readable name for this extension.</p>
	 * <p>The default implementation returns <code>getClass().getName()</code>
	 * for anonymous classes, else <code>getClass().getSimpleName()</code>,
	 * with spaces added to split camel case.</p>
	 * @return readable name
	 */
	default String getName() {
		if(getClass().isAnonymousClass()) {
			return RegExUtil.addSpacesToCamelCase(getClass().getName());
		}
		return RegExUtil.addSpacesToCamelCase(getClass().getSimpleName());
	}

	/**
	 * <p>Returns an optional human-readable description for this extension.</p>
	 * <p>The default implementation always returns an empty optional.</p>
	 * @return readable description (optional)
	 */
	default Optional<String> getDescription() {
		return Optional.empty();
	}


	/**
	 * A description of an eclipse extension point for an extension of type {@link T}.
	 * @param <T> the type of the extension, extending {@link IExtension}
	 * @see ExtensionManager#ExtensionManager(Description)
	 * @see TypedExtensionManager#TypedExtensionManager(Description)
	 */
	interface Description<T extends IExtension> {

		/**
		 * Returns the runtime class of this extension.
		 * @return class object of this extension
		 */
		Class<T> getExtensionClass();

		/**
		 * Returns the extension point ID.
		 * @return the extension point ID
		 */
		String getExtensionPointId();

		/**
		 * Returns the name of the extension's element.
		 * @return the name of the extension's element
		 */
		String getElementName();

		/**
		 * Returns the class attribute of the extensions element, i.e. the executable extension.
		 * @return the name of the class attribute
		 */
		String getClassAttribute();

		/**
		 * Returns an extension description with the given arguments.
		 * @param extensionClass the extension's runtime class
		 * @param extensionPointId the extension point ID
		 * @param elementName the name of the extension's element
		 * @param classAttribute the name of the class attribute
		 * @return extension description
		 */
		static <T extends IExtension> Description<T> of(Class<T> extensionClass,
				String extensionPointId, String elementName, String classAttribute) {
			return new ExtensionDescription<T>(extensionClass, extensionPointId, elementName, classAttribute);
		}
	}
}
