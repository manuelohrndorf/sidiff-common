package org.sidiff.common.extension;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;
import org.sidiff.common.extension.IExtension.Description;

/**
 * <p>A generic manager for typed extensions, extending {@link ExtensionManager}
 * to add functions to retrieve extensions for specific document types, as well
 * as generic extensions.</p>
 * <p>The interface {@link ITypedExtension} specifies an extension,
 * which supports only certain document types.</p>
 * <p>Parameter and return values should never be <code>null</code>.</p>
 * @param <T> the type of the extension, extending {@link ITypedExtension}
 * @author Robert Müller
 */
public class TypedExtensionManager<T extends ITypedExtension> extends ExtensionManager<T> {

	/**
	 * Creates a new, empty typed extension manager.
	 */
	public TypedExtensionManager() {
		super();
	}

	/**
	 * Creates a new, empty typed extension manager and loads the extensions
	 * from the extension point specified by the description.
	 * @param description description of the extension point
	 */
	public TypedExtensionManager(final Description<? extends T> description) {
		super(description);
	}

	/**
	 * Returns all extensions of this manager that support all of the specified document types,
	 * optionally also including extensions supporting the generic document type.
	 * @param documentTypes the document types
	 * @param includeGeneric whether to include generic extensions
	 * @return all extensions that support this document type, empty if none
	 */
	public final Collection<T> getExtensions(final Collection<String> documentTypes, final boolean includeGeneric) {
		Assert.isNotNull(documentTypes);
		return getExtensions().stream()
				.filter(e -> extensionSupportsType(e, documentTypes, includeGeneric))
				.collect(Collectors.toList());
	}

	/**
	 * Returns all extensions of this manager that support all document types, i.e. the generic document type.
	 * @return extensions supporting the generic document types, empty if none
	 */
	public final Collection<T> getGenericExtensions() {
		return getExtensions().stream()
				.filter(ITypedExtension::isGeneric)
				.collect(Collectors.toList());
	}

	/**
	 * <p>Returns the default extension of this manager.</p>
	 * <p>The default implementation of TypedExtensionManager returns any generic
	 * extension if this manager has any, else an empty optional is returned.</p>
	 * <p>Subclasses may override.</p>
	 * @return the default extension, or empty Optional if none
	 */
	@Override
	public Optional<T> getDefaultExtension() {
		return getGenericExtensions().stream().findFirst();
	}

	/**
	 * <p>Returns a default extension of this manager that supports all of the given document types.</p>
	 * <p>If any extension support specifically this document types, it is returned.
	 * Else, if any generic extension exists, it is returned. Else the returned Optional is empty.</p>
	 * <p>Subclasses may override.</p>
	 * @param documentTypes the document types, may contain {@link ITypedExtension#GENERIC_TYPE}
	 * @return {@link Optional} containing the default extension for this document type, or empty optional if none
	 */
	public Optional<T> getDefaultExtension(final Collection<String> documentTypes) {
		Assert.isNotNull(documentTypes);
		// 1. try to find a non-generic extension
		// 2. if none is available, find a generic one
		return
			Stream.concat(
				getExtensions(documentTypes, false).stream(),
				getGenericExtensions().stream()
			).findFirst();
	}

	/**
	 * Returns a set of all document types that are supported by this manager's extensions.
	 * @return set of document types, may contain {@link ITypedExtension#GENERIC_TYPE}
	 */
	public final Set<String> getSupportedDocumentTypes() {
		return getExtensions().stream()
				.map(ITypedExtension::getDocumentTypes)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	private static boolean extensionSupportsType(final ITypedExtension ext,
			final Collection<String> documentTypes, final boolean includeGeneric) {
		return (includeGeneric && ext.isGeneric())
				|| ext.getDocumentTypes().containsAll(documentTypes);
	}
}
