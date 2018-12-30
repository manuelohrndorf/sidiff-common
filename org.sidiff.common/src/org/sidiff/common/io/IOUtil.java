package org.sidiff.common.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.eclipse.core.runtime.FileLocator;

/**
 * Contains utility functions to work with input and
 * output streams. {@link Files} also contains further
 * utility functions.
 * @author Robert Müller
 */
public class IOUtil {

	private IOUtil() {
		throw new AssertionError();
	}

	/**
	 * Fully reads an input stream and converts the read data
	 * to a string using the given charset. The input stream is closed.
	 * @param inStream the input stream
	 * @param charset the charset, see {@link StandardCharsets}
	 * @return string contents of the input stream
	 * @throws IOException
	 */
	public static String toString(InputStream inStream, Charset charset) throws IOException {
		try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			transfer(inStream, outStream);
			return outStream.toString(charset.name());
		} finally {
			inStream.close();
		}
	}

	/**
	 * Reads the whole input stream and writes it to the output stream.
	 * The caller must close the streams.
	 * @param in the input stream
	 * @param out the output stream
	 * @throws IOException if an I/O error occurred
	 */
	public static void transfer(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[10*1024];
		int length;
        while((length = in.read(buffer)) > 0) {
        	out.write(buffer, 0, length);
        }
	}

	/**
	 * Opens a new input stream for a file inside a plugin bundle.
	 * The caller must close this input stream when it is no longer used.
	 * @param pluginId the plugin bundle ID
	 * @param path the path inside the plugin, with / as separators, leading / is option
	 * @return new input stream
	 * @throws IOException if an I/O error occurred, if the path is invalid, or if the file doesn't exist
	 */
	public static InputStream openInputStream(String pluginId, String path) throws IOException {
		return locatePluginFile(pluginId, path).openStream();
	}

	/**
	 * Returns the absolute path of a file inside a plugin bundle.
	 * @param pluginId the plugin bundle ID
	 * @param path the path inside the plugin, with / as separators, leading / is option
	 * @return path absolute {@link Path} of the file inside the plugin bundle
	 * @throws IOException if an I/O error occurred, if the path is invalid, or if the file doesn't exist
	 */
	public static Path getAbsolutePath(String pluginId, String path) throws IOException {
		return new File(FileLocator.toFileURL(locatePluginFile(pluginId, path)).getFile()).toPath();
	}

	private static URL locatePluginFile(String pluginId, String path) throws FileNotFoundException, MalformedURLException {
		if(!path.startsWith("/")) {
			path = "/" + path;
		}
		URL url = FileLocator.find(new URL("platform:/plugin/" + pluginId + path));
		if(url == null) {
			throw new FileNotFoundException(path + " could not be found in plugin " + pluginId);
		}
		return url;
	}
}
