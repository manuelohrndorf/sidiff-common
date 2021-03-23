package org.sidiff.common.io;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.*;
import org.sidiff.common.CommonPlugin;

/**
 * Contains utility functions to work with input and
 * output streams. {@link Files} also contains further
 * utility functions.
 * @author rmueller
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
	 * Creates the given folder and its parents in the workspace.
	 * @param folder the folder to create
	 * @param monitor progress monitor
	 * @throws CoreException if the folder could not be created
	 */
	public static void createFolders(IFolder folder, IProgressMonitor monitor) throws CoreException {
		if(!folder.exists()) {
			SubMonitor progress = SubMonitor.convert(monitor, 2);
			if(folder.getParent() instanceof IFolder) {
				createFolders((IFolder)folder.getParent(), progress.split(1));
			}
			folder.create(true, true, progress.split(1));
		}
	}

	/**
	 * Deletes a folder with all its contents.
	 * @param folder the folder
	 */
	public static void deleteFolder(File folder) {
		Assert.isLegal(folder.isDirectory(), "Not a folder: " + folder);
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File file : files) {
				if (file.isDirectory()) {
					deleteFolder(file);
				} else {
					file.delete();
				}
			}
		}
		folder.delete();
	}

	/**
	 * Writes a string to a file in the workspace.
	 * @param string the string to write
	 * @param file the platform file
	 * @throws CoreException if writing to the file failed for any reason
	 */
	public static void writeStringToFile(String string, IFile file) throws CoreException {
		writeBytesToFile(string.getBytes(), file);
	}

	/**
	 * Writes a byte array to a file in the workspace.
	 * @param byteArray the bytes to write
	 * @param file the platform file
	 * @throws CoreException if writing to the file failed for any reason
	 */
	public static void writeBytesToFile(byte byteArray[], IFile file) throws CoreException {
		try(InputStream byteStream = new ByteArrayInputStream(byteArray)) {
			if(file.exists()) {
				file.setContents(byteStream, true, true, null);
			} else {
				file.create(byteStream, true, null);
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CommonPlugin.ID,
					"Failed to write text to file", e));
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
			return locatePluginFile(pluginId, "/" + path);
		}
		URL url = FileLocator.find(new URL("platform:/plugin/" + pluginId + path));
		if(url == null) {
			throw new FileNotFoundException(path + " could not be found in plugin " + pluginId);
		}
		return url;
	}
}
