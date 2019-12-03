package org.sidiff.common.logging.internal;

import java.text.SimpleDateFormat;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;
import org.sidiff.common.CommonPlugin;
import org.sidiff.common.logging.ILogChannel;
import org.sidiff.common.logging.LogEvent;

/**
 * LogChannels that forwards the log messages to the log service of the OSGi framework.
 */
public class OSGILogChannel implements ILogChannel {

	private BundleContext context;
	private ServiceReference<LoggerFactory> factoryServiceRef;
	private boolean hasPrintedError = false;
	
	@Override
	public SimpleDateFormat createDateFormat() {
		return new SimpleDateFormat("EE dd.MM.yy hh:mm:ss ");
	}

	@Override
	public void log(String message, LogEvent event) {
		LoggerFactory factory = getLoggetFactory();
		if (factory != null) {
			Logger logger = factory.getLogger(OSGILogChannel.class);
			switch(event) {
				case CONFIG:
				case EVENT:
				case SIGNAL:
				case DEBUG:
					logger.debug(message);
					break;
				case ERROR:
					logger.error(message);
					break;
				case INFO:
				case MESSAGE:
				case NOTICE:
					logger.info(message);
					break;
				case WARNING:
					logger.warn(message);
					break;
			}
		} else {
			System.out.println("OSGI-LOG " + event.name() + ": " + message);
		}
	}
	
	private LoggerFactory getLoggetFactory() {
		if(context == null){
			if(CommonPlugin.isActivated()) {
				context = CommonPlugin.getBundleContext();
			} else {
				System.err.println("ERROR - OSGI LogChannel cannot get OSGI Context!");
			}
		}

		if(factoryServiceRef == null) {
			 ServiceReference<LoggerFactory> serviceRef = context.getServiceReference(LoggerFactory.class);
			 if(serviceRef != null) {
				 factoryServiceRef = serviceRef;
			 } else if (!hasPrintedError) {
				 System.err.println("ERROR - OSGI LogChannel cannot get OSGI LoggerFactory service!");
				 hasPrintedError = true;
			 }
		}

		if(factoryServiceRef != null) {
			try {
				return context.getService(factoryServiceRef);
			} catch (Exception e) {
				System.err.println("ERROR - OSGI LogChannel cannot get OSGI LoggerFactory!");
			}			
		}
		return null;
	}
	
	@Override
	public boolean doIndentation() {
		return false;
	}

	@Override
	public boolean includeLogEvent() {
		return false;
	}

	@Override
	public boolean includeTimeStamp() {
		return false;
	}

	@Override
	public String getKey() {
		return getClass().getSimpleName();
	}
}