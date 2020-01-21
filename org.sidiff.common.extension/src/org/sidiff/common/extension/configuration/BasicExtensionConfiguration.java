package org.sidiff.common.extension.configuration;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * A basic implementation of {@link IExtensionConfiguration}
 * that uses a fixed collection of {@link ConfigurationOption}s.
 * @author rmueller
 */
public class BasicExtensionConfiguration extends AbstractMapBasedExtensionConfiguration {

	private final Collection<ConfigurationOption<?>> options;

	/**
	 * Creates a new basic configuration using the given configuration options.
	 * @param options the configuration options
	 */
	public BasicExtensionConfiguration(Collection<ConfigurationOption<?>> options) {
		this.options = options;
	}

	@Override
	protected Consumer<Consumer<ConfigurationOption<?>>> getOptionsFactory() {
		return acceptor -> options.forEach(acceptor::accept);
	}
}
