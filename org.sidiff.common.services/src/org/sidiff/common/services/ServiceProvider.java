package org.sidiff.common.services;

/**
 * This interface describes the provider of a {@link ProvidableService}.
 * The {@link ProvidableService} has to be given as type parameter.
 * 
 * @author wenzel
 *
 * @param <T> type of {@link ProvidableService} that is instantiated
 * by this Provider
 */
public interface ServiceProvider<T extends ProvidableService> {

	public static final String PROVIDER_SUFFIX = "Provider";
	
	/**
	 * 
	 * @return Returns an instance of the {@link ProvidableService},
	 * which has been given as type parameter.
	 */
	public T createInstance();
	
}
