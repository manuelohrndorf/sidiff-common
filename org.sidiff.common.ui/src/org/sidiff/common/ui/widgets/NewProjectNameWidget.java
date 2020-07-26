package org.sidiff.common.ui.widgets;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.sidiff.common.ui.widgets.TextSelectionWidget;
import org.sidiff.common.ui.widgets.IWidgetValidation.ValidationMessage.ValidationType;

/**
 * @author rmueller
 */
public class NewProjectNameWidget extends TextSelectionWidget {

	public NewProjectNameWidget() {
		setTitle("Project Name");
	}

	@Override
	protected ValidationMessage doValidate() {
		if(getSelection().isEmpty()) {
			return new ValidationMessage(ValidationType.ERROR, "A project name is required.");
		}
		if(!Path.ROOT.isValidSegment(getSelection().get(0))) {
			return new ValidationMessage(ValidationType.ERROR, "The project name is not a valid path segment.");
		}
		if(ResourcesPlugin.getWorkspace().getRoot().getProject(getSelection().get(0)).exists()) {
			return new ValidationMessage(ValidationType.ERROR, "A project with this name already exists in the workspace.");
		}
		return super.doValidate();
	}
}
