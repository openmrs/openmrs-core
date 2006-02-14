package org.openmrs.domain.event;



import org.springframework.context.ApplicationEvent;
import org.openmrs.Patient;


public class CreatePatientEvent extends ApplicationEvent {

	private Patient patient;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6409892142326526949L;

	public CreatePatientEvent(Object source, Patient patient) { 
		super( source );
		this.patient = patient;
	}
	
	
	public void setPatient(Patient patient) { 
		this.patient = patient;
	}
	
	public Patient getPatient() { 		
		return this.patient;
	}
}


