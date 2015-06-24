package org.sidiff.common.services;

/**
 * Interface that marks a service to be provideable. It means, that with each
 * request of the service a new instance is created and returned.
 * 
 * @see ServiceProvider
 * 
 * @author wenzel
 *
 */
public interface ProvidableService extends Service {
	
}
