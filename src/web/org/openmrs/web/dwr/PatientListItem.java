package org.openmrs.web.dwr;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientName;

public class PatientListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer patientId;
	private String identifier;
	private String familyName;
	private String givenName;
	private String gender;
	private String race;
	private Date birthdate;
	private Boolean birthdateEstimated;
	private String mothersName;

	public PatientListItem() { }
	
	public PatientListItem(Patient patient) {
		patientId = patient.getPatientId();
		identifier = patient.getIdentifiers().iterator().next().getIdentifier();
		PatientName pn = patient.getNames().iterator().next();
		familyName = pn.getFamilyName();
		givenName = pn.getGivenName();
		gender = patient.getGender();
		race = patient.getRace();
		birthdate = patient.getBirthdate();
		birthdateEstimated = patient.isBirthdateEstimated();
		mothersName = patient.getMothersName();
	}
	
	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public Boolean getBirthdateEstimated() {
		return birthdateEstimated;
	}

	public void setBirthdateEstimated(Boolean birthdateEstimated) {
		this.birthdateEstimated = birthdateEstimated;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getMothersName() {
		return mothersName;
	}

	public void setMothersName(String mothersName) {
		this.mothersName = mothersName;
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public String getRace() {
		return race;
	}

	public void setRace(String race) {
		this.race = race;
	}

}
