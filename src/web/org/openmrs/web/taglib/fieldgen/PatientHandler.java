package org.openmrs.web.taglib.fieldgen;

public class PatientHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
	private String defaultUrl = "patient.field";
	
	public void run() {
		setUrl(defaultUrl);

		/*
		//System.out.println("\n\n\nIN GETOUTPUT() METHOD OF PATIENTHANDLER\n\n\n");
		return startingOutput;
		*/
	}
}
