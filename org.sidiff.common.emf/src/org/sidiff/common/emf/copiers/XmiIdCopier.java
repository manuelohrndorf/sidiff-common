package org.sidiff.common.emf.copiers;

import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.sidiff.common.emf.EMFUtil;

/**
 * Extends the {@link org.eclipse.emf.ecore.util.EcoreUtil.Copier} to also copy XmiIds from original to copied EObjects.
 * @author rmueller
 */
public class XmiIdCopier extends Copier {

	private static final long serialVersionUID = -7906189342176650323L;

	@Override
	public void copyReferences() {
		super.copyReferences();
		copyXmiIds();
	}

	protected void copyXmiIds() {
		forEach((original, copy) -> {
			String id = EMFUtil.getXmiId(original);
			if(id != null) {
				EMFUtil.setXmiId(copy, id);
			}
		});
	}
}
