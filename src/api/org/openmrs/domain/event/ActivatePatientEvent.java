package org.openmrs.domain.event;


import org.springframework.context.ApplicationEvent;
import java.util.Collection;

public class ActivatePatientEvent extends ApplicationEvent {

	private Collection patients;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5747908443002131089L;

	public ActivatePatientEvent(Object source, Collection patients) { 
		super( source );
		this.patients = patients;
	}
}


