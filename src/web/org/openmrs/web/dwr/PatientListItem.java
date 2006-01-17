package org.openmrs.web.dwr;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientName;

public class PatientListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer patientId;
	private String identifier;
	private String familyName;
	private String middleName;
	private String givenName;
	private String gender;
	private String tribe;
	private Date birthdate;
	private Boolean birthdateEstimated = false;
	private String mothersName;
	private String address1;
	private String address2;
	private Boolean voided = false;

	public PatientListItem() { }
	
	public PatientListItem(Patient patient) {

		if (patient != null) {
			patientId = patient.getPatientId();
			identifier = patient.getIdentifiers().iterator().next().getIdentifier();
			PatientName pn = patient.getNames().iterator().next();
			familyName = pn.getFamilyName();
			middleName = pn.getMiddleName();
			givenName = pn.getGivenName();
			gender = patient.getGender();
			tribe = patient.getTribe().getName();
			birthdate = patient.getBirthdate();
			birthdateEstimated = patient.isBirthdateEstimated();
			mothersName = patient.getMothersName();
			voided = patient.isVoided();
			if (patient.getAddresses().size() > 0) {
				PatientAddress pa = (PatientAddress)patient.getAddresses().toArray()[0];
				address1 = pa.getAddress1();
				address2 = pa.getAddress2();
			}
		}
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
		if (familyName == null) familyName = "";
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getMiddleName() {
		if (middleName == null) middleName = "";
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getGivenName() {
		if (givenName == null) givenName = "";
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
		if (mothersName == null) mothersName = "";
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

	public String getTribe() {
		if (tribe == null) tribe = "";
		return tribe;
	}

	public void setTribe(String tribe) {
		this.tribe = tribe;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	
	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public Boolean getVoided() {
		return voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	

}
