package org.sidiff.common.extension;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

/**
 * <p>An abstract extension implements {@link IExtension}, and
 * overrides {@link #getKey()} and {@link #getName()} to use the plugin manifest,
 * using the {@value #ATTRIBUTE_KEY} and {@value #ATTRIBUTE_NAME} attributes of the
 * extension's element, or the "ID" and "Name" attributes of the extension.</p>
 * <p>For {@link ITypedExtension}s, use {@link AbstractTypedExtension} instead.</p>
 * @author Robert Müller
 */
public abstract class AbstractExtension implements IExtension, IExecutableExtension {

	public static final String ATTRIBUTE_KEY = "key";
	public static final String ATTRIBUTE_NAME = "name";

	/**
	 * The extension's key
	 */
	private String key;

	/**
	 * The extension's name
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

		key = doGetKey(config);
		name = doGetName(config);
	}

	protected static String getFirstUseableValue(Stream<Supplier<String>> suppliers) {
		return suppliers
			.map(supplier -> supplier.get())
			.filter(Objects::nonNull)
			.filter(s -> !s.isEmpty())
			.findFirst()
			.orElseThrow(() -> new NoSuchElementException("No useable value found, suppliers should always return a default"));
	}

	protected String doGetKey(IConfigurationElement config) {
		return getFirstUseableValue(Stream.of(
				() -> config.getAttribute(ATTRIBUTE_KEY),
				config.getDeclaringExtension()::getSimpleIdentifier,
				IExtension.super::getKey));
	}

	protected String doGetName(IConfigurationElement config) {
		return getFirstUseableValue(Stream.of(
				() -> config.getAttribute(ATTRIBUTE_NAME),
				config.getDeclaringExtension()::getLabel,
				IExtension.super::getName));
	}

	/**
	 * <p>Returns the Key of the extension. The following are used in this order:</p>
	 * <ol>
	 * <li>The attribute {@value #ATTRIBUTE_KEY} of the extension's element in the manifest.</li>
	 * <li>The attribute "ID" of the extension.</li>
	 * <li>The default implementation of {@link IExtension#getKey()}</li>
	 * </ol>
	 * @return the extension's key
	 */
	@Override
	public String getKey() {
		return checkInitialized(key);
	}

	/**
	 * <p>Returns the Name of the extension. The following are used in this order:</p>
	 * <ol>
	 * <li>The attribute {@value #ATTRIBUTE_NAME} of the extension's element in the manifest.</li>
	 * <li>The attribute "Name" of the extension.</li>
	 * <li>The default implementation of {@link IExtension#getName()}</li>
	 * </ol>
	 * @return the extension's name
	 */
	@Override
	public String getName() {
		return checkInitialized(name);
	}

	protected static <T> T checkInitialized(T object) {
		if(object == null) {
			throw new IllegalStateException("Extension was not initialized. Extensions must be created using the extension manager.");
		}
		return object;
	}
}
