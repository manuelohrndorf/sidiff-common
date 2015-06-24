package org.sidiff.common.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.services.debug.SiDiffDebugger;
import org.sidiff.common.services.events.EventDispatcher;
import org.sidiff.common.services.events.SCEvent;
import org.sidiff.common.services.events.SCEventListener;

/**
 * This class puts different service instances together in order to treat them
 * as services that have each other as context.
 * 
 * @author wenzel
 * 
 */
public class ServiceContext {
	
	/**
	 * Service context already initialized?
	 */
	private boolean isInitialized = false;
	/**
	 * Holds the registered services.
	 */
	private Map<Class<?>, Object> servicesInContext = null;
	/**
	 * A list of hidden services. These services need to be context-sensitive.
	 * They are initialized together with other services and they can work in 
	 * the service context. However, they are not accessible from outside. 
	 */
	private Set<ContextSensitiveService> hiddenServices = null;
	/**
	 * Holds the services' initialization parameters.
	 */
	private Map<ContextSensitiveService, int[]> initSequences = null;
	/**
	 * Holds the default initialization parameters (if set).
	 */
	private int[] defaultSequence = null;
	/**
	 * Holds the initialization parameters. Indicates that initializion is in progress. 
	 */
	private Object[] initParameters = null;
	/**
	 * Holds the already initialized Services (during the initializion)
	 */
	private Set<ContextSensitiveService> initializedServices = null;
	
	/**
	 * Holds the event dispatchers.
	 */
	private Map<Class<?>, EventDispatcher> eventDispatcher = null;
	/**
	 * Holds a global dispatcher.
	 */
	private EventDispatcher globalEventDispatcher = null;

	/**
	 * Constructor.
	 */
	public ServiceContext() {
		this.servicesInContext = new HashMap<Class<?>, Object>();
		this.initSequences = new HashMap<ContextSensitiveService, int[]>();
		this.eventDispatcher = new HashMap<Class<?>, EventDispatcher>();
		this.globalEventDispatcher = new EventDispatcher(null);
		this.hiddenServices = new HashSet<ContextSensitiveService>();
	}
	
	/**
	 * Adds a hidden service. These services need to be context-sensitive.
	 * They are initialized together with other services and they can work in 
	 * the service context. However, they are not accessible from outside. 
	 */
	public boolean putHiddenService(ContextSensitiveService e) {
		return this.hiddenServices.add(e);
	}

	/**
	 * Removes a hidden service. 
	 */
	public boolean removeHiddenService(Object o) {
		return this.hiddenServices.remove(o);
	}

	/**
	 * Checks whether the services of this context have already been
	 * initialized.
	 * 
	 * @return Services have already been initialized?
	 */
	public boolean isInitialized() {
		return this.isInitialized;
	}
	
