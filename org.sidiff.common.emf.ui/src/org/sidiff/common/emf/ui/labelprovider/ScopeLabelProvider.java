package org.sidiff.common.emf.ui.labelprovider;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.sidiff.common.emf.access.Scope;

public class ScopeLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		if(element instanceof Scope) {
			switch((Scope)element) {
				case RESOURCE: return "Single Resource";
				case RESOURCE_SET: return "Complete Resource Set";
			};
		}
		return super.getText(element);
	}
}