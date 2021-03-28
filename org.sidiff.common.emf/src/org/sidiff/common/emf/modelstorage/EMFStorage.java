package org.sidiff.common.emf.modelstorage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * Contains utility functions to convert between
 * <ul>
 * <li>platform/file {@link URI}</li>
 * <li>{@link IResource} ({@link IFile}, {@link IFolder})</li>
 * <li>{@link File} / {@link Path}</li>
 * </ul>
 */
public class EMFStorage {

	private EMFStorage() {
		throw new AssertionError();
	}

	/**
	 * Returns the normalized URI of the given {@link Resource}.
	 * If the resource is contained in a {@link ResourceSet} the
	 * set's {@link URIConverter} is used to normalize the resource's URI.
	 * Otherwise, the resource's URI is returned without normalization.
	 * @param resource the resource
	 * @return normalized URI of this resource
	 */
	public static URI getNormalizedURI(Resource resource) {
		if(resource.getResourceSet() != null) {
			return resource.getResourceSet().getURIConverter().normalize(resource.getURI());
		}
		return resource.getURI();
	}

	/**
	 * Returns a platform URI for the path of the {@link IResource} in the workspace.
	 * @param resource the resource in the workspace
	 * @return platform URI of the resource
	 */
	public static URI toPlatformURI(IResource resource) {
		Objects.requireNonNull(resource, "resource must not be null");
		return toPlatformURI(resource.getFullPath());
	}

	/**
	 * Returns a platform URI for the given {@link IPath}, which must be
	 * of the form <code>/project/folder/file.txt</code>.
	 * @param path the path in the workspace
	 * @return platform URI with the path
	 */
	public static URI toPlatformURI(IPath path) {
		Objects.requireNonNull(path, "path must not be null");
		return URI.createPlatformResourceURI(path.toString(), true);
	}

	/**
	 * <p>Tries to convert the {@link File} in the local file system to a platform URI.</p>
	 * <p>This is a convenience method equivalent to <code>toPlatformURI(toFileURI(file))</code>.</p>
	 * @param file the file in the local file system
	 * @return platform URI of the file
	 */
	public static URI toPlatformURI(File file) {
		return toPlatformURI(toFileURI(file));
	}

	/**
	 * <p>Tries to convert the {@link Path} in the local file system to a platform URI.</p>
	 * <p>This is a convenience method equivalent to <code>toPlatformURI(toFileURI(path))</code>.</p>
	 * @param path a path specifying a file in the local file system
	 * @return platform URI of the file
	 */
	public static URI toPlatformURI(Path path) {
		return toPlatformURI(toFileURI(path));
	}

	/**
	 * Tries to convert an URI to platform URI. If the URI already is a platform
	 * URI or cannot be converted, it is returned as is. URIs are deresolved
	 * against the workspace root to form platform resource URIs.
	 * Note that this method <u>does not support platform <i>plugin</i> URIs</u>.
	 * @param uri the URI
	 * @return the URI (as platform URI, if possible)
	 */
	public static URI toPlatformURI(URI uri) {
		Objects.requireNonNull(uri, "uri must not be null");
		if(uri.isPlatform()) {
			return uri;
		}
		if(uri.isArchive()) {
			URI authority = toPlatformURI(URI.createURI(uri.authority().substring(0, uri.authority().length()-1)));
			return URI.createHierarchicalURI("archive", authority.toString() + "!",
					null, uri.segments(), null, uri.fragment());
		}
		if(uri.isFile()) {
			// append an empty segment so that the base is a prefix URI
			URI workspaceBase = toFileURI(getWorkspaceRoot().getLocation()).appendSegment("");
			URI replaced = uri.replacePrefix(workspaceBase, URI.createURI("/"));
			if(replaced != null) {
				// The replacement is missing the platform:/resource prefix; we must however not encode it twice
				return URI.createPlatformResourceURI(replaced.toString(), false);
			}

			try {
				IFile files[] = getWorkspaceRoot().findFilesForLocationURI(new java.net.URI(uri.toString()));
				if(files.length >= 1) {
					return toPlatformURI(files[0]).appendFragment(uri.fragment());
				}
			} catch (URISyntaxException | IllegalArgumentException e) {
				// fall through
			}

			for(int i = 0; i < uri.segmentCount(); i++) {
				IProject project = getWorkspaceRoot().getProject(uri.segment(i));
				if(project.exists()) {
					String segments = IntStream.range(i+1, uri.segmentCount()).mapToObj(uri::segment).collect(Collectors.joining("/"));
					return URI.createPlatformResourceURI("/" + project.getName() + "/" + segments, false).appendFragment(uri.fragment());
				}
			}
		}
		return uri;
	}

