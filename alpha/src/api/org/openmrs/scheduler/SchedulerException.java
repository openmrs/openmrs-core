package org.openmrs.scheduler;

public class SchedulerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4462693049954360187L;

	public SchedulerException() {
		super();
	}
	
	public SchedulerException(Throwable cause) {
		super(cause);
	}
	
	public SchedulerException(String message) {
		super(message);
	}
	
	public SchedulerException(String message, Throwable cause) {
		super(message, cause);
	}

}
