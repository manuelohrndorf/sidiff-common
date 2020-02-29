package org.sidiff.common.ui.util;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheet;

/**
 * A utility class to interact with the default property sheet and it's pages.
 * @author rmueller
 */
public class PropertySheetUtil {

	public static PropertySheet getPropertySheet() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if(activePage != null) {
			IViewPart view = activePage.findView(IPageLayout.ID_PROP_SHEET);
			if(view != null) {
				if(view instanceof PropertySheet) {
					return (PropertySheet)view;
				}
			}
		}
		return null;
	}

	public static <T extends IPropertySheetPage> T getCurrentPropertySheetPage(Class<T> pageType) {
		PropertySheet propertySheet = getPropertySheet();
		if(propertySheet != null) {
			IPage page = propertySheet.getCurrentPage();
			if(pageType.isInstance(page)) {
				return pageType.cast(page);
			}			
		}
		return null;
	}

	public static void notifySelectionChanged(IWorkbenchPart part, ISelection selection) {
		IPropertySheetPage page = getCurrentPropertySheetPage(IPropertySheetPage.class);
		if(page != null) {
			page.selectionChanged(part, selection);
		}
	}
}
