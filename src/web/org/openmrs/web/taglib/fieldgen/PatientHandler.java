package org.openmrs.web.taglib.fieldgen;

public class PatientHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
	private String defaultUrl = "patient.field";
	
	public void run() {
		setUrl(defaultUrl);

	}
}
