package org.sidiff.core.annotation;

import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.services.ConfigurableService;
import org.sidiff.common.services.Service;

public interface AnnotationService extends ConfigurableService, Service {

	/**
	 * Annotates configured annotations to all elements of the given model.
	 * 
	 * @param model Resource containing the Model.
	 */
	public void annotate(Resource model);
	
	/**
	 * Annotates the given attribute to all elements of the given model.
	 * As a side effect, all required annotations keys are tried to annotate implicitly.
	 * 
	 * @param model Resource containing the Model.
	 * @param keys a set of configured annotation keys.
	 */
	public void annotate(Resource model, Set<String> keys);

	/**
	 * Annotates the given attribute to all elements of the given model.
	 * @param model Resource containing the Model.
	 * @param key a configured annotation key.
	 */
	public void annotate(Resource model, String key);
	
	/**
	 * Removes all annotations provided by the current service
	 * from the given model.
	 * 
	 * @param model Resource containing the Model.
	 */
	public void removeAnnotations(Resource model);
	
	/**
	 * Removes a set of annotations, provided by the current service 
	 * from the given model.
	 * 
	 * @param keys a set of configured annotation keys.
	 * @param model Resource containing the Model.
	 */
	public void removeAnnotations(Resource model, Set<String> keys);
	
	/**
	 * Removes all annotations from the given model.
	 * @param model
	 */
	public void removeAnnotations(Resource model, String key);
	
	/**
	 * Returns the set of available Annotation Keys, known to the service. 
	 * 
	 * @return The set of available Keys. 
	 */
	public Set<String> availableKeys();
	
	/**
	 * @param model
	 * @param key
	 * @return
	 */
	public Set<String> executedKeys(Resource model);
}
