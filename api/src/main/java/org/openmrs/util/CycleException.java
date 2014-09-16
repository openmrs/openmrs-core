package org.openmrs.util;

public class CycleException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public CycleException() {
	}
	
	public CycleException(String message) {
		super(message);
	}
	
}
