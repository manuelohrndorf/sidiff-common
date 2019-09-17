package org.sidiff.common.emf.adapters.internal;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.emf.EMFUtil;
import org.sidiff.common.emf.adapters.ElementByIDAdapter;

/**
 * Adapter that holds an index of all model elements inside a resource.
 * The adapter is assigned to a Resource.
 * @author wenzel
 */
public class ElementByIDAdapterImpl extends AdapterImpl implements ElementByIDAdapter {

	private final Map<String, EObject> map;

	public ElementByIDAdapterImpl(Resource resource) {
		map = CollectionUtil.asStream(resource.getAllContents()).collect(Collectors.toMap(EMFUtil::getEObjectID, Function.identity()));
	}

	@Override
	public EObject getElement(String id) {
		return map.get(id);
	}

	@Override
	public boolean isAdapterForType(Object type) {
		return type == ElementByIDAdapter.class;
	}

	@Override
	public String toString() {
		return "ElementByIDAdapter[target=" + getTarget() + ", map=" + map + "]";
	}
}
