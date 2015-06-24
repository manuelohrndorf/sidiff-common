//TODO Review, Asserts, Doku
package org.sidiff.common.services;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.sidiff.common.exceptions.SiDiffRuntimeException;

/**
 * The ServiceConfigurator is used to configure a service. It is registered at the 
 * OSGi framework in the first place. With the request of configuration, it
 * configures and registers the actual service (i.e.~an {@link ConfigurableService})
 * at the OSGi framework.
 * 
 * Due to differentiation between different service configurators, it es necessary
 * to create a subclass. The subclass is only required for useful naming. It does
 * not require any implementation but the same constructor as 
 * {@link ServiceConfigurator}. It is enough the delegate all constructor parameters
 * to {@link ServiceConfigurator}.
 * 
 * @author wenzel
 *
 */
public class ServiceConfigurator {

	public static final String CONFIGURATOR_SUFFIX = "Configurator";
	
	private Class<?> interfaceClass;
	private Class<? extends ConfigurableService> implementationClass;

	/**
	 * Creates a service configurator and registers it as an service itself.
	 * 
	 * @param context
	 * @param implementationClass
	 */
	//TODO: Warum wird für die Interface-Klasse nicht auch eine Subklasse von ConfigurableService erwartet?
	ServiceConfigurator(Class<?> interfaceClass, Class<? extends ConfigurableService> implementationClass) {
		this.interfaceClass = interfaceClass;
		this.implementationClass = implementationClass;
	}

	Class<?> getInterfaceClass() {
		return interfaceClass;
	}
	
	Class<? extends ConfigurableService> getImplementationClass() {
		return implementationClass;
	}

	/**
	 * Creates, configures, and registers a {@link ConfigurableService} regarding a variant declaration.
	 * 
	 * @param variant
	 *            The variant enables existence of two or more service instances configured for different model types.
	 * @param configData
	 *            The actual data for configuration. It is given to the service implementation.
	 */
	//FIXME SW Fix: Problem, wenn Variante schon existiert!
	void registerVariantInstance(BundleContext context, String variant, String docType, Object... configData) {
		try {
			// create the service
			ConfigurableService service = implementationClass.newInstance();
			// configure the service
			String confDocType = service.configure(configData);
			
			
			//FIXME Check only if not UML
			// Used for UML Profiles and Document Type will not match with Config Doc Type	
			// Needs to be revised for a more safe and generic approach 
			//
			// check if document type is supported by configuration
			if (confDocType == null || confDocType.equals(".*") || (docType!=null && (docType.matches(confDocType) || confDocType.contains("UML")))) {
				// register the configured service 
				if (service instanceof ServiceProvider<?>) {
					ServiceHelper.registerServiceProvider(context, getInterfaceClass(), (ServiceProvider<?>)service, docType, variant, service.getProperties());
				} else {
					ServiceHelper.registerService(context, getInterfaceClass(), service, docType, variant, service.getProperties());
				}
			} else {
				throw new SiDiffRuntimeException("Invalid document type. Mismatch between given type ("+docType+") and configuration (",confDocType,")");
			}
		} catch (Exception e) {
			throw new SiDiffRuntimeException("Unable to instantiate configureable service ", getInterfaceClass().getName()," ", e);
		}
	}

	void unregister(BundleContext context) {
		unregisterVariantInstance(context, null, null);
	}
	
	/**
	 * Unregisters a service variant.
	 * 
	 * @param variant
	 *            The variant that will be unregistered.
	 */
	void unregisterVariantInstance(BundleContext context, String docType, String variant) {
		String filter = ServiceHelper.getFilter(true, docType, variant);
		try {
			ServiceReference[] refs = context.getAllServiceReferences(getInterfaceClass().getName(), filter);
			if (refs != null) {
				for (ServiceReference reference : refs) {
					ConfigurableService service = (ConfigurableService) context.getService(reference);
					if (service!=null){
						service.deconfigure();
					}
					context.ungetService(reference);
				}
			}
		} catch (InvalidSyntaxException e) {
			throw new SiDiffRuntimeException("Unable to unregister configureable service ", getInterfaceClass().getName(), ". Problem with filter", e);
		}
	}

}
