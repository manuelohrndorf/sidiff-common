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
 * @author rmueller
 */
public class PropertiesUtil {

	private PropertiesUtil() {
		throw new AssertionError();
	}

	public static Properties findUniqueProperties(IContainer container) throws CoreException {
		return loadProperties(getUnique(findPropertiesFiles(container), container.toString()));
	}

	public static Properties findUniqueProperties(Path folder) throws CoreException {
		return loadProperties(getUnique(findPropertiesFiles(folder), folder.toString()));
	}

	public static Properties findPreferredProperties(IFile inputFile) throws CoreException {
		IContainer searchContainer = inputFile.getParent();
		List<IFile> propertiesFiles = Collections.emptyList();
		while(propertiesFiles.isEmpty()
				&& (searchContainer instanceof IFolder || searchContainer instanceof IProject)) {
			propertiesFiles = findPropertiesFiles(searchContainer);
			searchContainer = searchContainer.getParent();
		}
		if(!propertiesFiles.isEmpty()) {
			return loadProperties(propertiesFiles.stream().max(getPropertyFileComparator(inputFile)).get());
		}
		return new Properties();
	}

	public static Properties loadProperties(IFile propertiesFile) throws CoreException {
		try (InputStream inStream = propertiesFile.getContents(true)) {
			Properties properties = new Properties();
			properties.load(inStream);
			return properties;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CommonPlugin.ID,
					"Failed to load properties from file: " + propertiesFile, e));
		}
	}

	public static Properties loadProperties(Path propertiesFile) throws CoreException {
		try (BufferedReader reader = Files.newBufferedReader(propertiesFile)) {
			Properties properties = new Properties();
			properties.load(reader);
			return properties;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CommonPlugin.ID,
					"Failed to load properties from file: " + propertiesFile, e));
		}
	}

	public static List<IFile> findPropertiesFiles(IContainer folder) throws CoreException {
		List<IFile> propertiesFiles = new ArrayList<>();
		folder.accept(resource -> {
			if(resource instanceof IFile && "properties".equals(resource.getFileExtension())) {
				propertiesFiles.add((IFile)resource);
			}
			return resource.equals(folder);
		});
		return propertiesFiles;
	}

	public static List<Path> findPropertiesFiles(Path folder) throws CoreException {
		try {
			return Files.list(folder)
					.filter(p -> p.getFileName().toString().endsWith(".properties"))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CommonPlugin.ID,
					"Failed to list properties in folder: " + folder, e));
		}
	}

	private static Comparator<IFile> getPropertyFileComparator(IFile inputFile) {
		return Comparator.comparingInt(propertiesFile -> {
			String propertiesFileName = propertiesFile.getName().substring(0, propertiesFile.getName().lastIndexOf("."));
			String inputFileName = inputFile.getName().substring(0, inputFile.getName().lastIndexOf("."));
			if(propertiesFileName.equals(inputFileName)) {
				return 100;
			}
			if(inputFileName.startsWith(propertiesFileName)) {
				return 50;
			}
			if(propertiesFileName.contains(inputFileName)) {
				return 10;
			}
			return 0;
		});
	}

	private static <T> T getUnique(List<T> propertiesFiles, String containerName) {
		switch(propertiesFiles.size()) {
			case 0: throw new IllegalStateException("No properties files found in " + containerName);
			case 1: return propertiesFiles.get(0);
			default: throw new IllegalStateException("Multiple properties files found in " + containerName);
		}
	}

	public static void storeProperties(Properties properties, Path propertiesFile) throws CoreException {
		try (BufferedWriter writer = Files.newBufferedWriter(propertiesFile)) {
			properties.store(writer, null);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CommonPlugin.ID,
					"Failed to store properties to file: " + propertiesFile, e));
		}
	}

	public static void storeProperties(Properties properties, IFile propertiesFile) throws CoreException {
		try(ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			properties.store(outStream, null);
			IOUtil.writeBytesToFile(outStream.toByteArray(), propertiesFile);
		} catch (IOException | CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, CommonPlugin.ID,
					"Failed to store properties to file: " + propertiesFile, e));
		}
	}
}
