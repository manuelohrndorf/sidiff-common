package org.sidiff.common.services;

/**
 * Interface that marks a service to be provideable and configurable. 
 * It means, that there is a {@link ServiceProvider} which can be
 * configured.
 * 
 * @see ServiceProvider
 * @see ConfigurableService
 * @see ConfigurableServiceProvider
 * 
 * @author wenzel
 *
 */
public interface ConfigurableProvidableService extends ProvidableService {

}
