package org.sidiff.common.emf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sidiff.common.emf.adapters.SiDiffAdapterFactory;

/**
 * We implemented our own EMFAdapter registry for comfortability and speedup reasons.
 */
public class EMFAdapter {

	public static final EMFAdapter INSTANCE = new EMFAdapter();

	private List<AdapterFactory> factories = new ArrayList<>();

	public EMFAdapter() {
	}

	public boolean addAdapterFactory(SiDiffAdapterFactory factory) {
		return factories.add(Objects.requireNonNull(factory));
	}

	public boolean removeAdapterFactory(SiDiffAdapterFactory factory) {
		return factories.remove(factory);
	}

	public <T> T adapt(Notifier target, Class<T> type) {
		Objects.requireNonNull(target, "target is null");
		Objects.requireNonNull(type, "type is null");

		if(type.isInstance(target)) {
			return type.cast(target);
		} else if (target instanceof EObject) {
			Adapter registeredAdapter = EcoreUtil.getRegisteredAdapter((EObject)target, type);
			if(type.isInstance(registeredAdapter)) {
				return type.cast(registeredAdapter);
			}
		} else if (target instanceof Resource) {
			Adapter registeredAdapter = EcoreUtil.getRegisteredAdapter((Resource)target, type);
			if(type.isInstance(registeredAdapter)) {
				return type.cast(registeredAdapter);
			}
		}
		AdapterFactory factory = EcoreUtil.getAdapterFactory(factories, type);
		if (factory != null) {
			Adapter newAdapter = factory.adapt(target, type);
			if(type.isInstance(newAdapter)) {
				return type.cast(newAdapter);
			}
		}
		throw new UnsupportedOperationException("EMFAdapter cannot adapt " + target.getClass().getName() + " to " + type.getName());
	}
}
