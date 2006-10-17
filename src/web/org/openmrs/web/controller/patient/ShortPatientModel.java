package org.openmrs.web.controller.patient;

import java.util.Date;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientName;

public class ShortPatientModel {
	private Integer patientId;
	private String identifier = "";
	private Boolean identifierCheckDigit = false;
	private String otherIdentifiers = "";
	private String familyName = "";
	private String middleName = "";
	private String givenName = "";
	private String otherNames = "";
	private String gender;
	private Location healthCenter = null;
	private String tribe = "";
	private Date birthdate;
	private Boolean birthdateEstimated = false;
	private String mothersName;
	private PatientAddress address;
	private Boolean voided = false;
	private Boolean dead = false;
	private String causeOfDeath = "";
	private Date deathDate = null;
	
	public Boolean getDead() {
		return dead;
	}

	public void setDead(Boolean dead) {
		this.dead = dead;
	}

	public ShortPatientModel() { }
	
	public ShortPatientModel(Patient patient) {
		this();
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
			
			// get patient's names
			first = true;
			for (PatientName pn : patient.getNames()) {
				if (first) {
					familyName = pn.getFamilyName();
					middleName = pn.getMiddleName();
					givenName = pn.getGivenName();
					first = false;
				} else {
					if (otherNames != "")
						otherNames += ",";
					otherNames += " " + pn.getGivenName() + " " + pn.getMiddleName() + " " + pn.getFamilyName();
				}
			}
			
			gender = patient.getGender();
			if (patient.getTribe() != null)
				tribe = patient.getTribe().getName();
			
			birthdate = patient.getBirthdate();
			birthdateEstimated = patient.isBirthdateEstimated();
			mothersName = patient.getMothersName();
			healthCenter = patient.getHealthCenter();
			voided = patient.isVoided();
			dead = patient.isDead();
			causeOfDeath = patient.getCauseOfDeath();
			deathDate = patient.getDeathDate();
						
			address = patient.getPatientAddress();
		}
	}

	public PatientAddress getAddress() {
		return address;
	}

	public void setAddress(PatientAddress address) {
		this.address = address;
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

	public Boolean getIdentifierCheckDigit() {
		return identifierCheckDigit;
	}

	public void setIdentifierCheckDigit(Boolean identifierCheckDigit) {
		this.identifierCheckDigit = identifierCheckDigit;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getMothersName() {
		return mothersName;
	}

	public void setMothersName(String mothersName) {
		this.mothersName = mothersName;
	}

	public String getOtherIdentifiers() {
		return otherIdentifiers;
	}

	public void setOtherIdentifiers(String otherIdentifiers) {
		this.otherIdentifiers = otherIdentifiers;
	}

	public String getOtherNames() {
		return otherNames;
	}

	public void setOtherNames(String otherNames) {
		this.otherNames = otherNames;
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public String getTribe() {
		return tribe;
	}

	public void setTribe(String tribe) {
		this.tribe = tribe;
	}

	public Boolean getVoided() {
		return voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	public Location getHealthCenter() {
		return healthCenter;
	}

	public void setHealthCenter(Location healthCenter) {
		this.healthCenter = healthCenter;
	}

	public String getCauseOfDeath() {
		return causeOfDeath;
	}

	public void setCauseOfDeath(String causeOfDeath) {
		this.causeOfDeath = causeOfDeath;
	}

	public Date getDeathDate() {
		return deathDate;
	}

	public void setDeathDate(Date deathDate) {
		this.deathDate = deathDate;
	}
	
}