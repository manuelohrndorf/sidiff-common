//TODO Review, Asserts, Doku
package org.sidiff.common.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.util.ReflectionUtil;

/**
 * This is a helper class to access services from a given context. It should be used thoroughly within the SiDiff project.
 * If the requested service is a {@link ConfigurableService}, the request is delegated to the {@link ServiceConfigurator}.
 * Otherwise it is deletated to the given {@link BundleContext} directly. 
 * If the requested service is a {@link ProvidableService}, the respective {@link ServiceProvider} is requested and triggered to create a new instance of the service.
 * @author wenzel
 *
 */
public class ServiceHelper {
	
	private static final String RANKING = "service.ranking";
	static final String VARIANT = "org.sidiff.common.services.ServiceHelper.variant";
	static final String DOCTYPE = "org.sidiff.common.services.ServiceHelper.doctype";
	public static final String DEFAULT = "org.sidiff.common.services.ServiceHelper.default";
	
	/**
	 * Sorts service references by their ranking.
	 * @author wenzel
	 *
	 */
	static class ServiceComparator implements Comparator<ServiceReference> {
		@Override
		public int compare(ServiceReference o1, ServiceReference o2) {
			int i1, i2;
			try {
				i1 = Integer.parseInt((String)o1.getProperty(RANKING));
			} catch (NumberFormatException e) {
				i1 = 0;
			}
			try {
				i2 = Integer.parseInt((String)o2.getProperty(RANKING));
			} catch (NumberFormatException e) {
				i2 = 0;
			}
			return i2-i1;
		}
	}
	
	/**
	 * Returns the hierarchy of service interfaces implemented by the given service.
	 * @param service
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static List<Class<? extends Service>> getServiceHierachy(Class<?> clazz){
		ArrayList<Class<? extends Service>> result = new ArrayList<Class<? extends Service>>();
		if (Service.class.isAssignableFrom(clazz)) {
			result.add((Class<? extends Service>)clazz);
			getServiceHierachy(result, (Class<? extends Service>)clazz);
		}
		return result;
	}
	
	private static void getServiceHierachy(ArrayList<Class<? extends Service>> result, Class<? extends Service> iface){
		Class<? extends Service> parent = getServiceInterface(iface);
		if (parent!=null&&!result.contains((Class<? extends Service>)parent)) {
			result.add((Class<? extends Service>)parent);
			getServiceHierachy(result, parent);
		}
	}
	
	@SuppressWarnings("unchecked")
	static Class<? extends Service> getServiceInterface(Class<?> clazz) {
		Class<?>[] ifaces = clazz.getInterfaces();
		if (ifaces==null||ifaces.length==0) {
			throw new SiDiffRuntimeException("Unable to determine service type! You must not use anonymous classes to implement services!");
		}
		Class<? extends Service> result = null;
		for (int i=0; i<ifaces.length; i++) {
			if (Service.class.isAssignableFrom(ifaces[i]) && ifaces[i] != Service.class) {
				if (result!=null) {
					throw new SiDiffRuntimeException("Ambiguous hierarchy of service interfaces: ",clazz, "\nfound ",result," and ",ifaces[i]);
				}
				result = (Class<? extends Service>)ifaces[i];
			}
		}
		return result;
	}
	
	/**
	 * Returns the hierarchy of service provider interfaces implemented by the given service provider.
	 * @param provider
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static List<Class<? extends ServiceProvider<?>>> getProviderHierachy(ServiceProvider<?> provider){
		ArrayList<Class<? extends ServiceProvider<?>>> result = new ArrayList<Class<? extends ServiceProvider<?>>>();
		getProviderHierachy(result, (Class<? extends ServiceProvider<?>>)provider.getClass());
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static void getProviderHierachy(ArrayList<Class<? extends ServiceProvider<?>>> result, Class<? extends ServiceProvider<?>> iface){
		Class<?>[] ifaces = iface.getInterfaces();
		boolean found = false;
		for (int i=0; i<ifaces.length; i++) {
			if (ServiceProvider.class.isAssignableFrom(ifaces[i]) && ifaces[i] != ServiceProvider.class) {
				if (found) {
					throw new SiDiffRuntimeException("Ambiguous hierarchy of service provider interfaces: ",iface);
				}
				if (!result.contains((Class<? extends ServiceProvider<?>>)ifaces[i])) {
					result.add((Class<? extends ServiceProvider<?>>)ifaces[i]);
					getProviderHierachy(result, (Class<? extends ServiceProvider<?>>)ifaces[i]);
				}
//				if (ifaces[i].getGenericInterfaces().length==1 && ifaces[i].getGenericInterfaces()[0] instanceof ParameterizedType) {
//					ParameterizedType pt = (ParameterizedType)ifaces[i].getGenericInterfaces()[0];
//					if (pt.getRawType() != ServiceProvider.class) {
//						throw new IllegalArgumentException();
//					}
//					Class<? extends ProvidableService> type = (Class<? extends ProvidableService>)pt.getActualTypeArguments()[0];
//					List<Class<? extends Service>> list = getServiceHierachy(type);
//					for (Class<? extends Service> sc: list) {
//						if (ProvidableService.class.isAssignableFrom(sc) && sc != ProvidableService.class) {
//							System.out.println(sc.getName());
//							Class<? extends ServiceProvider<?>> pc = null;
//							try {
//								pc = (Class<? extends ServiceProvider<?>>)Class.forName(sc.getName()+ServiceProvider.PROVIDER_SUFFIX, true, sc.getClassLoader());
//								System.out.println(pc.getName());
//								if (pc != null) {
//									result.add(pc);
//									getProviderHierachy(result, pc);
//								}
//							} catch (ClassNotFoundException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//					}
//				}
				found = true;
			}
		}
	}
	
	/**
	 * Registers a service in the OSGi framework
	 * @param context
	 * @param service
	 */
	public static void registerService(BundleContext context, Class<?> clazz, Object service, String docType, String variant) {
		registerService(context, clazz, service, docType, variant, null);
	}
	
