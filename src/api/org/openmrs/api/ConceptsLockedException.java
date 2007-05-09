package org.openmrs.api;


public class ConceptsLockedException extends APIException {

	private static final long serialVersionUID = 132352321232223L;

	public ConceptsLockedException() {
		this("The concepts are currently locked. Editing of concepts is not allowed at this time.");
	}
	
	public ConceptsLockedException(String message) {
		super(message);
	}

	public ConceptsLockedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConceptsLockedException(Throwable cause) {
		super(cause);
	}
}
