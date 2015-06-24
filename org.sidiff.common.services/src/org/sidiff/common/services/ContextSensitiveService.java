package org.sidiff.common.services;

/**
 * Marker interface for services that rely on the ServiceContext.
 * @author wenzel
 *
 */
public interface ContextSensitiveService {

	/**
	 * Initialize service with the underlying parameters.
	 * 
	 * @param serviceContext The underlying service context.
	 * @param params The parameters.
	 */
	public void initialize(ServiceContext serviceContext, Object... params);

}