	public static void registerService(BundleContext context, Class<?> clazz, Object service, String docType, String variant, Dictionary<String, String> props) {
		if (DEFAULT.equals(variant))
			variant = null; // TODO Diese Zeile muss raus sobald alle Bundles mit explizitem DEFAULT registriert werden.
		if (!clazz.isAssignableFrom(service.getClass()))
			throw new UnsupportedOperationException();
		int ranking = 25;
		if (props == null) {
			props = new Hashtable<String, String>();
		}
		if (docType != null) {
			props.put(DOCTYPE, docType);
		}
		if (variant != null) {
			props.put(VARIANT, variant);
		}
		if (!Service.class.isAssignableFrom(clazz)) {
			props.put(RANKING, ""+ranking--);
			context.registerService(clazz.getName(), service, props);
		} else {
		//context.registerService(clazz.getName(), service, props);
			for (Class<? extends Service> sc: getServiceHierachy(clazz)) {
				props.put(RANKING, ""+ranking--);
				context.registerService(sc.getName(), service, props);
			}
		}
	}
	
	/**
	 * Registers a service provider in the OSGi framework
	 * @param context
	 * @param provider
	 * 
	 * FIXME Unregister auch notwendig?
	 */
	public static void registerServiceProvider(BundleContext context, Class<?> clazz, ServiceProvider<?> provider, String docType, String variant) {
		registerServiceProvider(context, clazz, provider, docType, variant, null);
	}
	
	public static void registerServiceProvider(BundleContext context, Class<?> clazz, ServiceProvider<?> provider, String docType, String variant, Dictionary<String, String> props) {
		if (DEFAULT.equals(variant))
			variant = null; // TODO Diese Zeile muss raus sobald alle Bundles mit explizitem DEFAULT registriert werden.
		int ranking = 25;
		if (props == null) {
			props = new Hashtable<String, String>();
		}
		if (docType != null) {
			props.put(DOCTYPE, docType);
		}
		if (variant != null) {
			props.put(VARIANT, variant);
		}
		props.put(RANKING, ""+ranking--);
		context.registerService(clazz.getName(), provider, props);
		for (Class<? extends ServiceProvider<?>> pc: getProviderHierachy(provider)) {
			props.put(RANKING, ""+ranking--);
			context.registerService(pc.getName(), provider, props);
		}
	}
	
	static String getFilter(boolean scrict, String docType, String variant) {
		String filter = null;
		if (docType != null) {
			filter = "(" + DOCTYPE + "=" + docType + ")";
			if (!scrict) {
				filter = "(|" + filter + "(!("+DOCTYPE+"=*)))";
			}
		}
		if (variant != null) {
			if (filter != null) filter = filter+"(" + VARIANT + "=" + variant + ")";
			else filter = "(" + VARIANT + "=" + variant + ")";
		}
		if (docType != null && variant != null) {
			filter = "(&"+filter+")";
		}
		return filter;
	}