	/**
	 * Initializes all context-sensitive services in this context.<br />
	 * Call this method only once otherwise it will fail.
	 * 
	 * @param params The parameters.
	 * @throws IllegalInitializationException Thrown when service context has already been initialized.
	 */
	public void initialize(Object... params)
	throws IllegalInitializationException {
		
		assert(this.isInitialized == false) : "Service context has already been initialized!";
		assert(this.putHiddenService(SiDiffDebugger.getInstance())): "Cannot enable Debugging System";
		
		if (this.isInitialized == true){
			throw new IllegalInitializationException("Service context has already been initialized! Reinitialization is not allowed.");
		}
		
		this.initParameters = params;
		
		// During the initializion 
		this.initializedServices = new HashSet<ContextSensitiveService>();
		
		/**
		 * Keine Garantien �ber Reihenfolge der Initialisierung. 
		 * 
		 * Problematisch wenn die init-Methode eines Service einen anderen Service nutzen will und dieser 
		 * noch nicht initialisiert wurde.
		 * 
		 * Bsp. CandidatesTreeServiceImpl.initialize greift auf CorrespondenceService zu.
		 * 
		 * PP 27.07.09
		 * 
		 * Der zugriff auf einen bestimmten Service waehrend der initialisierung des Kontextes veranlasst
		 * jetzt eine vorgezogene initialisierung des angeforderten dienstes.  
		 * 
		 * MS 27.07.09
		 * 
		 */
		
		// Initialize services
		for (Object service : servicesInContext.values()) {
			if (service instanceof ContextSensitiveService) {
				initialize((ContextSensitiveService) service);		
			}
		}
		
		for (ContextSensitiveService hidden : hiddenServices) {
			initialize(hidden);
		}
		
		// Now, this Context is finally initialized!
		this.initializedServices =null;
		
		this.isInitialized = true;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getInitialisingParameter(int i,Class<T> type){
		if(this.isInitialized){
			return (T)this.initParameters[i];
		} else {
			throw new NotInitializedYetException();
		}
		
	}
	
	private void initialize(ContextSensitiveService csService){
		
		assert(!isInitialized) : "Already Initialized!";
		assert(this.initParameters!=null) : "Missing initializion parameters";
		assert(this.initializedServices!=null) : "Missing already initialized services!";
		
		if (!this.initializedServices.contains(csService)) {
			// Construct the initialization index sequence.
			int[] svcInitParams = null;
			if (this.initSequences.containsKey(csService)) {
				// Use initialization parameters passed by user.
				svcInitParams = this.initSequences.get(csService);
			} else if (this.defaultSequence != null) {
				// Use default initialization parameters.
				svcInitParams = defaultSequence;
			} else {
				// Use all initialization parameters.
			}

			// Create Object Sequence
			Object[] svcParams = null;
			if (svcInitParams == null) {
				// Use all parameters.
				svcParams = this.initParameters;
			} else {
				// Use only specified parameters.
				svcParams = new Object[svcInitParams.length];
				for (int i = 0; i < svcInitParams.length; i++) {
					assert (this.initParameters.length > svcInitParams[i]) : "Cannot accsess index '" + svcParams[i] + "' more parameters needed";
					svcParams[i] = this.initParameters[svcInitParams[i]];
				}
			}

			// .. and initialize with context and parameters.
			csService.initialize(this, svcParams);
			this.initializedServices.add(csService);
		}
	}
	
	/**
	 * Set the default initialization parameters which will be used when
	 * on registration for a service were not passed specific initialization parameters
	 * and to prevent that in other case all parameters will be passed to service.<br />
	 * This method call is only allowed if service context was not initialized yet.
	 * 
	 * @param defaultParams The default initialization parameters.
	 * @throws IllegalInitializationException Thrown when service context is initialized.
	 */
	public void setDefaultParams(int... defaultParams)
	throws IllegalInitializationException {
		
		assert(this.isInitialized == false) : "Service context has already been initialized!";

		if (this.isInitialized == true)
			throw new IllegalInitializationException("Service context has already been initialized! Setting default params is not allowed.");
		
		this.defaultSequence = defaultParams;
	}
	
	/**
	 * Checks whether a instance of the given service is contained in this
	 * context.
	 * 
	 * @param serviceID The service ID.
	 * @return If given service is contained in this context.
	 */
	public boolean containsService(Class<?> serviceID) {
		
		return this.servicesInContext.containsKey(serviceID);
	}

	/**
	 * Checks whether the given service instance is part of this context.
	 * 
	 * @param service The service to check for.
	 * @return Whether the given service instance is part of this context.
	 */
	public boolean containsService(Object service) {
		
		return this.servicesInContext.containsValue(service);
	}

	@SuppressWarnings("unchecked")
	public <X> X getService(Class<X> serviceID) {

		Object service = this.servicesInContext.get(serviceID);
		if (service == null) {
			throw new MissingServiceException(serviceID);
		} else {
			// On demand initializion
			if(!isInitialized && service instanceof ContextSensitiveService){
				initialize((ContextSensitiveService)service);
			}
		}
		return (X) service;
	}

	/**
	 * Returns a list of services which are part of this context.
	 * 
	 * @return A list of services which are part of this context.
	 */
	public Set<Class<?>> supportedServices() {
		
		return this.servicesInContext.keySet();
	}
	
	/**
	 * Adds a service to this context.<br />
	 * This method call is only allowed if service context was not initialized yet.
	 * 
	 * @param serviceId The service ID.
	 * @param service Instance of the specific service.
	 * @param initParams The services initialization parameters.
	 * @return <code>null</code> if no service with specific
	 *         <code>serviceId</code> has been registered yet, otherwise the old
	 *         service which was replaced by new one.
	 * @throws IllegalInitializationException Thrown when service context is initialized.
	 */
	public Object putService(Class<?> serviceId, Object service, int... initParams)
	throws IllegalInitializationException {
		
		assert (this.isInitialized == false) : "Service context has already been initialized!";
		
		assert (service != null) : "Service to add is NULL.";
		assert (serviceId != null) : "Service ID to add is NULL.";
		assert (serviceId.isAssignableFrom(service.getClass())) : "Service ID is not assignable from service class.";
		
		if (this.isInitialized == true)
			throw new IllegalInitializationException("Service context has already been initialized! Reinitialization is not allowed.");

		Object obj = this.servicesInContext.put(serviceId, service);
	
		// Store initializionsequence if needed
		if(initParams.length>0){
			if(service instanceof ContextSensitiveService){
				this.initSequences.put((ContextSensitiveService)service, initParams);
			} else {
				assert(false): "Initializion parameter in conjunction of a non ContextSensitiveService implementing Service?!";
			}
		}
		
		return obj;
	}

	/**
	 * Adds a service to this context.<br />
	 * This method call is only allowed if service context was not initialized yet.
	 * 
	 * @param service Instance of the specific service.
	 * @param initParams The services initialization parameters.
	 * @return <code>null</code> if no service with specific
	 *         <code>serviceId</code> has been registered yet, otherwise the old
	 *         service which was replaced by new one.
	 * @return whether the service was registered for all provided interfaces.
	 * @throws IllegalInitializationException Thrown when service context is initialized.
	 **/
	public boolean putService(Service service, int... initParams) throws IllegalInitializationException {
		
		assert (this.isInitialized == false) : "Service context has already been initialized!";
		
		assert (service != null) : "Service to add is NULL.";
		
		if (this.isInitialized == true)
			throw new IllegalInitializationException("Service context has already been initialized! Adding new service is not allowed.");

		boolean result = true;

		if (!this.servicesInContext.containsValue(service)) {
			Class<?> serviceId = ServiceHelper.getServiceInterface(service.getClass());
			if (serviceId==null) {
				throw new SiDiffRuntimeException("Unable to determine service type! You must not use anonymous classes to implement services!");
			}
			List<Class<? extends Service>> typeHierarchy;
			if (serviceId!=null) {
				typeHierarchy = ServiceHelper.getServiceHierachy(serviceId);	
			} else {
				typeHierarchy = new ArrayList<Class<? extends Service>>();
				if (service instanceof Service) {
					typeHierarchy.add((Class<? extends Service>)service.getClass());
				}
			}
			
			assert (typeHierarchy != null && !typeHierarchy.isEmpty()) : "Service without Interface hierarchy?!";

			final Object replace = servicesInContext.get(typeHierarchy.get(0));
			for (Class<?> type : typeHierarchy) {
				if (!this.servicesInContext.containsKey(type) || this.servicesInContext.get(type) == replace) {
					// Service not registered yet or should be replaced
					this.servicesInContext.put(type, service);
				} else {
					// we hit a specialized Service, abort
					result &= false;
					break;
				}
			}
			
			// Store initializionsequence if needed
			if(initParams.length>0){
				if(service instanceof ContextSensitiveService){
					this.initSequences.put((ContextSensitiveService)service, initParams);
				} else {
					assert(false): "Initializion parameter in conjunction of a non ContextSensitiveService implementing Service?!";
				}
			}
		} else {
			// Service already registered
			if( (!this.initSequences.containsKey(service)&&initParams.length==0) 
			  ||( this.initSequences.containsKey(service)&&this.initSequences.get(service).equals(initParams))){
				// Service already registered with same initializion sequence
				result &= false;
			} else {
				throw new IllegalArgumentException("Inconclusive initializion sequence");
			}
		}

		return result;
	}

	/**
	 * Removes a service from this context. 
	 * 
	 * @param serviceId The service ID.
	 * @param service Instance of the specific service.
	 * @return The service instance if service was removed successfully, <code>null</code> if service was not registered in context.
	 * @throws IllegalInitializationException Thrown when service context is initialized.
	 */
	public Object removeService(Class<?> serviceId, Object service)
	throws IllegalInitializationException {
		
		assert (this.isInitialized == false) : "Service context has already been initialized!";

		assert (service != null) : "Service to remove is NULL.";
		assert (serviceId != null) : "Service ID to remove is NULL.";
		assert (serviceId.isAssignableFrom(service.getClass())) : "Service ID is not assignable from service class.";

		if (this.isInitialized == true)
			throw new IllegalInitializationException("Service context has already been initialized! Removing a service is not allowed.");
		
		// Remove initialization parameters if service is not registered in context any more.
		if (this.servicesInContext.containsValue(service) == false)
			this.initSequences.remove(service);
		
		Object obj = this.servicesInContext.remove(serviceId);
		
		return obj;
	}
	
	/**
	 * Returns all service instances which are part of this context.
	 * 
	 * @return A collection of all service instances registered in this context.
	 */
	public Collection<Object> getServices() {
		
		assert(isInitialized) : "getServices is NOT intended for use before or during the initialization!";
		
		return this.servicesInContext.values();
	}

	/**
	 * Returns the IDs of all services which are part of this context.
	 * 
	 * @return A collection of all service IDs registered in this context.
	 */
	public Collection<Class<?>> getServiceIDs() {
		
		assert(isInitialized) : "getServiceIDs is NOT intended for use before or during the initialization!";
		
		return this.servicesInContext.keySet();
	}

	/**
	 * Returns all service instances which part of this context and provide
	 * given Service
	 * 
	 * @return Computed set of all services.
	 */
	@SuppressWarnings("unchecked")
	public <T> Iterable<T> getServices(Class<? extends T> lookuptype) {

		assert(isInitialized) : "getServices is NOT intendet for use before or during the initializion!";
		
		LinkedList<T> result = new LinkedList<T>();
		for (Object service : servicesInContext.values()) {
			
			if(lookuptype.isInstance(service)){
				result.add((T)service);
			}
		}
		return result;
	}

	/**
	 * Fires the given event and delivers it to all listeners which are
	 * registered for this type of event.
	 * 
	 * @param event
	 * @return whether the event was propagated.
	 */
	public boolean fireEvent(SCEvent event) {
		
		assert(isInitialized) : "Context not initialized!";
	
		boolean result = false;
		
		if (this.eventDispatcher != null) {
			Class<? extends SCEvent> eventclass = event.getClass();
			if (this.eventDispatcher.containsKey(eventclass)) {
				result |= this.eventDispatcher.get(eventclass).fireEvent(event);
			}
		}
		
		if(!this.globalEventDispatcher.isEmpty()){
			result |= this.globalEventDispatcher.fireEvent(event);
		}
		
		return result;
	}

	/**
	 * Registers an event listener for a particular type of events.
	 * 
	 * @param eventtype
	 * @param listener
	 * @return
	 */
	public boolean addEventListener(Class<? extends SCEvent> eventtype,
			SCEventListener listener) {

		EventDispatcher dispatcher = null;
		
		if(eventtype==null){
			dispatcher = this.globalEventDispatcher;
		} else {
			// Look for suitable dispatcher
			dispatcher= this.eventDispatcher.get(eventtype);
			if (dispatcher == null) {
				dispatcher = new EventDispatcher(eventtype);
				this.eventDispatcher.put(eventtype, dispatcher);
			}
		}

		return dispatcher.addEventListener(listener);
	}

	/**
	 * Removes an event listener from a particular type of events.
	 * 
	 * @param eventtype
	 * @param listener
	 */
	public void removeEventListner(Class<? extends SCEvent> eventtype, SCEventListener listener) {

		if (eventtype == null) {
			this.globalEventDispatcher.removeEventListener(listener);
		} else {
			EventDispatcher dispatcher = this.eventDispatcher.get(eventtype);
			if (dispatcher != null) {
				dispatcher.removeEventListener(listener);
				if (dispatcher.isEmpty()) {
					this.eventDispatcher.remove(eventtype);
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public class MissingServiceException extends SiDiffRuntimeException {
		public MissingServiceException(Class<?> missing) {
			super(missing.getName());
		}

	}
	@SuppressWarnings("serial")
	public class NotInitializedYetException extends SiDiffRuntimeException {
	}
	
}
