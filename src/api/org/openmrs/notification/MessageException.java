package org.openmrs.notification;

public class MessageException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5421053761024878322L;

	public MessageException() {
		super();
	}
	
	public MessageException(Throwable cause) {
		super(cause);
	}
	
	public MessageException(String message) {
		super(message);
	}
	
	public MessageException(String message, Throwable cause) {
		super(message, cause);
	}

}
