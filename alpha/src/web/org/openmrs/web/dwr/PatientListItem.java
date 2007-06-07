package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;

public class PatientListItem extends PersonListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer patientId;
	private String identifier = "";
	private Boolean identifierCheckDigit = false;
	private String otherIdentifiers = "";
	private String tribe = "";
	
	public PatientListItem() { }
	
	public PatientListItem(Patient patient) {
		super(patient);
		
		if (patient != null) {
			
			patientId = patient.getPatientId();
			
			// get patient's identifiers
			boolean first = true;
			for (PatientIdentifier pi : patient.getIdentifiers()) {
				if (first) {
					identifier = pi.getIdentifier();
					identifierCheckDigit = pi.getIdentifierType().hasCheckDigit();
					first = false;
				}
				else {
					if (otherIdentifiers != "")
						otherIdentifiers += ",";
					otherIdentifiers += " " + pi.getIdentifier();
				}
			}
			
			if (patient.getTribe() != null)
				tribe = patient.getTribe().getName();
		}
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof PatientListItem) {
			PatientListItem pi = (PatientListItem)obj;
			if (pi.getPatientId() == null || patientId == null)
				return false;
			return pi.getPatientId().equals(patientId);
		}
		return false;
	}
	
	public int hashCode() {
		if (patientId == null)
			return super.hashCode();
		return patientId.hashCode();
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public String getOtherIdentifiers() {
		return otherIdentifiers;
	}

	public void setOtherIdentifiers(String otherIdentifiers) {
		this.otherIdentifiers = otherIdentifiers;
	}
	
	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public String getTribe() {
		if (tribe == null) tribe = "";
		return tribe;
	}

	public void setTribe(String tribe) {
		this.tribe = tribe;
	}

	/**
	 * @return Returns the identifierIdentifierCheckdigit.
	 */
	public Boolean getIdentifierCheckDigit() {
		return identifierCheckDigit;
	}

	/**
	 * @param identifierIdentifierCheckdigit The identifierIdentifierCheckdigit to set.
	 */
	public void setIdentifierCheckDigit(Boolean identifierCheckDigit) {
		this.identifierCheckDigit = identifierCheckDigit;
	}
	
}
