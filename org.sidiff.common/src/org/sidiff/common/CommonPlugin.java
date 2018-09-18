package org.sidiff.common;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.sidiff.common.util.ObjectUtil;
import org.sidiff.common.util.StringUtil;
import org.sidiff.common.util.internal.*;

/**
 * The Common-Plugin bundle activator, which initializes
 * and deinitializes this plugin.
 * 
 * @author Maik Schmidt, Robert Müller
 */
public class CommonPlugin implements BundleActivator {

	/**
	 * The bundle ID
	 */
	public static final String ID = "org.sidiff.common";

	/**
	 * Stores the context in which the bundle is running, if given.
	 */
	private static BundleContext context = null;

	@Override
	public void start(BundleContext context) throws Exception {
		CommonPlugin.context = context;

		// register all provided StringResolvers
		StringUtil.addStringResolver(new ArrayStringResolver());
		StringUtil.addStringResolver(new CollectionStringResolver());
		StringUtil.addStringResolver(new ExceptionStringResolver());
		StringUtil.addStringResolver(new ErrorStringResolver());
		StringUtil.addStringResolver(new HashTableStringResolver());
		StringUtil.addStringResolver(new MapStringResolver());
		StringUtil.addStringResolver(new ThreadStringResolver());
		StringUtil.addStringResolver(new StackTraceStringResolver());

		// register all ObjectConverter
		ObjectUtil.registerConverter(new StringConverter());
		ObjectUtil.registerConverter(new IntegerConverter());
		ObjectUtil.registerConverter(new GenericListConverter());
		ObjectUtil.registerConverter(new GenericMapConverter());
		ObjectUtil.registerConverter(new GenericSetConverter());
		ObjectUtil.registerConverter(new FloatConverter());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// unregister all provided StringResolvers
		StringUtil.removeStringResolver(ArrayStringResolver.class);
		StringUtil.removeStringResolver(CollectionStringResolver.class);
		StringUtil.removeStringResolver(ExceptionStringResolver.class);
		StringUtil.removeStringResolver(ErrorStringResolver.class);
		StringUtil.removeStringResolver(HashTableStringResolver.class);
		StringUtil.removeStringResolver(MapStringResolver.class);
		StringUtil.removeStringResolver(ThreadStringResolver.class);
		StringUtil.removeStringResolver(StackTraceStringResolver.class);

		CommonPlugin.context = null;
	}

	/**
	 * Returns the Common-Plugin's execution context.
	 * @return the bundle context of this plugin
	 */
	public static BundleContext getBundleContext() {
		if(context == null)
			throw new IllegalStateException("plugin is not activated");
		return context;
	}

	/**
	 * Returns whether the Common-Plugin is activated.
	 * @return <code>true</code> if activated, <code>false</code> otherwise
	 */
	public static boolean isActivated() {
		return context != null;
	}
}
