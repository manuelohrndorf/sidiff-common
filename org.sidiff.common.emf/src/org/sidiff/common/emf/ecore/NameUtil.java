package org.sidiff.common.emf.ecore;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EStringToStringMapEntryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class NameUtil {

	private static final Map<String, String> dict;

	static {
		dict = new HashMap<>();
		dict.put("SET", "Set");
		dict.put("UNSET", "Unset");
		dict.put("ADD", "Add");
		dict.put("CREATE", "Create");
		dict.put("DELETE", "Delete");
		dict.put("MOVE", "Move");
		dict.put("REMOVE", "Remove");
		dict.put("CHANGE", "Change");
		dict.put("NOT", "Not");
		dict.put("REFERENCE", "Reference");
		dict.put("MOVEs", "Moves");
		dict.put("CHANGEs", "Changes");
		dict.put("FROM", "From");
		dict.put("TO", "To");
		dict.put("IN", "In");
		dict.put("ATTRIBUTE", "Attribute");
		dict.put("Id", "ID");
		dict.put("TGT", "Target");
		dict.put("SRC", "Source");
		dict.put("AND", "And");
	}

	private static String dictionary(String input) {
		return dict.getOrDefault(input, input);
	}

	public static String beautifyName(String name) {
		return translate(capitalizeFirstLetter(removeCamelCase(name.replace('_', ' '))));
	}

	public static String removeCamelCase(String name) {
		if (!name.matches("^[a-zA-Z]*$")) {
			return name;
		}
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1 $2";
		return name.replaceAll(regex, replacement);
	}

	private static String capitalizeFirstLetter(String input) {
		StringBuilder result = new StringBuilder(input.length());
		String[] words = input.split("\\s");

		for (int i = 0; i < words.length; ++i) {
			if (i > 0) {
				result.append(" ");
			}

			if (words[i].length() > 1) {
				result.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
			}
		}
		return result.toString();
	}

	private static String translate(String input) {
		StringBuilder result = new StringBuilder(input.length());
		String[] words = input.split("\\s");

		for (int i = 0; i < words.length; ++i) {
			if (i > 0) {
				result.append(" ");
			}
			result.append(dictionary(words[i]));
		}
		return result.toString();
	}

	public static String getName(EObject eObject) {
		if (eObject == null) {
			return "null";
		}
		if (eObject instanceof EAnnotation) {
			EAnnotation annotation = (EAnnotation) eObject;
			String res = "Annotation: " + annotation.getSource();
			return res;
		}

		else if (eObject instanceof EStringToStringMapEntryImpl) {
			EStringToStringMapEntryImpl entryImpl = (EStringToStringMapEntryImpl) eObject;
			String res = "MapEntry: " + entryImpl.getKey() + " -> " + entryImpl.getValue();
			if (entryImpl.eContainer() != null){
				res += " in \"" + getName(entryImpl.eContainer()) + "\"";
			}

			return res;
		}

		else {
			// Generic name search:
			String name = "[" + eObject.eClass().getName() + "]";

			// Check for attribute "name":
			EStructuralFeature attrName = eObject.eClass().getEStructuralFeature("name");
			if (attrName != null && attrName instanceof EAttribute) {
				Object nameAttrValue = eObject.eGet(attrName);

				if (nameAttrValue instanceof String) {
					name = (String) nameAttrValue;
				}
			}
			return name;
		}
	}

	public static String getQualifiedArgumentName(EObject eObject){
		String label = "";
		EObject eContainer = eObject.eContainer();
		if (eObject instanceof EAnnotation) {

			EAnnotation eAnnotation = (EAnnotation) eObject;
			String path = "";
			while (eContainer != null) {
				for (EAttribute a : eContainer.eClass().getEAllAttributes()) {
					if (a.getName().equalsIgnoreCase("name")) {
						path = eContainer.eGet(a) + "." + path;
					}
				}

				eContainer = eContainer.eContainer();
			}
			label = String.format("%s%s (%s)", path, eAnnotation.getSource(),
					eAnnotation.eClass().getName());
			return label;
		}

		for (EAttribute attribute : eObject.eClass().getEAllAttributes()) {
			if (attribute.getName().equalsIgnoreCase("name")) {
				String path = "";
				while (eContainer != null) {
					for (EAttribute a : eContainer.eClass().getEAllAttributes()) {
						if (a.getName().equalsIgnoreCase("name")) {
							path = eContainer.eGet(a) + "." + path;
						}
					}

					eContainer = eContainer.eContainer();
				}

				label = String.format("%s%s (%s)", path, eObject
						.eGet(attribute), eObject.eClass().getName());

				return label;
			}
		}

		for (EAttribute attribute : eObject.eClass().getEAllAttributes()) {
			if (attribute.getName().equalsIgnoreCase("id")) {
				String path = "";
				while (eContainer != null) {
					for (EAttribute a : eContainer.eClass().getEAllAttributes()) {
						if (a.getName().equalsIgnoreCase("id")) {
							path = eContainer.eGet(a) + "." + path;
						}
					}
					eContainer = eContainer.eContainer();
				}
				label = String.format("%s.%s", eObject.eClass().getName(),
						eObject.eGet(attribute));
				return label;
			}
		}

		String[] fragments = EcoreUtil.getURI(eObject).toString().split("\\.");
		String indexFragment = fragments[fragments.length - 1];

		if (indexFragment.matches("\\d+")) {
			label = String.format("%s.%s (%s)", eObject.eContainingFeature()
					.getName(), fragments[fragments.length - 1], eObject
					.eClass().getName());
		} else {
			label = eObject.eClass().getName();
		}


		return label;
	}

}
