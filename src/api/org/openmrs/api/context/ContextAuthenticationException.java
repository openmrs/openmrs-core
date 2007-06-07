package org.openmrs.api.context;

public class ContextAuthenticationException extends Exception {
	
	public static final long serialVersionUID = 22323L;
	
	public ContextAuthenticationException() {
		super();
	}
	
	public ContextAuthenticationException(String message) {
		super(message);
	}
	
	public ContextAuthenticationException(Throwable cause) {
		super(cause);
	}

}
