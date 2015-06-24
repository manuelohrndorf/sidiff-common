package org.sidiff.common.services;

/**
 * A configurable service provider is created and configured during registration by a 
 * {@link ServiceConfigurator}. It serves as a service provider in order to create 
 * new service instances whenever a service is requested.
 * 
 * Attention: A ConfigurableServiceProvider has to have the standard constructor 
 * for instantiation.
 * 
 * @author wenzel
 * 
 */
public interface ConfigurableServiceProvider<T extends ProvidableService> extends ConfigurableService, ServiceProvider<T> {
}