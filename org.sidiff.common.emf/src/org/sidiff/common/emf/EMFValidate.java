package org.sidiff.common.emf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.sidiff.common.emf.exceptions.InvalidModelException;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;

public class EMFValidate {

	private static int minimumSeverity = Diagnostic.WARNING;

	public static void validateObject(EObject... eObjects) throws InvalidModelException {
		LogUtil.log(LogEvent.NOTICE, "------------------------------------------------------------");
		LogUtil.log(LogEvent.NOTICE, "-------------------------- Validate ------------------------");
		LogUtil.log(LogEvent.NOTICE, "------------------------------------------------------------");

		StringBuilder builder = new StringBuilder();
		for (EObject eObject : eObjects) {
			List<String> errors = new ArrayList<>();
			List<String> warnings = new ArrayList<>();
			List<String> infos = new ArrayList<>();

			String name = EMFUtil.getEObjectSignatureName(eObject);

			LogUtil.log(LogEvent.NOTICE, "Validating: " + eObject);
			Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eObject);
			if (diagnostic.getSeverity() >= minimumSeverity) {
				if (builder.length() > 0) {
					builder.append(" ;");
				}
				builder.append(name).append(": ;");

				LogUtil.log(LogEvent.MESSAGE, diagnostic.getMessage());
				for (Diagnostic childDiagnostic : diagnostic.getChildren()) {
					switch (childDiagnostic.getSeverity()) {
						case Diagnostic.ERROR:
							LogUtil.log(LogEvent.ERROR, "\t" + childDiagnostic.getMessage());
							errors.add(childDiagnostic.getMessage());
							break;
						case Diagnostic.WARNING:
							LogUtil.log(LogEvent.WARNING, "\t" + childDiagnostic.getMessage());
							warnings.add(childDiagnostic.getMessage());
							break;
						case Diagnostic.INFO:
							LogUtil.log(LogEvent.INFO, "\t" + childDiagnostic.getMessage());
							infos.add(childDiagnostic.getMessage());
							break;
					}
				}
				if (!warnings.isEmpty()) {
					builder.append("- - - - - - - - - - WARNINGS - - - - - - - - - -;")
							.append(warnings.stream().collect(Collectors.joining(";")));
				}
				if (!errors.isEmpty()) {
					builder.append("- - - - - - - - - - ERRORS - - - - - - - - - - - -;")
						.append(errors.stream().collect(Collectors.joining(";")));
				}
				if (!infos.isEmpty()) {
					builder.append("- - - - - - - - - - INFOS - - - - - - - - - - - -;")
						.append(infos.stream().collect(Collectors.joining(";")));
				}
			}
		}
		if (builder.length() > 0) {
			throw new InvalidModelException(builder.toString());
		}
		LogUtil.log(LogEvent.NOTICE, "Validation successful [Min. Severity: " + minimumSeverity + "]");
	}

	/**
	 * Convenient method for validation a Resource, this just delegates the
	 * validation for each root node of the resource.
	 * 
	 * @param model
	 *            resource to validate
	 * @throws InvalidModelException
	 *             if there are some negative validation results
	 */
	public static void validateModel(Resource model) throws InvalidModelException {
		for (EObject root : model.getContents()) {
			validateObject(root);
		}
	}

	/**
	 * Convenient method for validation a ResourceSet, this just delegates the
	 * validation for each root Resource of the ResourceSet.
	 * 
	 * @param model
	 *            resource set to validate
	 * @throws InvalidModelException
	 *             if there are some negative validation results
	 */
	public static void validateModel(ResourceSet model) throws InvalidModelException {
		for (Resource root : model.getResources()) {
			validateModel(root);
		}

	}

	/**
	 * Sets the minimum severity which is needed for throwing
	 * an @{InvalidModelException}. If not set, defaults to @{Diagnostic.Warning}
	 * and thus includes warnings as well as errors according to EMF Diagnostic.
	 * @param severity
	 */
	public static void setMinimumSeverity(int severity) {
		minimumSeverity = severity;
	}

	public static int getMinimumSeverity() {
		return minimumSeverity;
	}
}
