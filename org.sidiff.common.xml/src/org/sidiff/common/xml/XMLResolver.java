package org.sidiff.common.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.io.ResourceUtil;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;
import org.sidiff.common.xml.internal.MapLoader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Class that is used by the XML parsers to resolve XML document type definitions.
 */
public class XMLResolver implements EntityResolver, URIResolver {

	private static final String DTDMAPPING_MAPTYPE = "DTDMappings";
	private final static String BOOTSTRAP_FILENAME = "org.sidiff.common.xml.EntityResolver.xml";

	private static XMLResolver instance = null;
	private Map<String, String> mappings = null;

	private XMLResolver() {
		mappings = new HashMap<String, String>();
		// bootstrap
		mappings.put("http://pi.informatik.uni-siegen.de/SiDiff/org.sidiff.common.io.map.dtd", "org.sidiff.common.io.map.dtd");
	}

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

		if (systemId.startsWith("file")) {
			systemId = systemId.substring(systemId.lastIndexOf("/") + 1);
		}

		String mapping = mappings.get(systemId);

		if (publicId == null && mapping != null) {

			assert(LogUtil.log(LogEvent.DEBUG, "Public Id :" + publicId + ", System Id :" + systemId + "\n -> Mapped to " + mapping));

			InputStream result = ResourceUtil.getInputStreamByResourceName(mapping);
			if (result != null && result.available() > 0) {
				return new InputSource(result);
			} else {
				throw new SiDiffRuntimeException("Cannot get " + mapping + " as Stream, please check your classpath");
			}
		}

		assert(LogUtil.log(LogEvent.DEBUG, "Public Id :" + publicId + ", System Id :" + systemId + "\n  -> No Mapping"));

		return null;
	}

	public Source resolve(String href, String base) throws TransformerException {

		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setEntityResolver(this);
			SAXSource source = new SAXSource(reader, new InputSource(new FileInputStream(href)));
			return source;
		} catch (Exception e) {
			throw new SiDiffRuntimeException("Cannot resolve " + href, e);
		}
	}

	public static XMLResolver getInstance() {
		if (instance == null) {
			instance = new XMLResolver();
			// bootstrap
			MapLoader.parseMapFromStream(DTDMAPPING_MAPTYPE, instance.mappings,
					ResourceUtil.getInputStreamByResourceName(BOOTSTRAP_FILENAME));
			// TODO Mapping Datei f.d. Bootstrap aufraeumen!!! 
		}
		return instance;
	}

	/**
	 * Includes new mapping data given in XML
	 * @param mappingData A string containing XML data
	 */
	public void includeMapping(InputStream mappingData) {
		MapLoader.parseMapFromStream(DTDMAPPING_MAPTYPE, this.mappings, mappingData);
	}
}
