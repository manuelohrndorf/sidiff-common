package org.sidiff.common.emf.adapters;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

/**
 * The SiDiffAdapterFactory creates adapters that remain assigned to the target.
 * I.e. whenever the adapter is requested, the same instance is returned.
 * @author wenzel
 *
 */
public abstract class SiDiffAdapterFactory extends AdapterFactoryImpl implements AdapterFactory {

	private Object adapterType;

	public SiDiffAdapterFactory(Class<?> type) {
		this.adapterType = type;
	}

	public Object getAdapterType() {
		return adapterType;
	}

	@Override
	public boolean isFactoryForType(Object type) {
		return type == adapterType;
	}
}
