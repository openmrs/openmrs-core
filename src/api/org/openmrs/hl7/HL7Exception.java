package org.openmrs.hl7;

public class HL7Exception extends Exception {
	
	private static final long serialVersionUID = -3279754307712384801L;

	public HL7Exception() {
		super();
	}
	
	public HL7Exception(Throwable cause) {
		super(cause);
	}
	
	public HL7Exception(String message) {
		super(message);
	}
	
	public HL7Exception(String message, Throwable cause) {
		super(message, cause);
	}

}
