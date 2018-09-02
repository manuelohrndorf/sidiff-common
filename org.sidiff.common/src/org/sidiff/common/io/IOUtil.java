package org.sidiff.common.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;

/**
 * Utility class for IO-related stuff 
 */
public class IOUtil {

	/**
	 * Creates an input stream from a file.
	 * @param filename
	 * @return
	 * @deprecated Use <code>new FileInputStream(filename)</code> instead
	 */
	public static InputStream getInputStreamFromFile(String filename) {
		File file = new File(filename);
		InputStream result = null;
		try {
			result = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			LogUtil.log(LogEvent.ERROR, "File not found: "+filename);
		}
		return result;
	}

	/**
	 * Creates an input stream from a string.
	 * @param data
	 * @return
	 * @deprecated Use <code>new ByteArrayInputStream(data.getBytes())</code> instead
	 */
	public static InputStream getInputStreamFromString(String data) {
		return new StringInputStream(data);
	}

	/**
	 * Creates an input stream from a resource.
	 * (The resource has to be in the classpath of one of the class loaders registered at the ResourceUtil.)
	 * @param resourceName
	 * @return
	 */
	public static InputStream getInputStreamFromResource(String resourceName) {
		return ResourceUtil.getInputStreamByResourceName(resourceName);
	}

	/**
	 * Creates an input stream from a file or a resource.
	 * @param resourceOrFileName
	 * @return
	 */
	public static InputStream getInputStream(String resourceOrFileName) {
		InputStream result = null;
		URL url;
		assert(LogUtil.log(LogEvent.DEBUG, "Try to get '" + resourceOrFileName + "' as File ..."));
		try {
			result = new FileInputStream(new File(resourceOrFileName));
		} catch (FileNotFoundException e) {
		}
		if (result == null) {
			assert(LogUtil.log(LogEvent.DEBUG, "Try to get '" + resourceOrFileName + "' as Resource ..."));
			result = getInputStreamFromResource(resourceOrFileName);
		}
		if (result == null){
			assert(LogUtil.log(LogEvent.DEBUG, "Try to get '" + resourceOrFileName + "' as Plugin Resource..."));
			if(resourceOrFileName.startsWith("platform:/plugin")){
				try {
					url = new URL(resourceOrFileName);
					result = url.openConnection().getInputStream();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (result == null) {
			throw new IllegalArgumentException("Unable to get Input Stream '" + resourceOrFileName + "'");
		}
		return result;
	}

	/**
	 * Creates an output stream that writes into the given file.
	 * @param filename
	 * @return
	 * @deprecated Use <code>new FileOutputStream(filename)</code> instead
	 */
	public static OutputStream getOutputStreamIntoFile(String filename) {
		File file = new File(filename);
		OutputStream result = null;
		try {
			result = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Creates an output stream that writes into the given string buffer.
	 * @param buffer
	 * @return
	 * @deprecated Use <code>ByteArrayOutputStream baos = new ByteArrayOutputStream();
	 * baos.toString(charsetName); // e.g. "UTF-8"</code>
	 * 
	 */
	public static OutputStream getOutputStreamIntoString(StringBuffer buffer) {
		return new StringOutputStream(buffer);
	}

	/**
	 * Reads all data from the given stream and returns it as string.
	 * @param stream
	 * @return
	 */
	public static String readFromStream(InputStream inStream) {
		try (ByteArrayOutputStream outStream = new ByteArrayOutputStream();) {
			transfer(inStream, outStream);
			return outStream.toString();
		} catch (IOException e) {
			throw new SiDiffRuntimeException(IOUtil.class, "Error while Reading from stream ", e);
		}
	}
	
	/**
	 * Reads the first [charCount] chars from the given stream and returns them as string.
	 * @param stream
	 * @param charCount
	 * @return
	 */
	public static String readFromStream(InputStream stream, int charCount) {
		return readFromStream(new BoundedInputStream(stream, charCount));
	}

	/**
	 * Reads the whole input stream and writes it to the output stream.
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
	 * An input streams which read a maximum number of bytes.
	 *
	 */
	private static class BoundedInputStream extends InputStream {

		private final InputStream inStream;
		private long remainingBytes;

		public BoundedInputStream(InputStream inStream, long maxBytes) {
			Assert.isLegal(maxBytes > 0, "maxBytes must be greater than 0");
			this.inStream = inStream;
			this.remainingBytes = maxBytes;
		}

		@Override
		public int read() throws IOException {
			if (remainingBytes > 0) {
	            int c = inStream.read();
	            if (c >= 0) {
	            	remainingBytes -= 1;
	            }
	            return c;
	        }
			return -1;
		}

		@Override
		public int read(byte[] b) throws IOException {
			return this.read(b, 0, b.length);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (remainingBytes > 0) {
	            int readBytes = inStream.read(b, off, (int)Math.min(len, remainingBytes));
	            remainingBytes -= readBytes;
	            return readBytes;
	        }
			return -1;
		}

		@Override
		public int available() throws IOException {
			return (int)Math.min(inStream.available(), remainingBytes);
		}

		@Override
		public long skip(long n) throws IOException {
			long skipped = inStream.skip(Math.min(n, remainingBytes));
			remainingBytes -= skipped;
	        return skipped;
		}

		@Override
		public void close() throws IOException {
			super.close();
		}

		@Override
		public synchronized void mark(int readlimit) {
			// does nothing
		}

		@Override
		public boolean markSupported() {
			return false;
		}

		@Override
		public synchronized void reset() throws IOException {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * OutputStream implementation for Strings
	 */
	@Deprecated
	private static class StringOutputStream extends OutputStream {
		
		private StringBuffer buffer = null;
		
		public StringOutputStream(StringBuffer buffer) {
			this.buffer = buffer;
		}
		
		public void write(int character) throws java.io.IOException {
			this.buffer.append((char) character);
		}
	}

	/**
	 * InputStream implementation for Strings
	 */
	@Deprecated
	private static class StringInputStream extends InputStream {
		
		private int position = 0;
		private String data = null;
		
		private StringInputStream(String data) {
			this.data = data;
		}
		
		public int read() throws java.io.IOException {
			if (position < data.length()) {
				return data.charAt(position++);
			} else {
				return -1;
			}
		}
	}
}
