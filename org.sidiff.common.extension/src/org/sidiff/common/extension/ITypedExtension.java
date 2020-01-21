package org.sidiff.common.extension;

import java.util.Collections;
import java.util.Set;

/**
 * <p>An {@link IExtension extension} that supports only certain document types.</p>
 * <p>May also support all document types, i.e. be generic.</b>
 * <p>Parameter and return values should never be <code>null</code>.</p>
 * @author rmueller
 */
public interface ITypedExtension extends IExtension {

	/**
	 * A constant value for the "generic" document type,
	 * i.e. <i>any</i> document type.
	 */
	String GENERIC_TYPE = "generic";

	/**
	 * Returns all document types that this extension supports.
	 * May also contain the generic document type
	 * ({@link ITypedExtension#GENERIC_TYPE})
	 * if all document types are supported.
	 * @return collection of all document types that this extension supports
	 */
	Set<String> getDocumentTypes();

	/**
	 * <p>Returns whether this extension is generic.</p>
	 * <p>The default implementation returns whether {@link #getDocumentTypes()}
	 * contains {@value #GENERIC_TYPE}.</p>
	 * @return <code>true</code> if generic, <code>false</code> otherwise
	 */
	default boolean isGeneric() {
		return getDocumentTypes().contains(GENERIC_TYPE);
	}

	/**
	 * <p>Returns whether this extension can handle the set of document types.</p>
	 * <p>The default implementation returns <code>true</code>, if the extension
	 * {@link #isGeneric() is generic} or {@link #getDocumentTypes() its document types}
	 * contain the all document types of the specified set.
	 * @param documentTypes
	 * @return
	 */
	default boolean canHandle(Set<String> documentTypes) {
		return isGeneric() || getDocumentTypes().containsAll(documentTypes);
	}

	/**
	 * <p>Returns whether this extension can handle the document type.</p>
	 * <p>The default implementation delegates to {@link #canHandle(Set)}
	 * using a singleton collection.</p>
	 * @param documentType
	 * @return
	 */
	default boolean canHandle(String documentType) {
		return canHandle(Collections.singleton(documentType));
	}
}