	/**
	 * <p>Tries to convert the {@link IResource} in the workspace to a file URI.</p>
	 * <p>If the resource has a location in the local file system, it is used
	 * for the file URI, else the URI is converted to a platform URI first and
	 * then resolved to a file URI.</p>
	 * @param resource the resource in the workspace
	 * @return file URI of the resource
	 */
	public static URI toFileURI(IResource resource) {
		Objects.requireNonNull(resource, "resource must not be null");
		if(resource.getLocation() != null) {
			return toFileURI(resource.getLocation());
		}
		return toFileURI(toPlatformURI(resource));
	}

	/**
	 * Returns a file URI for the absolute path of the given {@link IPath}.
	 * @param path the path in the local file system
	 * @return file URI for absolute path
	 */
	public static URI toFileURI(IPath path) {
		Objects.requireNonNull(path, "path must not be null");
		return URI.createFileURI(path.makeAbsolute().toString());
	}

	/**
	 * Returns a file URI for the absolute path of the given {@link File}.
	 * @param file the file
	 * @return file URI for absolute path of file
	 */
	public static URI toFileURI(File file) {
		Objects.requireNonNull(file, "file must not be null");
		return URI.createFileURI(file.getAbsolutePath());
	}

	/**
	 * Returns a file URI for the absolute path of the given {@link Path}.
	 * @param path the path
	 * @return file URI for absolute path specified by path
	 */
	public static URI toFileURI(Path path) {
		Objects.requireNonNull(path, "path must not be null");
		return URI.createFileURI(path.toAbsolutePath().toString());
	}

	/**
	 * <p>Tries to convert an URI to a file URI. If the URI already is a file URI
	 * or cannot be converted, it is returned as is.</p>
	 * <p>Platform resource URIs are resolved to file URIs using the
	 * {@link EcorePlugin#getPlatformResourceMap() Ecore platform resource map}
	 * if possible, or else by querying the workspace root.</p>
	 * <p>Platform plugin URIs are resolved to file URIs using the location
	 * of the corresponding plugin bundle.</p>
	 * @param uri the URI
	 * @return the URI (as file URI, if possible)
	 */
	public static URI toFileURI(URI uri) {
		Objects.requireNonNull(uri, "uri must not be null");
		if(uri.isFile()) {
			return uri;
		} else if(uri.isPlatformResource() && uri.segmentCount() > 1) {
			// first, try the ecore platform resource map
			URI resolved = EcorePlugin.resolvePlatformResourcePath(uri.toPlatformString(true));
			if(resolved != null) {
				return resolved;
			}
			// segment 0 is always "resource"
			IProject project = getWorkspaceRoot().getProject(uri.segment(1));
			URI result;
			if(project.exists()) {
				// if the project exists, it might have a mapped location
				result = toFileURI(project.getLocation());
			} else {
				// if the project doesn't exist, we still want to return a file URI, but project.getLocation() fails
				result = toFileURI(getWorkspaceRoot().getLocation()).appendSegment(project.getName());
			}
			for(int i = 2; i < uri.segmentCount(); i++) {
				result = result.appendSegment(uri.segment(i));
			}
			return result.appendFragment(uri.fragment());
		} else if(uri.isPlatformPlugin() && uri.segmentCount() > 1) {
			try {
				URL url = FileLocator.toFileURL(new URL(uri.toString()));
				return URI.createFileURI(url.toURI().getSchemeSpecificPart()).appendFragment(uri.fragment());
			} catch (IOException | URISyntaxException e) {
				throw new IllegalArgumentException("Unable to resolve platform plugin URI: " + uri, e);
			}
		}
		return uri;
	}

	/**
	 * Tries to convert the URI to a file URI and returns a {@link Path} in the local file system.
	 * If the URI cannot be converted to a file URI, <code>null</code> is returned.
	 * @param uri the URI
	 * @return path of file in the local file system for the given URI, <code>null</code> if not convertible to a file URI
	 */
	public static Path toPath(URI uri) {
		URI fileURI = toFileURI(uri);
		if(fileURI.isFile()) {
			return Paths.get(fileURI.toFileString());
		}
		return null;
	}

	/**
	 * <p>Tries to convert the {@link IResource} in the workspace to a {@link Path} in the local file system.</p>
	 * @param resource the workspace resource
	 * @return path of in the local file system, <code>null</code> if not convertible
	 */
	public static Path toPath(IResource resource) {
		return toPath(toFileURI(resource));
	}

