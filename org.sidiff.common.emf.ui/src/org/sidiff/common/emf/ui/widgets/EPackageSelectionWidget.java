package org.sidiff.common.emf.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.common.emf.ui.labelprovider.EPackageLabelProvider;
import org.sidiff.common.ui.widgets.AbstractListWidget;

/**
 * A widget to select a set of {@link EPackage}s from the {@link EPackage.Registry#INSTANCE}.
 * Title and lower/upper bounds can be adjusted, see {@link AbstractListWidget}.
 * @author Robert Müller
 */
public class EPackageSelectionWidget extends AbstractListWidget<EPackage> {

	private List<EPackage> ePackages;

	/**
	 * Creates an EPackage selection widget, which is not ordered,
	 * filterable and uses a suitable label provider for EPackages.
	 */
	public EPackageSelectionWidget() {
		super(EPackage.class);
		setOrdered(false);
		setFilterable(true);
		setLabelProvider(new EPackageLabelProvider());
	}

	@Override
	public List<EPackage> getSelectableValues() {
		if(ePackages == null) {
			// we cache this value because this is a costly operation
			ePackages = new ArrayList<>(EMFMetaAccess.getAllRegisteredEPackages());
		}
		return ePackages;
	}
}
