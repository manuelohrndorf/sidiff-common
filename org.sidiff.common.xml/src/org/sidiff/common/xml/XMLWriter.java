package org.sidiff.common.xml;

import java.io.*;
import java.util.*;

import org.sidiff.common.exceptions.SiDiffRuntimeException;

/**
 * Class to create XML files. 
 */
public class XMLWriter {

	private static final String TAB = "    ";

	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n";
	public static final String XML_COMMENT_VAR = "#COMMENT#";
	public static final String XML_COMMENT = "<!-- " + XML_COMMENT_VAR + " -->\r\n";
	public static final String DOCTYPE_ROOTELEMENT_VAR = "#ROOT#";
	public static final String DOCTYPE_TYPE_VAR = "#DOCTYPE#";
	public static final String DOCTYPE_TYPE_DEFINITION = "#DEFINITION#";
	public static final String XML_DOCTYPE = "<!DOCTYPE " + DOCTYPE_ROOTELEMENT_VAR + " SYSTEM \"" + DOCTYPE_TYPE_VAR + "\"" + DOCTYPE_TYPE_DEFINITION + ">\r\n";
	public static final String XML_CDATA_VAR = "#CDATA#";
	public static final String XML_CDATA = "<![CDATA[" + XML_CDATA_VAR + "]]>";

	private int indentLevel;
	private Stack<String> openElements;
	private Map<String, String> charTranslationMap;
	private Map<String, String> idTranslationMap;
	private Map<String, Vector<String>> idValidationMap;
	private Writer out;

	public XMLWriter(OutputStream outputStream) {
		this(new OutputStreamWriter(outputStream));
	}

	public XMLWriter(Writer writer) {
		this.indentLevel = 0;
		this.openElements = new Stack<>();
		this.out = writer;
		initCharTranslations();
	}

	public void setNamespace(String nsName, String URI) {
		throw new UnsupportedOperationException();
	}

	public void initDocument(String rootelement) {
		initDocument(null, null, rootelement, null);
	}

	public void initDocument(String doctype, String rootelement) {
		initDocument(doctype, null, rootelement, null);
	}

	public void initDocument(String doctype, String[][] idValidationMap, String rootelement) {
		initDocument(doctype, idValidationMap, rootelement, null);
	}

	public void initDocument(String doctype, String[][] idValidationMap, String rootelement, Map<String, String> attrs) {
		if (rootelement != null && !"".equals(rootelement)) {
			try {
				out.write(XML_HEADER);

				if (doctype != null) {
					out.write(XML_DOCTYPE.replace(DOCTYPE_ROOTELEMENT_VAR, rootelement).replace(DOCTYPE_TYPE_VAR, doctype).replace(DOCTYPE_TYPE_DEFINITION, ""));
				}
				if (idValidationMap != null) {
					initIdTranslations();
					initIdValidation(idValidationMap);
				}
				generateStartTag(rootelement, attrs);
			} catch (IOException e) {
				performException(e);
			}
		} else {
			throw new SiDiffRuntimeException("Cannot init Document -> invalid Rootelement" + rootelement);
		}
	}

	public void initDocumentEnhanced(String doctype, String rootelement, String doctypedefinition, Map<String, String> attrs) {
		if (rootelement != null && !"".equals(rootelement)) {
			try {
				out.write(XML_HEADER);

				if (doctype != null) {
					out.write(XML_DOCTYPE.replace(DOCTYPE_ROOTELEMENT_VAR, rootelement).replace(DOCTYPE_TYPE_VAR, doctype).replace(DOCTYPE_TYPE_DEFINITION, doctypedefinition));
				}
				generateStartTag(rootelement, attrs);
			} catch (IOException e) {
				performException(e);
			}
		} else {
			throw new SiDiffRuntimeException("Cannot init Document -> invalid Rootelement" + rootelement);
		}
	}

