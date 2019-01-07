package org.sidiff.common.emf.modelstorage;


import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

public class XmiIdResourceImpl extends XMIResourceImpl {

	public XmiIdResourceImpl(URI uri){
		super(uri);
	}

	@Override
	protected boolean useIDs() {
		return true;
	}

	@Override
	protected boolean useUUIDs() {
		return true;
	}

	@Override
	protected boolean assignIDsWhileLoading() {
		return true;
	}
}
