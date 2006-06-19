package org.openmrs.web.dwr;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.PatientName;

public class EncounterListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer encounterId;
	private String encounterType;
	private String patientName;
	private String location;
	private String providerName;
	private String formName;
	private Date encounterDateTime;
	private boolean voided = false;


	public EncounterListItem() { }
	
	public EncounterListItem(Encounter encounter) {

		if (encounter != null) {
			encounterId = encounter.getEncounterId();
			encounterDateTime = encounter.getEncounterDatetime();
			PatientName pn = encounter.getPatient().getPatientName();
			if (pn != null) {
				patientName = "";
				if (pn.getGivenName() != null)
					patientName += pn.getGivenName();
				if (pn.getMiddleName() != null)
					patientName += " " + pn.getMiddleName();
				if (pn.getFamilyName() != null)
					patientName += " " + pn.getFamilyName();
			}
			if (encounter.getProvider() != null)
				providerName = encounter.getProvider().getFirstName() + " " + encounter.getProvider().getLastName();
			if (encounter.getLocation() != null)
				location = encounter.getLocation().getName();
			if (encounter.getEncounterType() != null)
				encounterType = encounter.getEncounterType().getName();
			if (encounter.getForm() != null)
				formName = encounter.getForm().getName();
			voided = encounter.isVoided();
		}
	}

	public Integer getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}

	public Date getEncounterDateTime() {
		return encounterDateTime;
	}

	public void setEncounterDateTime(Date encounterDateTime) {
		this.encounterDateTime = encounterDateTime;
	}

	public String getEncounterType() {
		return encounterType;
	}

	public void setEncounterType(String encounterType) {
		this.encounterType = encounterType;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public boolean isVoided() {
		return voided;
	}

	public void setVoided(boolean voided) {
		this.voided = voided;
	}
	
	

}
