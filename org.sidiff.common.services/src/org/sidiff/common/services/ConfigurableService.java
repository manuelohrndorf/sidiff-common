package org.sidiff.common.services;

import java.util.Dictionary;

/**
 * A configureable service is created and configured during registration 
 * by a {@link ServiceConfigurator}.
 * 
 * Attention: A ConfigurableService requires the standard constructor for instantiation.
 * 
 * @author wenzel
 * 
 */
public interface ConfigurableService {

	/**
	 * Method is called to configure the service.
	 * 
	 * @param configData
	 *            The data needed for configuration.
	 * @return The document type for which the service will be configured. 
	 * 		   The document type is given as String, which is the name of 
	 *         the document type or a regular expression that matches 
	 *         multiple document types.
	 *         In SiDiff we use the namespace URI of the respective EMF metamodel.
	 */
	public String configure(Object... configData);

	/**
	 * Method is called when the service gets unregistered. It can be used for cleaning.
	 */
	public void deconfigure();

	/**
	 * Properties which can be defined by the service.
	 * 
	 * @return
	 */
	public Dictionary<String, String> getProperties();

}