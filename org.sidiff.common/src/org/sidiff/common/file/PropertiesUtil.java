package org.sidiff.common.file;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.sidiff.common.CommonPlugin;
import org.sidiff.common.io.IOUtil;

/**
 * Contains utility methods to work with {@link Properties} files.
 * Most methods have two variants, one to use {@link IResource}s and another to use {@link Path}s.
 * @author rmueller
 */
public class PropertiesUtil {

	private static final String FILE_EXTENSION = "properties";

	private PropertiesUtil() {
		throw new AssertionError();
	}

	/**
	 * Loads properties from a unique properties file in a folder.
	 * Throws runtime exception if not exactly one properties file is found.
	 * @param folder the folder to find a file in
	 * @return Properties loaded from unique properties file in the folder.
	 * @throws CoreException if searching or loading the properties file failed
	 */
	public static Properties findUniqueProperties(IContainer folder) throws CoreException {
		return loadProperties(getUnique(findPropertiesFiles(folder), folder.toString()));
	}

	/**
	 * Loads properties from a unique properties file in a folder.
	 * Throws runtime exception if not exactly one properties file is found.
	 * @param folder the folder to find a file in
	 * @return Properties loaded from unique properties file in the folder.
	 * @throws CoreException if searching or loading the properties file failed
	 */
	public static Properties findUniqueProperties(Path folder) throws CoreException {
		return loadProperties(getUnique(findPropertiesFiles(folder), folder.toString()));
	}

	private static <T> T getUnique(List<T> propertiesFiles, String containerName) {
		switch(propertiesFiles.size()) {
			case 0: throw new IllegalStateException("No properties files found in folder '" + containerName + "'");
			case 1: return propertiesFiles.get(0);
			default: throw new IllegalStateException("Multiple properties files found in folder '" + containerName + "'");
		}
	}


	/**
	 * Return a list of all properties files directly contained in a given folder.
	 * @param folder the folder to enumerate
	 * @return list of all properties files directly in the folder
	 * @throws CoreException if listing the files failed
	 */
	public static List<IFile> findPropertiesFiles(IContainer folder) throws CoreException {
		List<IFile> propertiesFiles = new ArrayList<>();
		folder.accept(resource -> {
			if(resource instanceof IFile && FILE_EXTENSION.equals(resource.getFileExtension())) {
				propertiesFiles.add((IFile)resource);
			}
			return resource.equals(folder); // only search this folder
		});
		return propertiesFiles;
	}

	/**
	 * Return a list of all properties files directly contained in a given folder.
	 * @param folder the folder to enumerate
	 * @return list of all properties files directly in the folder
	 * @throws CoreException if listing the files failed
	 */
	public static List<Path> findPropertiesFiles(Path folder) throws CoreException {
		try {
			return Files.list(folder)
					.filter(path -> path.getFileName() != null && path.getFileName().toString().endsWith("." + FILE_EXTENSION))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CommonPlugin.ID,
					"Failed to list properties in folder '" + folder + "'", e));
		}
	}

	/**
	 * Finds and loads a "preferred" properties file given a specific input file.
	 * The folder containing the input as well as all parent folders are searched.
	 * The following rules apply to find the preferred properties:
	 * <ul>
	 * <li>Direct containers are preferred over their parents. If a folder contains multiple files:</li>
	 * <li>1) Choose properties file with the same name as the input file (excluding extension).</li>
	 * <li>2) Choose properties file if the name of the input file starts with the name of the properties file.</li>
	 * <li>3) Choose properties file with name containing the name of the input file.</li>
	 * <li>4) Fallback to choosing the first properties file found.</li>
	 * </ul>
	 * @param inputFile the input file
	 * @return preferred properties to use with the input
	 * @throws CoreException if loading the properties or listing the files failed
	 */
	public static Properties findPreferredProperties(IFile inputFile) throws CoreException {
		IContainer searchContainer = inputFile.getParent();
		List<IFile> propertiesFiles = Collections.emptyList();
		while (propertiesFiles.isEmpty() && (searchContainer instanceof IFolder || searchContainer instanceof IProject)) {
			propertiesFiles = findPropertiesFiles(searchContainer);
			searchContainer = searchContainer.getParent();
		}
		if (propertiesFiles.isEmpty()) {
			return new Properties();
		}
		return loadProperties(propertiesFiles.stream().max(getPropertyFileComparator(inputFile)).get());
	}

	private static Comparator<IFile> getPropertyFileComparator(IFile inputFile) {
		return Comparator.comparingInt(propertiesFile -> {
			String propertiesFileName = FileOperations.trimLastFileExtension(propertiesFile.getName());
			String inputFileName = FileOperations.trimLastFileExtension(inputFile.getName());
			if (propertiesFileName.equals(inputFileName)) {
				return 100;
			} else if (inputFileName.startsWith(propertiesFileName)) {
				return 50;
			} else if (propertiesFileName.contains(inputFileName)) {
				return 10;
			}
			return 0;
		});
	}


	/**
	 * Loads {@link Properties} from a file in the workspace.
	 * @param propertiesFile the file
	 * @return properties loaded from file
	 * @throws CoreException if loading the properties failed
	 */
	public static Properties loadProperties(IFile propertiesFile) throws CoreException {
		try (InputStream inStream = propertiesFile.getContents(true)) {
			Properties properties = new Properties();
			properties.load(inStream);
			return properties;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CommonPlugin.ID,
					"Failed to load properties from file '" + propertiesFile + "'", e));
		}
	}

	/**
	 * Loads {@link Properties} from a file in the file system.
	 * @param propertiesFile the file
	 * @return properties loaded from file
	 * @throws CoreException if loading the properties failed
	 */
	public static Properties loadProperties(Path propertiesFile) throws CoreException {
		try (BufferedReader reader = Files.newBufferedReader(propertiesFile)) {
			Properties properties = new Properties();
			properties.load(reader);
			return properties;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CommonPlugin.ID,
					"Failed to load properties from file '" + propertiesFile + "'", e));
		}
	}


	/**
	 * Stores {@link Properties} to a file in the workspace.
	 * @param properties the properties to store
	 * @param propertiesFile the file to write to
	 * @throws CoreException if storing the properties failed
	 */
	public static void storeProperties(Properties properties, IFile propertiesFile) throws CoreException {
		try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			properties.store(outStream, null);
			IOUtil.writeBytesToFile(outStream.toByteArray(), propertiesFile);
		} catch (IOException | CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, CommonPlugin.ID,
					"Failed to store properties in file '" + propertiesFile + "'", e));
		}
	}

	/**
	 * Stores {@link Properties} to a file in the file system.
	 * @param properties the properties to store
	 * @param propertiesFile the file to write to
	 * @throws CoreException if storing the properties failed
	 */
	public static void storeProperties(Properties properties, Path propertiesFile) throws CoreException {
		try (BufferedWriter writer = Files.newBufferedWriter(propertiesFile)) {
			properties.store(writer, null);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CommonPlugin.ID,
					"Failed to store properties in file '" + propertiesFile + "'", e));
		}
	}
}
