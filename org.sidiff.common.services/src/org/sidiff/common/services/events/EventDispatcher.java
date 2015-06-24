package org.sidiff.common.services.events;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages the listeners for a certain type of events.
 * Each event comes with its own event dispatcher in order to
 * avoid the bottleneck of a single event bus.
 * @author wenzel
 *
 */
public class EventDispatcher {
	
	private Class<?> eventClass = null;
	private Set<SCEventListener> listeners = null;
	
	public EventDispatcher(Class<? extends SCEvent> eventClass) {
		this.eventClass=eventClass;
	}
	
	public boolean addEventListener(SCEventListener listener){
		
		assert(listener!=null);

		if(listeners==null){
			this.listeners= new HashSet<SCEventListener>();
		}
		
		return this.listeners.add(listener);
	}
	
	public boolean removeEventListener(SCEventListener listener){
		
		if(this.listeners!=null){
			boolean removed = this.listeners.remove(listener);
			if(removed&&this.listeners.size()==0){
				// Disable Propagation
				this.listeners=null;
			}
			return removed;
		} else {
			return false;
		}
		
	}
	
	public boolean fireEvent(SCEvent event){
		
		assert(event != null) : "Dispatched event cannot be null!";
		assert(this.eventClass==null || event.getClass()==this.eventClass) : "Dispatcher was not designed to perform given event!";

		if(listeners!=null){
			for(SCEventListener listener : this.listeners){
				listener.eventDispatched(event);
			}
			return true;
		}
		
		return false;
	}
	
	public boolean isEmpty() {
		return this.listeners==null;
	}
	
}
