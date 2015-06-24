package org.sidiff.core.annotation.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.sidiff.common.io.IOUtil;
import org.sidiff.common.io.ResourceUtil;
import org.sidiff.common.services.ServiceHelper;
import org.sidiff.common.xml.XMLResolver;
import org.sidiff.core.annotation.AnnotationService;

/**
 * TODO impl-Paket soll nicht exportiert werden (wird derzeit von S3V benutzt)
 * Idee: S3V sollte die benoetigten internen Klassen kopieren (ant-skript)
 */
public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		ServiceHelper.registerServiceConfigurator(context, AnnotationService.class, AnnotationServiceImpl.class);
		ResourceUtil.registerClassLoader(this.getClass().getClassLoader());
		XMLResolver.getInstance().includeMapping(IOUtil.getInputStream("org.sidiff.core.annotation.default.dtdmap.xml"));
	}

	public void stop(BundleContext context) throws Exception {
		ServiceHelper.unregisterInstances(context, AnnotationService.class);
		ResourceUtil.unregisterClassLoader(this.getClass().getClassLoader());
	}

}
