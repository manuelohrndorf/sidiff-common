package org.sidiff.common.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.xerces.parsers.DOMParser;
import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * Utility class for parsing XML documents.
 */
public class XMLParser {

	public static final String DEFAULT_PARSER_NAME_SAX = "org.apache.xerces.parsers.SAXParser";
	
	private static DOMParser domParser = createDOMParser();
	private static XMLReader saxParser = createSAXParser();

	private XMLParser() {
	}

	private static ErrorHandler errorHandler = new XMLErrorHander();

	/**
	 * Parses an XML Document with given ContentHandler by SAX
	 * 
	 * @param istream
	 * @param contentHandler
	 */
	public static void parseStream(InputStream istream, ContentHandler contentHandler) {
		parseXML(new InputSource(istream), contentHandler);
	}

	/**
	 * Parses an XML Document with given ContentHandler by SAX
	 * 
	 * @param istream
	 * @param contentHandler
	 */
	public static Document parseStream(InputStream istream) {
		return parseXML(new InputSource(istream));
	}

	/**
	 * Parses an XML Document with DOM
	 * 
	 * @param istream
	 * @return Document Object
	 */
	public static Document parseXML(InputSource xmlinput) {

		Document result = null;

		synchronized (domParser)
		{
			try {
				domParser.parse(xmlinput);
			} catch (IOException e) {
				throw new SiDiffRuntimeException("IO Error while parsing DOM", e);
			} catch (SAXException e) {
				throw new SiDiffRuntimeException("SAX Error while parsing DOM", e);
			} finally {
				result = domParser.getDocument();
				domParser.reset();
			}
		}

		return result;

	}

	/**
	 * Performs the parsing of an XML file with a given ContentHandler.
	 * @param xmlinput
	 * @param contentHandler
	 */
	public static void parseXML(InputSource xmlinput, ContentHandler contentHandler) {
		
		synchronized (saxParser) {

			saxParser.setContentHandler(contentHandler);
			try {
				saxParser.parse(xmlinput);
			} catch (IOException e) {
				throw new SiDiffRuntimeException("IO Error while parsing " + xmlinput + " with " + contentHandler, e);
			} catch (SAXException e) {
				throw new SiDiffRuntimeException("SAX Error while parsing " + xmlinput + " with " + contentHandler, e);
			} finally {
				saxParser.setContentHandler(null);
			}

		}

	}

	/**
	 * Switches a parser feature on or off.
	 * @param feature
	 * @param value
	 */
	public static void setFeature(ParserFeature feature, boolean value) {
		feature.featureValue = value;
	}

	/**
	 * Creates and returns an instance of the SAX parser.
	 * @return
	 */
	public static XMLReader createSAXParser() {
		XMLReader parser = null;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		} catch (Exception e) {
			throw new SiDiffRuntimeException("Error while Initializing XML Reader", e);
		}

		parser.setEntityResolver(XMLResolver.getInstance());
		parser.setErrorHandler(errorHandler);
		for (ParserFeature feature : ParserFeature.values()) {
			try {
				parser.setFeature(feature.featureID, feature.featureValue);
			} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
				e.printStackTrace();
			}
		}
		return parser;

	}

	/**
	 * Creates and returns an instance of the DOM parser.
	 * @return
	 */
	public static DOMParser createDOMParser() {

		DOMParser parser = new DOMParser();

		parser.setEntityResolver(XMLResolver.getInstance());
		parser.setErrorHandler(errorHandler);
		for (ParserFeature feature : ParserFeature.values()) {
			try {
				parser.setFeature(feature.featureID, feature.featureValue);
			} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
				e.printStackTrace();
			}
		}
		return parser;

	}

	private enum ParserFeature {
		/** Namespaces feature id (http://xml.org/sax/features/namespaces). */
		NAMESPACES_FEATURE("http://xml.org/sax/features/namespaces", true),
		/** Validation feature id (http://xml.org/sax/features/validation). */
		VALIDATION_FEATURE("http://xml.org/sax/features/validation", false),
		/** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
		SCHEMA_VALIDATION_FEATURE("http://apache.org/xml/features/validation/schema", false),
		/** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
		SCHEMA_FULL_CHECKING_FEATURE("http://apache.org/xml/features/validation/schema-full-checking", false),
		/** Honour all schema locations feature id (http://apache.org/xml/features/honour-all-schemaLocations). */
		HONOUR_ALL_SCHEMA_LOCATIONS("http://apache.org/xml/features/honour-all-schemaLocations", false),
		/** Validate schema annotations feature id (http://apache.org/xml/features/validate-annotations). */
		VALIDATE_ANNOTATIONS("http://apache.org/xml/features/validate-annotations", false),
		/** Dynamic validation feature id (http://apache.org/xml/features/validation/dynamic). */
		DYNAMIC_VALIDATION_FEATURE("http://apache.org/xml/features/validation/dynamic", false),
		/** XInclude feature id (http://apache.org/xml/features/xinclude). */
		XINCLUDE_FEATURE("http://apache.org/xml/features/xinclude", false),
		/** XInclude fixup base URIs feature id (http://apache.org/xml/features/xinclude/fixup-base-uris). */
		XINCLUDE_FIXUP_BASE_URIS_FEATURE("http://apache.org/xml/features/xinclude/fixup-base-uris", true),
		/** XInclude fixup language feature id (http://apache.org/xml/features/xinclude/fixup-language). */
		XINCLUDE_FIXUP_LANGUAGE_FEATURE("http://apache.org/xml/features/xinclude/fixup-language", true);

		private String featureID;
		private boolean featureValue;

		private ParserFeature(String featureID, boolean featureDefault) {
			this.featureID = featureID;
			this.featureValue = featureDefault;
		}
	}

	private static class XMLErrorHander implements ErrorHandler {

		@Override
		public void error(SAXParseException exception) throws SAXException {
			throw new SiDiffRuntimeException("Parser Error," + exception.getMessage(), exception);
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			throw new SiDiffRuntimeException("Fatal Parser Error," + exception.getMessage(), exception);
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			throw new SiDiffRuntimeException("Parser Warning," + exception.getMessage(), exception);
		}

	}
	
	// XPathFactory and XPath for queries
	private static XPathFactory factory = XPathFactory.newInstance();
	private static XPath xpath = factory.newXPath();

	/**
	 * executes XPath queries on a given document represented as DOM
	 * @param context The context of the expression, an XML document or node for example.
	 * @param expressionString the XPath query
	 * @return
	 */
	public static NodeList processXPath(Object context, String expressionString) {
		try {
			XPathExpression expression = xpath.compile(expressionString);
			return (NodeList) expression.evaluate(context, XPathConstants.NODESET);
		} catch (Exception e) {
			return null;
		}
	}


}
