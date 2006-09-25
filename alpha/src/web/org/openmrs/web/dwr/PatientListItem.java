package org.openmrs.web.dwr;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientName;

public class PatientListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer patientId;
	private String identifier = "";
	private Boolean identifierCheckDigit = false;
	private String otherIdentifiers = "";
	private String familyName = "";
	private String middleName = "";
	private String givenName = "";
	private String otherNames = "";
	private String gender;
	private String tribe = "";
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
			voided = patient.isVoided();
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
	
	public String getOtherIdentifiers() {
		return otherIdentifiers;
	}

	public void setOtherIdentifiers(String otherIdentifiers) {
		this.otherIdentifiers = otherIdentifiers;
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
	
	public String getOtherNames() {
		return otherNames;
	}

	public void setOtherNames(String otherNames) {
		this.otherNames = otherNames;
	}

	public Integer getAge() {
		
		if (birthdate == null)
			return null;
		
		Calendar today = Calendar.getInstance();
		
		Calendar bday = new GregorianCalendar();
		bday.setTime(birthdate);
		
		int age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);
		
		//tricky bit:
		// set birthday calendar to this year
		// if the current date is less that the new 'birthday', subtract a year
		bday.set(Calendar.YEAR, today.get(Calendar.YEAR));
		if (today.before(bday)) {
				age = age -1;
		}
		
		return age;
	}
}