	/**
	 * Requests a service of the given service interface.
	 * @param <T>
	 * @param context
	 * @param serviceInterface
	 * @return
	 */
	public static <T> T getService(BundleContext context, Class<T> serviceInterface) {
		return getService(context, serviceInterface, null, null);
	}
	
	public static <T> T getService(BundleContext context, Class<T> serviceInterface, String docType) {
		return getService(context, serviceInterface, docType, null);
	}
	
	/**
	 * nix -> irgendein Service
	 * doctype -> 1. service mit diesem doctype, 2. service ohne doctype
	 * variant -> service mit dieser variante
	 * doctype+variant -> 1. service mit dieser variante + doctype, 2. service mit dieser variante ohne doctype
	 * @param <T>
	 * @param context
	 * @param serviceInterface
	 * @param docType
	 * @param variant
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getService(BundleContext context, Class<T> serviceInterface, String docType, String variant) {
		if (DEFAULT.equals(variant))
			variant = null; // TODO Diese Zeile muss raus sobald alle Bundles mit explizitem DEFAULT registriert werden.
		String clazzname = serviceInterface.getName();
		if (ProvidableService.class.isAssignableFrom(serviceInterface)) {
			clazzname += ServiceProvider.PROVIDER_SUFFIX;
		}
		ServiceReference[] refs;
		String filter = getFilter(true, docType, variant);
		try {
			refs = context.getServiceReferences(clazzname, filter);
		} catch (InvalidSyntaxException e) {
			throw new SiDiffRuntimeException(e);
		}
		if (refs == null || refs.length == 0) {
			filter = getFilter(false, docType, variant);
			try {
				refs = context.getServiceReferences(clazzname, filter);
			} catch (InvalidSyntaxException e) {
				throw new SiDiffRuntimeException(e);
			}
		}
		if (refs == null || refs.length == 0) {
			return null;
		}
		Arrays.sort(refs, new ServiceComparator());
		Object service = context.getService(refs[0]);
		if (ProvidableService.class.isAssignableFrom(serviceInterface)) {
			if (!(service instanceof ServiceProvider))
				throw new SiDiffRuntimeException(
						"ProvidableService ",
						serviceInterface,
						" has registered a service which is not a ServiceProvider: ",
						service);
			return (T) ((ServiceProvider<?>) service).createInstance();
		} else {
			return (T) service;
		}
	}
	
	private static HashMap<String, ServiceConfigurator> configurators = new HashMap<String, ServiceConfigurator>();
	
	//TODO BundleContext wird hier nicht mehr benötigt.
	public static void registerServiceConfigurator(BundleContext context, Class<?> interfaceClass, Class<? extends ConfigurableService> implementationClass) {
		ServiceConfigurator sc = new ServiceConfigurator(interfaceClass, implementationClass);
		configurators.put(interfaceClass.getName(), sc);
	}

	private static ServiceConfigurator getServiceConfigurator(Class<?> interfaceClass) {
		String clazzname = interfaceClass.getName();
		if (ProvidableService.class.isAssignableFrom(interfaceClass)) {
			clazzname += ServiceProvider.PROVIDER_SUFFIX;
		}
		return configurators.get(clazzname);
	}
	
	public static void configureInstance(BundleContext context, Class<?> interfaceClass, String docType, String variant, Object... configData) {
		ServiceConfigurator sc = getServiceConfigurator(interfaceClass);
		if (sc == null)
			throw new SiDiffRuntimeException("No such configurator: ",interfaceClass.getName());

		sc.registerVariantInstance(context, variant, docType, configData);
	}
	
	public static void unregisterInstances(BundleContext context, Class<?> interfaceClass) {
		ServiceConfigurator sc = getServiceConfigurator(interfaceClass);
		if (sc == null)
			throw new SiDiffRuntimeException("No such configurator: ",interfaceClass.getName());
		sc.unregisterVariantInstance(context, null, null);
	}
	
	public static void unregisterVariantInstance(BundleContext context, Class<?> interfaceClass, String docType, String variant) {
		ServiceConfigurator sc = getServiceConfigurator(interfaceClass);
		if (sc == null)
			throw new SiDiffRuntimeException("No such configurator: ",interfaceClass.getName());
		sc.unregisterVariantInstance(context, docType, variant);
	}

	public static boolean isAvailable(String interfaceClass) {
		try {
			Class<?> clazz = ReflectionUtil.loadClass(interfaceClass);
			return clazz!=null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}