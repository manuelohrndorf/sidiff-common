package org.sidiff.common.extension;

import java.util.Set;

/**
 * <p>An {@link IExtension extension} that supports only certain document types.</p>
 * <p>May also support all document types, i.e. be generic.</b>
 * <p>Parameter and return values should never be <code>null</code>.</p>
 * @author Robert Müller
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
}
