package org.sidiff.common.services;

public class ServiceConfigurationRegistryOSGI implements ServiceConfigurationRegistry {

	final private static ServiceConfigurationRegistry IMPL = new Impl(); 
	
	public ServiceConfigurationRegistryOSGI(){}
	
	@Override
	public void xxx() {
		IMPL.xxx();	
	}
	
	private static class Impl implements ServiceConfigurationRegistry {

		@Override
		public void xxx() {
		
			
		}
		
	}

}
