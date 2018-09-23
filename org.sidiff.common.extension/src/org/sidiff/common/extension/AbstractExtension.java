package org.sidiff.common.extension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

/**
 * <p>An abstract extension implements {@link IExtension}, and
 * overrides {@link #getKey()} and {@link #getName()} to
 * use the ID and Name specified by the extension element
 * in the plug-in manifest (plugin.xml).</p>
 * <p>This implies, that an extension element in the
 * manifest only has exactly one executable extension.</p>
 * <p>For {@link ITypedExtension}s, use {@link AbstractTypedExtension} instead.</p>
 * @author Robert Müller
 */
public abstract class AbstractExtension implements IExtension, IExecutableExtension {

	/**
	 * ID specified in the manifest, <code>null</code> if none
	 */
	private String key;

	/**
	 * Name specified in the manifest, empty if none
	 */
	private String name;

	/**
	 * 
	 * <p>Called to initialize the extension with the data
	 * specified in the plugin.xml file.</p>
	 * <p><b>When this method is overridden, <u>the super-implementation
	 * must be called first</u>:</b></p>
	 * <pre>
	 * public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	 * 	super.setInitializationData(config, propertyName, data);
	 * 	// config.getAttribute("...")
	 * }
	 * </pre>
	 * {@inheritDoc}
	 */
	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		key = config.getDeclaringExtension().getSimpleIdentifier();
		name = config.getDeclaringExtension().getLabel();
	}

	/**
	 * <p>Returns the ID of the extension element in the plug-in
	 * manifest (plugin.xml), if it was specified.</p>
	 * <p>Otherwise, the default implementation of {@link IExtension#getKey()} is used.</p>
	 */
	@Override
	public String getKey() {
		if(key != null && !key.isEmpty()) {
			return key;
		}
		return IExtension.super.getKey();
	}

	/**
	 * <p>Returns the Name of the extension element in the plug-in
	 * manifest (plugin.xml), if it was specified.</p>
	 * <p>Otherwise, the default implementation of {@link IExtension#getName()} is used.</p>
	 */
	@Override
	public String getName() {
		if(name != null && !name.isEmpty()) {
			return name;
		}
		return IExtension.super.getName();
	}
}
