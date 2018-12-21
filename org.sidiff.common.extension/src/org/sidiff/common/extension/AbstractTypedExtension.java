package org.sidiff.common.extension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * <p>An abstract typed extension is an {@link AbstractExtension} and
 * also implements {@link ITypedExtension} to use the document types
 * specified as child elements in the plug-in manifest (plugin.xml).</p>
 * <p>The document types must be specified as individual child element
 * of the element that specifies the executable extension, each element
 * having the name {@value #ELEMENT_DOCUMENT_TYPE} and containing as text
 * the document type URI, or the value {@value #GENERIC_TYPE}.</p>
 * @author Robert Müller
 */
public abstract class AbstractTypedExtension extends AbstractExtension implements ITypedExtension {

	public static final String ELEMENT_DOCUMENT_TYPE = "documentType";

	/**
	 * Document types specified in the manifest, empty if none.
	 */
	private Set<String> documentTypes;

	/**
	 * Caches whether this is a generic extension.
	 */
	private boolean generic;

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		super.setInitializationData(config, propertyName, data);
		documentTypes = doGetDocumentTypes(config);
		generic = doGetGeneric(config);
	}

	protected Set<String> doGetDocumentTypes(IConfigurationElement config) {
		return Arrays.stream(config.getChildren(ELEMENT_DOCUMENT_TYPE))
			.map(IConfigurationElement::getValue)
			.collect(Collectors.toSet());
	}

	protected boolean doGetGeneric(IConfigurationElement config) {
		return ITypedExtension.super.isGeneric();
	}

	/**
	 * <p>Returns the document types specified by the extension
	 * element in the plug-in manifest (plugin.xml).</p>
	 * <p>These are the contents of the child elements with the
	 * name {@value #ELEMENT_DOCUMENT_TYPE} of the configuration element
	 * that specified the executable extension.</p>
	 * <p>The set is empty if no elements are found, and
	 * may contain the value {@value #GENERIC_TYPE}.</p>
	 */
	@Override
	public Set<String> getDocumentTypes() {
		return new HashSet<>(checkInitialized(documentTypes));
	}

	@Override
	public boolean isGeneric() {
		return generic;
	}
}
