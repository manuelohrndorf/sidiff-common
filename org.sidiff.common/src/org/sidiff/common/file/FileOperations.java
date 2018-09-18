package org.sidiff.common.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * <p>Provides utility functions to work with files.</p>
 * <p>It is recommended to use {@link Path} instead of strings when
 * working with absolute file and directory paths.</p>
 * <p><b>Most utility functions are no longer required</b>, since the
 * Java Platform already provides many with {@link Files}.</p>
 * <p>See the table below for some usage examples.</p>
 * <table>
 * <caption>Examples</caption>
 * <tr>
 *   <td>Get a {@link Path} from string</td>
 *   <td><code>Path path = Paths.get("C:/foo/bar.txt");</code></td>
 * </tr>
 * <tr>
 *   <td>Get a Path from file</td>
 *   <td><code>File file = new File("...");<br><code>Path path = file.toPath();</code></td>
 * <tr>
 *   <td>Copy a file</td>
 *   <td><code>Files.copy(sourcePath, targetPath);</code></td>
 * </tr>
 * <tr>
 *   <td>Move a file</td>
 *   <td><code>Files.copy(sourcePath, targetPath, StandardCopyOption.ATOMIC_MOVE);</code></td>
 * </tr>
 * <tr>
 *   <td>Read all lines of a text file</td>
 *   <td><code>for(String line : Files.readAllLines(path)) { ... }</code></td>
 * </tr>
 * <tr>
 *   <td>List all regular files in a directory</td>
 *   <td><code>Files.walk(directoryPath).filter(Files::isRegularFile).forEach(System.out::println);</code></td>
 * </tr>
 * </table>
 * @author Robert Müller
 * @see <a href="https://docs.oracle.com/javase/tutorial/essential/io/pathOps.html">Path Operations - Tutorial</a>
 */
public class FileOperations {

	private FileOperations() {
		throw new AssertionError();
	}

	/**
	 * Deletes all files and folders in the given directory recursively.
	 * Does not follow symbolic links.
	 * @param directoryPath path of the folder which should be deleted
	 * @throws IOException is some I/O exception occurred during the deletion
	 */
	public static void removeFolder(Path directoryPath) throws IOException {
		Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
		   @Override
		   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		       Files.delete(file);
		       return FileVisitResult.CONTINUE;
		   }

		   @Override
		   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		       Files.delete(dir);
		       return FileVisitResult.CONTINUE;
		   }
		});
	}

	/**
	 * Returns whether a directory with the given name exists
	 * anywhere in the given directory. Does not follow symbolic links.
	 * @param directoryPath path of the root directory to search
	 * @param dirName the directory name to search
	 * @return <code>true</code> if the folder exists, <code>false</code> otherwise
	 * @throws IOException if some I/O exception occurred during the search
	 */
	public static boolean existsFolder(Path directoryPath, Path dirName) throws IOException {
		boolean folderExists[] = new boolean [] { false }; 
		Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if(dir.getFileName().equals(dirName)) {
					folderExists[0] = true;
					return FileVisitResult.TERMINATE;
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return folderExists[0];
	}
}
