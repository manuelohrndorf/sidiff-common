package org.sidiff.common.emf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sidiff.common.emf.adapters.SiDiffAdapterFactory;

/**
 * We implemented our own EMFAdapter registry for comfortabily and speedup reasons.
 */
public class EMFAdapter {

	public static final EMFAdapter INSTANCE = new EMFAdapter();

	private List<AdapterFactory> factories=null;

	public EMFAdapter() {
		this.factories = new ArrayList<AdapterFactory>();
	}

	public boolean addAdapterFactory(SiDiffAdapterFactory e) {
		return factories.add(e);
	}

	public boolean removeAdapterFactory(SiDiffAdapterFactory o) {
		return factories.remove(o);
	}

	public <T> T adapt(Notifier target, Class<T> type) {
		AdapterFactory factory = EcoreUtil.getAdapterFactory(factories, type);
		if (factory != null) {
			return type.cast(factory.adapt(target, type));
		} else if (target instanceof EObject) {
			return type.cast(EcoreUtil.getRegisteredAdapter((EObject)target, type));
		} else if (target instanceof Resource) {
			return type.cast(EcoreUtil.getRegisteredAdapter((Resource)target, type));
		}
		throw new UnsupportedOperationException("EMFAdapter cannot adapt "+target.getClass().getName());
	}
}
