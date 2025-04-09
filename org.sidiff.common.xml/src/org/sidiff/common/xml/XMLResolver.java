package org.sidiff.common.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Class that is used by the XML parsers to resolve XML document type definitions.
 */
public class XMLResolver implements EntityResolver, URIResolver {

	private static final String DTDMAPPING_MAPTYPE = "DTDMappings";
	private static final String BOOTSTRAP_FILENAME = "org.sidiff.common.xml.EntityResolver.xml";

	private static XMLResolver instance;
	private Map<String, String> mappings;
	
	private List<IResourceLoader> loaders = new ArrayList<>();

	private XMLResolver() {
		mappings = new HashMap<>();
		// bootstrap
		mappings.put("http://pi.informatik.uni-siegen.de/SiDiff/org.sidiff.common.io.map.dtd", "org.sidiff.common.io.map.dtd");
	}
	
	/**
	 * Register a loader for resolving file names to input streams.
	 * 
	 * @param loader The loader.
	 */
	public void registerLoader(IResourceLoader loader) {
		this.loaders.add(loader);
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (systemId.startsWith("file")) {
			systemId = systemId.substring(systemId.lastIndexOf('/') + 1);
		}

		String mapping = mappings.get(systemId);
		if (publicId == null && mapping != null) {

			assert LogUtil.log(LogEvent.DEBUG, "Public Id :" + publicId + ", System Id :" + systemId + "\n -> Mapped to " + mapping);

			for (IResourceLoader loader : loaders) {
				InputStream result = loader.loadResourceAsStream(mapping);
				
				if (result != null && result.available() > 0) {
					return new InputSource(result);
				}
			}
			throw new SiDiffRuntimeException("Cannot get " + mapping + " as Stream, please check your classpath");
		}

		assert LogUtil.log(LogEvent.DEBUG, "Public Id :" + publicId + ", System Id :" + systemId + "\n  -> No Mapping");

		return null;
	}

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		try {
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			reader.setEntityResolver(this);
			return new SAXSource(reader, new InputSource(new FileInputStream(href)));
		} catch (Exception e) {
			throw new SiDiffRuntimeException("Cannot resolve " + href, e);
		}
	}

	public static XMLResolver getInstance() {
		if (instance == null) {
			instance = new XMLResolver();
		}
		return instance;
	}
	
	static void initInstance(IResourceLoader loader) {
		try {
			MapLoader.parseMapFromStream(DTDMAPPING_MAPTYPE, XMLResolver.getInstance().mappings,
					loader.loadResourceAsStream(BOOTSTRAP_FILENAME));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Includes new mapping data given in XML
	 * @param mappingData A string containing XML data
	 */
	public void includeMapping(InputStream mappingData) {
		MapLoader.parseMapFromStream(DTDMAPPING_MAPTYPE, this.mappings, mappingData);
	}
}
