package org.sidiff.common.io;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.Assert;

/**
 * An input stream which read a maximum number of bytes,
 * after which it returns EOF.
 * @author Robert Müller
 */
public class BoundedInputStream extends InputStream {

	private final InputStream inStream;
	private long remainingBytes;

	/**
	 * Creates a new BoundedInputStream, reading a the specified maximum
	 * number of bytes from the input stream.
	 * @param inStream the input stream
	 * @param maxBytes maximum number of bytes to read, must be greater than zero
	 */
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