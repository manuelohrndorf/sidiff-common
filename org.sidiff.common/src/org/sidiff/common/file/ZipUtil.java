package org.sidiff.common.file;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.io.IOUtil;

/**
 * Contains utility functions to work with Zip archives.
 * This class uses {@link Path}, see {@link FileOperations}.
 * @author Robert Müller
 */
public class ZipUtil {

	private static final String ZIP_SEPERATOR = "/";

	private ZipUtil() {
		throw new AssertionError();
	}

	/**
	 * Zips all files in the directory to the archive file.
	 * If the archive already exists, it is overridden.
	 * Empty folders are not added to the archive.
	 * @param directoryPath the path of the directory to zip
	 * @param archivePath the path of the archive
	 * @throws IOException if an I/O or Zip error occured
	 */
	public static void zip(Path directoryPath, Path archivePath) throws IOException {
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(archivePath))) {
			Pattern backslashPattern = Pattern.compile(Pattern.quote("\\"));
			Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String entryName = backslashPattern.matcher(directoryPath.relativize(file).toString()).replaceAll(ZIP_SEPERATOR);
					zipOutputStream.putNextEntry(new ZipEntry(entryName));
					Files.copy(file, zipOutputStream);
					zipOutputStream.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	/**
	 * Unzips the archive file to the given directory.
	 * Directories are created. Existing files are overridden.
	 * @param archivePath the path of the archive
	 * @param outputDirectory the path of the output directory
	 * @throws IOException if an I/O or Zip error occured
	 */
	public static void unzip(Path archivePath, Path outputDirectory) throws IOException {
		try (ZipFile zip = new ZipFile(archivePath.toFile())) {
			for(ZipEntry entry : CollectionUtil.asIterable(zip.entries())) {
				Path path = outputDirectory.resolve(entry.getName());
				Files.createDirectories(path);
				if(!entry.isDirectory()) {
					Files.copy(zip.getInputStream(entry), path, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	/**
	 * Returns the string contents of a Zip archive's entry.
	 * @param archivePath the path of the archive file
	 * @param entryName the name of the archive entry
	 * @param charset the charset of the file, see {@link StandardCharsets}
	 * @return the string contents of the entry
	 * @throws IOException if an I/O or Zip error occured
	 * @throws IllegalArgumentException if no Zip entry with the given name exists
	 * @throws UnsupportedEncodingException if the charset is not supported
	 */
	public static String readFile(Path archivePath, String entryName, Charset charset) throws IOException {
		try (ZipFile zip = new ZipFile(archivePath.toFile())) {
			ZipEntry entry = zip.getEntry(entryName);
			if(entry == null) {
				throw new IllegalArgumentException("Zip entry not found: " + entryName);
			}
			return IOUtil.toString(zip.getInputStream(entry), charset);
		}
	}

	/**
	 * Returns the names of all entries of the Zip file.
	 * @param archivePath the path of the Zip file
	 * @return all entry names
	 * @throws IOException if an I/O or Zip error occured
	 */
	public static Collection<String> getEntries(Path archivePath) throws IOException {
		try (ZipFile zip = new ZipFile(archivePath.toFile())) {
			return CollectionUtil.asStream(CollectionUtil.asIterable(zip.entries()))
					.map(entry -> entry.getName())
					.collect(Collectors.toList());
		}
	}
}
