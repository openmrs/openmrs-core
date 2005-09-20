package org.openmrs;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Patient
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class Patient implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer patientId;
	private String gender;
	private String race;
	private Date birthdate;
	private boolean birthdateEstimated;
	private String birthplace;
	private String citizenship;
	private Tribe tribe;
	private String mothersName;
	private Integer civilStatus;
	private Date deathDate;
	private String causeOfDeath;
	private String healthDistrict;
	private Integer healthCenter;
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private Boolean voided;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;
	private List<PatientAddress> addresses;
	private List<PatientName> names;
	private List<PatientIdentifier> identifiers;
	private boolean dirty;

	// Constructors

	/** default constructor */
	public Patient() {
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Patient) {
			Patient p = (Patient) obj;
			if (this.getPatientId() != null && p.getPatientId() != null)
				return (this.getPatientId().equals(p.getPatientId()));
		}
		return false;
	}
	
	/**
	 * True if a property of an object in a List has been modified
	 * 
	 * @return true/false whether the patient object has been modified
	 */
	public boolean isDirty() {
		if (dirty == true)
			return true;
		for(Iterator i = addresses.iterator(); i.hasNext();) {
			PatientAddress p = (PatientAddress)i;
			if (p.isDirty())
				return true;
		}
		for(Iterator i = names.iterator(); i.hasNext();) {
			PatientName p = (PatientName)i;
			if (p.isDirty())
				return true;
		}
		for(Iterator i = identifiers.iterator(); i.hasNext();) {
			PatientIdentifier p = (PatientIdentifier)i;
			if (p.isDirty())
				return true;
		}
		return false;
	}
	// Property accessors

	/**
	 * For unsetting the dirty bit after modification
	 */
	public void setClean() {
		dirty = false;
	}
	
	/**
	 * @return internal identifier for patient
	 */
	public Integer getPatientId() {
		return this.patientId;
	}

	/**
	 * Sets the internal identifier for a patient. <b>This should never be
	 * called directly</b>. It exists only for the use of the supporting
	 * infrastructure.
	 * 
	 * @param patientId
	 */
	public void setPatientId(Integer patientId) {
		dirty = true;
		this.patientId = patientId;
	}

	/**
	 * @return patient's gender
	 */
	public String getGender() {
		return this.gender;
	}

	/**
	 * @param gender
	 *            patient's gender
	 */
	public void setGender(String gender) {
		dirty = true;
		this.gender = gender;
	}

	/**
	 * @return patient's race
	 */
	public String getRace() {
		return this.race;
	}

	/**
	 * @param race
	 *            patient's race
	 */
	public void setRace(String race) {
		dirty = true;
		this.race = race;
	}

	/**
	 * @return patient's date of birth
	 */
	public Date getBirthdate() {
		return this.birthdate;
	}

	/**
	 * @param birthdate
	 *            patient's date of birth
	 */
	public void setBirthdate(Date birthdate) {
		dirty = true;
		this.birthdate = birthdate;
	}

	/**
	 * @return true if patient's birthdate is estimated
	 */
	public Boolean isBirthdateEstimated() {
		return this.birthdateEstimated;
	}

	/**
	 * @param birthdateEstimated
	 *            true if patient's birthdate is estimated
	 */
	public void setBirthdateEstimated(Boolean birthdateEstimated) {
		dirty = true;
		this.birthdateEstimated = birthdateEstimated;
	}

	/**
	 * @return patient's birthplace
	 */
	public String getBirthplace() {
		return this.birthplace;
	}

	/**
	 * @param birthplace
	 *            patient's birthplace
	 */
	public void setBirthplace(String birthplace) {
		dirty = true;
		this.birthplace = birthplace;
	}

	/**
	 * @return patient's citizenship
	 */
	public String getCitizenship() {
		return this.citizenship;
	}

	/**
	 * @param citizenship
	 *            patient's citizenship
	 */
	public void setCitizenship(String citizenship) {
		dirty = true;
		this.citizenship = citizenship;
	}
	
	/**
	 * @return patient's tribe
	 */
	public Tribe getTribe() {
		return tribe;
	}
	
	/**
	 * @param tribe patient's tribe
	 */
	public void setTribe(Tribe tribe) {
		dirty = true;
		this.tribe = tribe;
	}

	/**
	 * @return patient's mother's name
	 */
	public String getMothersName() {
		return this.mothersName;
	}

	/**
	 * @param mothersName
	 *            patient's mother's name
	 */
	public void setMothersName(String mothersName) {
		dirty = true;
		this.mothersName = mothersName;
	}

	/**
	 * @return patient's civil status (single, married, separated, divorced,
	 *         etc.)
	 */
	public Integer getCivilStatus() {
		return this.civilStatus;
	}

	/**
	 * @param civilStatus
	 *            patient's civil(marriage) status 
	 */
	public void setCivilStatus(Integer civilStatus) {
		dirty = true;
		this.civilStatus = civilStatus;
	}

	/**
	 * @return date of patient's death
	 */
	public Date getDeathDate() {
		return this.deathDate;
	}

	/**
	 * @param deathDate
	 *            date of patient's death
	 */
	public void setDeathDate(Date deathDate) {
		dirty = true;
		this.deathDate = deathDate;
	}

	/**
	 * @return cause of patient's death
	 */
	public String getCauseOfDeath() {
		return this.causeOfDeath;
	}

	/**
	 * @param causeOfDeath
	 *            cause of patient's death
	 */
	public void setCauseOfDeath(String causeOfDeath) {
		dirty = true;
		this.causeOfDeath = causeOfDeath;
	}

	/**
	 * @return patient's health district
	 */
	public String getHealthDistrict() {
		return this.healthDistrict;
	}

	/**
	 * @param healthDistrict
	 *            patient's health district
	 */
	public void setHealthDistrict(String healthDistrict) {
		dirty = true;
		this.healthDistrict = healthDistrict;
	}

	/**
	 * @return patient's health center
	 */
	public Integer getHealthCenter() {
		return this.healthCenter;
	}

	/**
	 * @param healthCenter
	 *            patient's health center
	 */
	public void setHealthCenter(Integer healthCenter) {
		dirty = true;
		this.healthCenter = healthCenter;
	}

	/**
	 * @return user who created this patient record
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            user creating this patient record
	 */
	public void setCreator(User creator) {
		dirty = true;
		this.creator = creator;
	}

	/**
	 * @return date on which patient record was created
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	/**
	 * @param dateCreated
	 *            date on which patient record was created
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @return user who last changed the patient's record
	 */
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy user who last changed the patient's record
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return date patient's record was last changed
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged date the patient's record was last changed
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * @return true if patient's record has been voided
	 */
	public Boolean isVoided() {
		return this.voided;
	}

	/**
	 * @param voided
	 *            true if patient's record should be voided from the system
	 */
	public void setVoided(Boolean voided) {
		dirty = true;
		this.voided = voided;
	}

	/**
	 * @return user who voided patient record
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy user who voided patient record
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return date patient's record was voided
	 */
	public Date getDateVoided() {
		return this.dateVoided;
	}

	/**
	 * @param dateVoided date patient's record was voided
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * @return reason patient's record was voided
	 */
	public String getVoidReason() {
		return this.voidReason;
	}

	/**
	 * @param voidReason reason patient's record was voided
	 */
	public void setVoidReason(String voidReason) {
		dirty = true;
		this.voidReason = voidReason;
	}

	/**
	 * @return list of known addresses for patient
	 * @see org.openmrs.PatientAddress
	 */
	public List<PatientAddress> getAddresses() {
		if (addresses == null)
			addresses = new LinkedList<PatientAddress>();
		return this.addresses;
	}

	/**
	 * @param patientAddresses list of known addresses for patient
	 * @see org.openmrs.PatientAddress
	 */
	public void setAddresses(List<PatientAddress> addresses) {
		dirty = true;
		this.addresses = addresses;
	}

	/**
	 * @return all known names for patient
	 * @see org.openmrs.PatientName
	 */
	public List<PatientName> getNames() {
		if (names == null)
			names = new LinkedList<PatientName>();
		return this.names;
	}

	/**
	 * @param names update all known names for patient
	 * @see org.openmrs.PatientName
	 */
	public void setNames(List<PatientName> names) {
		dirty = true;
		this.names = names;
	}

	/**
	 * @return all known identifiers for patient
	 * @see org.openmrs.PatientIdentifier
	 */
	public List<PatientIdentifier> getIdentifiers() {
		if (identifiers == null)
			identifiers = new LinkedList<PatientIdentifier>();
		return this.identifiers;
	}

	/**
	 * @param patientIdentifiers update all known identifiers for patient
	 * @see org.openmrs.PatientIdentifier
	 */
	public void setIdentifiers(List<PatientIdentifier> identifiers) {
		dirty = true;
		this.identifiers = identifiers;
	}
	
	// Convenience methods

	public void addName(PatientName name) {
		dirty = true;
		name.setPatient(this);
		if (names == null)
			names = new LinkedList<PatientName>();
		if (!names.contains(name) && name != null)
			names.add(name);
	}

	public void removeName(PatientName name) {
		dirty = true;
		if (names != null)
			names.remove(name);
	}
	
	public void addAddress(PatientAddress address) {
		dirty = true;
		address.setPatient(this);
		if (addresses == null)
			addresses = new LinkedList<PatientAddress>();
		if (!addresses.contains(address) && address != null)
			addresses.add(address);
	}
	public void removeAddress(PatientAddress address) {
		dirty = true;
		if (addresses != null)
			addresses.remove(address);
	}

	public void addIdentifier(PatientIdentifier patientIdentifier) {
		dirty = true;
		patientIdentifier.setPatient(this);
		if (identifiers == null)
			identifiers = new LinkedList<PatientIdentifier>();
		if (!identifiers.contains(patientIdentifier) && patientIdentifier != null)
			identifiers.add(patientIdentifier);
	}
	public void removeIdentifier(PatientIdentifier patientIdentifier) {
		dirty = true;
		if (identifiers != null)
			identifiers.remove(patientIdentifier);
	}
	
}