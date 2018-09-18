package org.sidiff.common.file;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides utility functions for calculating message digests / hashes.
 */
public class DigestUtil {

	private DigestUtil() {
		throw new AssertionError();
	}

	/**
	 * Calculate the MD5 hash value for the given input stream,
	 * which is fully read and then closed.
	 * 
	 * @param inStream the input stream
	 * @return The MD5 hash as byte array.
	 * @throws IOException if reading the input stream failed
	 */
	public static byte[] calculateMD5(InputStream inStream) throws IOException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
		try (DigestInputStream digestInputStream = new DigestInputStream(inStream, digest)) {
		    byte[] buffer = new byte[8192];
		    while(digestInputStream.read(buffer) != -1);
		    return digest.digest();
		}
	}
	
}
