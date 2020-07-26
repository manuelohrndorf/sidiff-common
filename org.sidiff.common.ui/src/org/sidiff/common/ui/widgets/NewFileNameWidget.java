package org.sidiff.common.ui.widgets;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.Path;
import org.sidiff.common.ui.widgets.IWidgetValidation.ValidationMessage.ValidationType;

/**
 * @author rmueller
 */
public class NewFileNameWidget extends TextSelectionWidget {

	private IContainer container;
	private Set<String> allowedExtensions = new HashSet<>();

	public NewFileNameWidget(IContainer container) {
		setTitle("File Name");
		this.container = Objects.requireNonNull(container);
	}

	public void setAllowedExtensions(Set<String> allowedExtensions) {
		this.allowedExtensions.clear();
		this.allowedExtensions.addAll(allowedExtensions);
	}

	@Override
	protected ValidationMessage doValidate() {
		if(getSelection().isEmpty()) {
			return new ValidationMessage(ValidationType.ERROR, "A file name is required.");
		}
		if(!Path.ROOT.isValidSegment(getSelection().get(0))) {
			return new ValidationMessage(ValidationType.ERROR, "The file name is not a valid path segment.");
		}
		Path path = new Path(getSelection().get(0));
		if(!allowedExtensions.isEmpty() && !allowedExtensions.contains(path.getFileExtension())) {
			return new ValidationMessage(ValidationType.ERROR, "The file must have one of the following file extensions: "
					+ allowedExtensions.stream().collect(Collectors.joining("', '", "'", "'")));
		}
		if(container.exists(path)) {
			return new ValidationMessage(ValidationType.ERROR, "A file with this name already exists at the specified location.");
		}
		return super.doValidate();
	}
}