	/**
	 * Tries to convert the URI to a file URI and returns the corresponding {@link File} in the local file system.
	 * The returned File is a resource handle and may or may not actually exist.
	 * If the URI cannot be converted to a file URI, <code>null</code> is returned.
	 * @param uri the URI
	 * @return file in the local file system for the given URI, <code>null</code> if not convertible to a file URI
	 */
	public static File toFile(URI uri) {
		URI fileURI = toFileURI(uri);
		if(fileURI.isFile()) {
			return new File(fileURI.toFileString());
		}
		return null;
	}

	/**
	 * <p>Tries to convert the {@link IResource} in the workspace to a {@link File} in the local file system.</p>
	 * <p>This is a convenience method equivalent to <code>toFile(toFileURI(file))</code>.</p>
	 * @param resource the resource in the workspace
	 * @return corresponding file in the local file system, <code>null</code> if none
	 */
	public static File toFile(IResource resource) {
		return toFile(toFileURI(resource));
	}

	/**
	 * Tries to convert the URI to a platform URI and returns the corresponding {@link IFile} in the workspace.
	 * For archive URIs that have a platform authority, the IFile is the archive file.
	 * The returned IFile is a resource handle and may or may not actually exist.
	 * If the URI cannot be converted to a platform URI, <code>null</code> is returned.
	 * @param uri the URI
	 * @return file in the workspace for the given URI, <code>null</code> if not in workspace
	 */
	public static IFile toIFile(URI uri) {
		URI platformURI = toPlatformURI(uri);
		if(platformURI.isPlatform()) {
			return getWorkspaceRoot().getFile(new org.eclipse.core.runtime.Path(platformURI.toPlatformString(true)));
		}
		if(platformURI.isArchive() && platformURI.authority().startsWith("platform:")) {
			return toIFile(URI.createURI(platformURI.authority().substring(0, platformURI.authority().length()-1)));
		}
		return null;
	}

	/**
	 * <p>Tries to convert the {@link Path} in the local file system to a {@link IFile} in the workspace</p>
	 * <p>This is a convenience method equivalent to <code>toIFile(toFileURI(path))</code>.</p>
	 * @param path a path of a file in the local file system
	 * @return corresponding file in the workspace, <code>null</code> if none
	 */
	public static IFile toIFile(IPath path) {
		return toIFile(toFileURI(path));
	}

	/**
	 * <p>Tries to convert the {@link File} in the local file system to a {@link IFile} in the workspace</p>
	 * <p>This is a convenience method equivalent to <code>toIFile(toFileURI(file))</code>.</p>
	 * @param file the file in the local file system
	 * @return corresponding file in the workspace, <code>null</code> if none
	 */
	public static IFile toIFile(File file) {
		return toIFile(toFileURI(file));
	}

	/**
	 * <p>Tries to convert the {@link Path} in the local file system to a {@link IFile} in the workspace</p>
	 * <p>This is a convenience method equivalent to <code>toIFile(toFileURI(path))</code>.</p>
	 * @param path a path of a file in the local file system
	 * @return corresponding file in the workspace, <code>null</code> if none
	 */
	public static IFile toIFile(Path path) {
		return toIFile(toFileURI(path));
	}

	/**
	 * Tries to convert the URI to a platform URI and returns the corresponding {@link IFolder} in the workspace.
	 * The returned IFolder is a resource handle and may or may not actually exist.
	 * If the URI cannot be converted to a platform URI, <code>null</code> is returned.
	 * @param uri the URI
	 * @return folder in the workspace for the given URI, <code>null</code> if not in workspace
	 */
	public static IFolder toIFolder(URI uri) {
		URI platformURI = toPlatformURI(uri);
		if(platformURI.isPlatform()) {
			return getWorkspaceRoot().getFolder(new org.eclipse.core.runtime.Path(platformURI.toPlatformString(true)));
		}
		return null;
	}

	/**
	 * <p>Tries to convert the {@link File} in the local file system to a {@link IFolder} in the workspace</p>
	 * <p>This is a convenience method equivalent to <code>toIFolder(toFileURI(file))</code>.</p>
	 * @param file the file in the local file system
	 * @return corresponding folder in the workspace, <code>null</code> if none
	 */
	public static IFolder toIFolder(File file) {
		return toIFolder(toFileURI(file));
	}

	/**
	 * <p>Tries to convert the {@link Path} in the local file system to a {@link IFolder} in the workspace</p>
	 * <p>This is a convenience method equivalent to <code>toIFolder(toFileURI(path))</code>.</p>
	 * @param path a path of a file in the local file system
	 * @return corresponding folder in the workspace, <code>null</code> if none
	 */
	public static IFolder toIFolder(Path path) {
		return toIFolder(toFileURI(path));
	}

	protected static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
