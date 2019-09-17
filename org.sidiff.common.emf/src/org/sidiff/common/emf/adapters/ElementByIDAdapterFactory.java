package org.sidiff.common.emf.adapters;

import org.eclipse.emf.common.notify.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.adapters.internal.ElementByIDAdapterImpl;

/**
 * Factory for ElementByID adapters.
 * @author wenzel
 */
public class ElementByIDAdapterFactory extends SiDiffAdapterFactory {

	public ElementByIDAdapterFactory() {
		super(ElementByIDAdapter.class);
	}
	
	@Override
	protected Adapter createAdapter(Notifier target) {
		if(target instanceof Resource) {
			return new ElementByIDAdapterImpl((Resource)target);			
		}
		return null;
	}
}
