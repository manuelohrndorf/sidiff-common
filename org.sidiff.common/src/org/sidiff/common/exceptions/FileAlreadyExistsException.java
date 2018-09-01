package org.sidiff.common.exceptions;

public class FileAlreadyExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1042970624040235740L;

	public FileAlreadyExistsException(){
		super();
	}
	
	public FileAlreadyExistsException(String message){
		super(message);
	}
}
