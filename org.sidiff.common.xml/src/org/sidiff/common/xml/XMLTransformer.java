package org.sidiff.common.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;
import org.xml.sax.InputSource;

/**
 * Utility class to transform XML documents.
 */
public class XMLTransformer {

	/**
	 * Transforms the given XML input stream with the given XSLT and returns the transformation result as a new input stream.
	 * @param xmlDataStream
	 * @param xsltData
	 * @return
	 */
	public static InputStream transform(InputStream xmlDataStream, InputStream xsltData) {
		StringWriter result = new StringWriter();
		transform(new InputSource(xmlDataStream), new StreamResult(result), new InputSource(xsltData));
		return new ByteArrayInputStream(result.toString().getBytes());
	}
	
	/**
	 * Transforms the given XML input stream with the given XSLT and returns the transformation result as a new input stream.
	 * The transformation result is also written to the file system.
	 * @param xmlDataStream
	 * @param xsltData
	 * @return
	 */
	public static InputStream transformUsingTempfile(InputStream xmlDataStream, InputStream xsltData) {
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile(xmlDataStream.hashCode()+"", ".xml");
		} catch (IOException e) {
			return null;
		}

		StringWriter result = new StringWriter();
		transform(new InputSource(xmlDataStream), new StreamResult(result), new InputSource(xsltData));
		try (Writer mout = new OutputStreamWriter(new FileOutputStream(tmpFile.getAbsolutePath()))) {
			mout.write(result.toString());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		try {
			return new FileInputStream(tmpFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static void transform(InputSource source, Result result, InputSource transformScript) {

		TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = tFactory.newTransformer(new SAXSource(transformScript));
			// Benoetigt um Referenzen im Transformationsskript aufzuloesen (auch in dort geladenen Dokumenten)
			transformer.setURIResolver(XMLResolver.getInstance());
			LogUtil.log(LogEvent.NOTICE, "Using Transformer " + transformer.getClass().getName());

			SAXSource saxSource = new SAXSource(XMLParser.createSAXParser(), source);
			transformer.transform(saxSource, result);

		} catch (Exception e) {
			throw new SiDiffRuntimeException(XMLTransformer.class, "Error while transforming document!", e);
		}
	}
}
