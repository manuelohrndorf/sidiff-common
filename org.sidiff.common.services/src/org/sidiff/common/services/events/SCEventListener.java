package org.sidiff.common.services.events;

import java.util.EventListener;

/**
 * A listener which gets informed whenever the particular event is fired.
 * @author wenzel
 *
 */
public interface SCEventListener extends EventListener {

	public void eventDispatched(SCEvent event);
}