	public void finishDocument() {
		if (this.openElements.size() == 1) {
			generateEndTag(this.openElements.pop());
		} else if (this.openElements.size() > 1) {
			throw new SiDiffRuntimeException("Cannot finish Document until " + openElements.pop() + " is open!");
		} else {
			throw new SiDiffRuntimeException("Document already closed!");
		}

		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			performException(e);
		}

	}

	public void generateComment(String comment) {
		try {
			generateTabs();
			out.write(XML_COMMENT.replace(XML_COMMENT_VAR, comment));
		} catch (IOException e) {
			performException(e);
		}
	}

	public void generateStartTag(String name, Map<String, String> attrs) {
		try {
			generateStartOrEmptyTag(name, attrs);
			out.write(">\r\n");
			this.openElements.push(name);
		} catch (IOException e) {
			performException(e);
		}
	}

	public void generateEmptyTag(String name, Map<String, String> attrs) {
		try {
			generateStartOrEmptyTag(name, attrs);
			out.write("/>\r\n");
			indentLevel--;
		} catch (IOException e) {
			performException(e);
		}
	}

	public void generateEndTag(String name) {
		try {

			String expected_element = null;
			if (!openElements.isEmpty()) {
				expected_element = this.openElements.pop();
			}

			if (expected_element == null || name.equals(expected_element)) {
				indentLevel--;
				generateTabs();
				out.write("</" + name);
				out.write(">\r\n");
			} else {
				throw new SiDiffRuntimeException("Cannot close Element " + name + ", must close " + expected_element + " before!");
			}
		} catch (IOException e) {
			performException(e);
		}
	}

	public void generateText(String text, boolean indent) {
		text = XML_CDATA.replaceAll(XML_CDATA_VAR, text);
		try {
			if (indent) {
				StringBuilder tabs = new StringBuilder();
				for (int i = 0; i < indentLevel + 1; i++) {
					tabs.append(TAB);
				}
				text = tabs + text.replaceAll("\n", "\n" + tabs);
				if (!text.endsWith("\n")) {
					text += "\n";
				}
			}
			out.write(text);
		} catch (IOException e) {
			performException(e);
		}
	}

	private void generateTabs() throws IOException {
		StringBuilder tabs = new StringBuilder();
		for (int i = 0; i < indentLevel; i++) {
			tabs.append(TAB);
		}
		out.write(tabs.toString());
	}

	private void initIdValidation(String[][] idValidationMap) {
		this.idValidationMap = new HashMap<>();
		for (String[] entry : idValidationMap) {
			if (entry.length != 2) {
				throw new IllegalArgumentException("Id Validation Entry must have 2 Elements!\n" + entry + "\n" + idValidationMap + "\n");
			} else {
				Vector<String> idAttr = this.idValidationMap.get(entry[0]);
				if (idAttr == null) {
					idAttr = new Vector<>();
					this.idValidationMap.put(entry[0], idAttr);
				}
				idAttr.add(entry[1]);
			}
		}
	}

	private void initIdTranslations() {
		this.idTranslationMap = new HashMap<>();
		this.idTranslationMap.put(" ", "__");
		this.idTranslationMap.put(":", "...");
		this.idTranslationMap.put("\"", "---");
		this.idTranslationMap.put("\n", "_CR_");
		this.idTranslationMap.put("<", "_LT_");
		this.idTranslationMap.put(">", "_GT_");
		this.idTranslationMap.put("&", "_AND_");
		this.idTranslationMap.put("(", "_RO_");
		this.idTranslationMap.put(")", "_RC_");
		this.idTranslationMap.put("/", "-_-");
		this.idTranslationMap.put(",", "-.-");

	}

	private void initCharTranslations() {
		this.charTranslationMap = new HashMap<>();
		this.charTranslationMap.put("\n", "&#10;");
		this.charTranslationMap.put("\"", "&quot;");
		this.charTranslationMap.put("<", "&lt;");
		this.charTranslationMap.put(">", "&gt;");
		this.charTranslationMap.put("&", "&amp;");
		// this.charTranslationMap.put("/", "&#123;");
	}

	private String escape(String toEscape) {
		return translate(toEscape, charTranslationMap);
	}

	private String translate(String toTranslate, Map<String, String> translationMap) {
		String result = toTranslate;
		for (Map.Entry<String, String> entry : translationMap.entrySet()) {
			String key = entry.getKey();
			result = result.replace(key, entry.getValue());
		}
		return result;
	}

	private String validateID(String element, String attribute, String value) {
		String result = null;
		if (this.idValidationMap != null) {
			Vector<String> idAttr = idValidationMap.get(element);
			if (idAttr != null && idAttr.contains(attribute)) {
				// We have a ID/IDREF Attribute, check for XML-Name Syntax
				value = translate(value, idTranslationMap); // Replace non Name-Syntax Characters
				if (Character.isLetter(value.charAt(0)) && !value.startsWith("xml")) { // Check Syntax
					result = value;
				} else {
					result = "_" + value;
				}
			} else {
				result = value;
			}
		} else {
			result = value;
		}
		return result;
	}

	private void generateStartOrEmptyTag(String name, Map<String, String> attrs) throws IOException {
		generateTabs();
		out.write("<" + name);

		if (attrs != null) {
			Iterator<String> iterAttrs = attrs.keySet().iterator();
			while (iterAttrs.hasNext()) {
				String key = iterAttrs.next();
				out.write(" " + key + "=\"" + escape(validateID(name, key, attrs.get(key))) + "\"");
			}
		}
		indentLevel++;
	}

	private void performException(Exception e) {
		throw new SiDiffRuntimeException("Exception while writing XML:", e);
	}

}
