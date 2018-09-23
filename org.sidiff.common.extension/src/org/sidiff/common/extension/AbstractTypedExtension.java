package org.sidiff.common.extension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * <p>An abstract typed extension is an {@link AbstractExtension} and
 * also implements {@link ITypedExtension} to use the document types
 * specified as child elements in the plug-in manifest (plugin.xml).</p>
 * <p>The document types must be specified as individual child element
 * of the element that specifies the executable extension, each element
 * having the name <code>documentType</code> and containing as text
 * the document type URI or the {@link ITypedExtension#GENERIC_TYPE generic type}.</p>
 * @author Robert Müller
 */
public abstract class AbstractTypedExtension extends AbstractExtension implements ITypedExtension {

	private static final String DOCUMENT_TYPE_ELEMENT = "documentType";

	/**
	 * Document types specified in the manifest, empty if none.
	 */
	private Set<String> documentTypes = new HashSet<>();

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		super.setInitializationData(config, propertyName, data);
		Arrays.stream(config.getChildren(DOCUMENT_TYPE_ELEMENT))
			.map(IConfigurationElement::getValue)
			.forEach(documentTypes::add);
	}

	/**
	 * <p>Returns the document types specified by the extension
	 * element in the plug-in manifest (plugin.xml).</p>
	 * <p>These are the contents of the child elements with the
	 * name <code>documentType</code> of the configuration element
	 * that specified the executable extension.</p>
	 * <p>The set is empty if no elements are found, and
	 * may contain the {@link ITypedExtension#GENERIC_TYPE generic type}.</p>
	 */
	@Override
	public Set<String> getDocumentTypes() {
		return new HashSet<>(documentTypes);
	}
}
