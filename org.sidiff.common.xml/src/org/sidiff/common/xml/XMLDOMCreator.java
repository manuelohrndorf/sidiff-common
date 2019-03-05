package org.sidiff.common.xml;

import java.io.OutputStream;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.w3c.dom.*;

/**
 * Utility class for creating and handling DOM documents of XML files.
 */
public class XMLDOMCreator {

	/**
	 * Creates a DOM document.
	 * @return
	 */
	public static Document createDocument() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			return null;
		}
		return db.newDocument();
	}
	
	/**
	 * Writes the given DOM document into the given OutputStream.
	 * @param outputStream
	 * @param document
	 */
	public static void writeDocument(OutputStream outputStream, Document document) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(outputStream);
			transformer.transform(source, result);
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			throw new SiDiffRuntimeException(e);
		}
	}	
	
	/**
	 * Returns a string representation of the given node (in XML syntax). 
	 * @param node
	 * @return
	 */
	public static String writeToString(Node node) {
		StringBuilder builder = new StringBuilder();
		writeToStringBuilder(builder, node);
		return builder.toString();
	}
	
	/**
	 * Returns a string representation of all child nodes of the given node (in XML syntax).
	 * It is surrounded by a container element if specified in the parameter optionalContainer.
	 * @param node
	 * @param optionalContainer name of the container element (null if no container element has to be created)
	 * @return
	 */
	public static String writeChildNodesToString(Node node, String optionalContainer) {
		StringBuilder builder = new StringBuilder();
		if (optionalContainer!=null && !optionalContainer.trim().equals("")) {
			builder.append("<");
			builder.append(optionalContainer);
			builder.append(">");
		}
		if (node.hasChildNodes()) {
			NodeList childs = node.getChildNodes();
			for (int i=0; i<childs.getLength(); i++)
				writeToStringBuilder(builder, childs.item(i));
		}
		if (optionalContainer!=null && !optionalContainer.trim().equals("")) {
			builder.append("</");
			builder.append(optionalContainer);
			builder.append(">");
		}
		return builder.toString();
	}
	
	private static void writeToStringBuilder(StringBuilder builder, Node node) {
		if (node.getNodeType()==3) {
			builder.append(node.getTextContent());
			return;
		}
		builder.append("<");
		builder.append(node.getNodeName());
		if (node.hasAttributes()) {
			NamedNodeMap map = node.getAttributes();
			for (int i=0; i<map.getLength(); i++) {
				Node attr = map.item(i);
				builder.append(" ");
				builder.append(attr.getLocalName());
				builder.append("=\"");
				builder.append(attr.getNodeValue());
				builder.append("\"");
			}
		}
		if (node.hasChildNodes()) {
			builder.append(">");
			NodeList childs = node.getChildNodes();
			for (int i=0; i<childs.getLength(); i++) {
				writeToStringBuilder(builder, childs.item(i));				
			}
			builder.append("</");
			builder.append(node.getNodeName());
			builder.append(">");
		} else {
			builder.append("/>");
		}
	}
}
