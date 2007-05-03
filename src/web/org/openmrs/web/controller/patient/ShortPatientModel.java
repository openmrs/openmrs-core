package org.openmrs.web.controller.patient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;

public class ShortPatientModel {
	private Integer patientId;
	private String identifier = "";
	private Boolean identifierCheckDigit = false;
	private String otherIdentifiers = "";
	private PersonName name = new PersonName();
	private String otherNames = "";
	private String gender;
	private String tribe = "";
	private Date birthdate;
	private Boolean birthdateEstimated = false;
	private PersonAddress address;
	private Boolean voided = false;
	private Boolean dead = false;
	private Concept causeOfDeath = null;
	private Date deathDate = null;
	
	// convenience map:
	// Map<attribute.getAttributeType().getName(), attribute>
	Map<String, PersonAttribute> attributeMap = null;
	
	// private Location healthCenter = null;
	// private String mothersName;
	
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
			for (PersonName pn : patient.getNames()) {
				if (first) {
					setName(pn);
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
			//mothersName = patient.getMothersName();
			//healthCenter = patient.getHealthCenter();
			voided = patient.isVoided();
			dead = patient.isDead();
			causeOfDeath = patient.getCauseOfDeath();
			deathDate = patient.getDeathDate();
						
			address = patient.getPersonAddress();
			
			attributeMap = new HashMap<String, PersonAttribute>();
			for (PersonAttribute attribute : patient.getActiveAttributes()) {
				attributeMap.put(attribute.getAttributeType().getName(), attribute);
			}
		}
	}

	public PersonAddress getAddress() {
		return address;
	}

	public void setAddress(PersonAddress address) {
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
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

	public Concept getCauseOfDeath() {
		return causeOfDeath;
	}

	public void setCauseOfDeath(Concept causeOfDeath) {
		this.causeOfDeath = causeOfDeath;
	}

	public Date getDeathDate() {
		return deathDate;
	}

	public void setDeathDate(Date deathDate) {
		this.deathDate = deathDate;
	}

	public Map<String, PersonAttribute> getAttributeMap() {
		return attributeMap;
	}

	public PersonName getName() {
		return name;
	}

	public void setName(PersonName name) {
		this.name = name;
	}
	
}