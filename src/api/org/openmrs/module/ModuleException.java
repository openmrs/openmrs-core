package org.openmrs.module;

/**
 * Represents often fatal errors that occur within the module package
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class ModuleException extends RuntimeException {
	public static final long serialVersionUID = 236472665L;
	
    public ModuleException(String message) {
    	super(message);
        }
    
	public ModuleException(String message, Throwable cause) {
        super(message, cause);
    }
	
	public ModuleException(String message, String moduleName) {
		super (message + " Module: " + moduleName);
	}
	
	public ModuleException(String message, String moduleName, Throwable cause) {
		super (message + " Module: " + moduleName, cause);
	}

}
