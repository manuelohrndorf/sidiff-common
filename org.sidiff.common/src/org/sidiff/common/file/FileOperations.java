package org.sidiff.common.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.sidiff.common.exceptions.FileAlreadyExistsException;
import org.sidiff.common.exceptions.FileNotCreatedException;

public class FileOperations {
	
	/**
	 * Creates a new directory
	 * 
	 * @param path absolute path of the directory that will be created
	 * @param overwrite if the directory already exists and the flag is false, an FileAlreadyExistsException will be thrown
	 * @throws FileNotCreatedException
	 * @throws FileAlreadyExistsException
	 */
	public static void createFolder(String path, boolean overwrite) throws FileNotCreatedException, FileAlreadyExistsException{
		File dir = new File(path);
		if(dir.exists() && !overwrite){
			throw new FileAlreadyExistsException("folder already exists");
		}else if(!dir.exists()){
			if(!dir.mkdir()) throw new FileNotCreatedException("could not create folder!");
		}else if(overwrite){
			removeFolder(path);
			if(!dir.mkdir()) throw new FileNotCreatedException("could not create folder!");
		}
	}
	
	/**
	 * 
	 * @param path absolute path of the folder which should be deleted
	 */
	public static void removeFolder(String path){
		File dir = new File(path);
		for(File file : dir.listFiles()){
			if(file.isDirectory())
				removeFolder(file.getPath());
			file.delete();
		}
		dir.delete();
	}
	
	
	/**
	 * 
	 * @param path absolute path of the root directory
	 * @param directory to be searched
	 * @return 
	 */
	public static boolean existsFolder(String path, String dirName){

		File dir = new File(path);
		if(dir.getName().equals(dirName)) return true;
		for(File file : dir.listFiles()){
			if(file.isDirectory() && !file.getName().startsWith("."))
				if(existsFolder(file.getPath(), dirName)) return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param dir absolute path of a directory
	 * @return List of all files contained in the directory
	 */
	public static List<File> getFilesFromDir(String dir){
		ArrayList<File> result = new ArrayList<File>();
		File file = new File(dir);
		for(File f : file.listFiles()){
			if(f.isFile())
				result.add(f);
			else
				result.addAll(getFilesFromDir(f.getPath()));
		}
		return result;
	}
	
	/**
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copyFile(String in, String out) throws IOException{
		FileInputStream inFile = new FileInputStream(new File(in));
		FileOutputStream outFile = new FileOutputStream(new File(out));
		
		FileChannel inChannel = inFile.getChannel();
		FileChannel outChannel = outFile.getChannel();
		
		try{
			inChannel.transferTo(0, inChannel.size(), outChannel);
		}catch(IOException e){
			throw e;
		}finally{
			if (inChannel != null)
				inChannel.close();
			if (inFile != null)
				inFile.close();
			if (outFile != null)
				outFile.close();
			if (outChannel != null)
				outChannel.close();
		}
	}
	
	/**
	 * Calculate the MD5 hash value of the given file.
	 * 
	 * @param file
	 *            The file path.
	 * @return The MD5 hash as byte array.
	 * @throws IOException
	 */
	public static byte[] readMD5FileHash(String file) throws IOException {
		DigestInputStream digestInputStream = null;
		
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
		    FileInputStream fileInputStream = new FileInputStream(file);
		    digestInputStream = new DigestInputStream(fileInputStream, digest);
			
		    byte[] buffer = new byte[8192];
		    while(digestInputStream.read(buffer) != -1);
		    
		    return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			if (digestInputStream != null) {
				digestInputStream.close();	
			}
		}
		
		return null;
	}
	
	
	//TODO remove...
	public static void createInfoFile(String path, String info){
		if (!(path.endsWith("/") || path.endsWith("\\"))) {
			path = path + System.getProperty("file.separator");
		}
		
		try {
			FileWriter file = new FileWriter (path+"patch.info");
			file.write(info);
			file.close();
	      }
	      catch (IOException e) {
	        System.out.println("Fehler: "+e.toString());
	      }
	}
}
