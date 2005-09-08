package org.openmrs;

import java.util.Date;
import java.util.List;

/**
 * Patient
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
	private boolean voided;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;
	private List<PatientAddress> addresses;
	private List<PatientName> names;
	private List<PatientIdentifier> identifiers;

	// Constructors

	/** default constructor */
	public Patient() {
	}

	// Property accessors

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
		this.race = race;
	}

	/**
	 * @return patient's data of birth
	 */
	public Date getBirthdate() {
		return this.birthdate;
	}

	/**
	 * @param birthdate
	 *            patient's date of birth
	 */
	public void setBirthdate(Date birthdate) {
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
	 *            patient's civil status
	 */
	public void setCivilStatus(Integer civilStatus) {
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
	public boolean isVoided() {
		return this.voided;
	}

	/**
	 * @param voided
	 *            true if patient's record should be voided from the system
	 */
	public void setVoided(boolean voided) {
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
		this.voidReason = voidReason;
	}

	/**
	 * @return list of known addresses for patient
	 * @see org.openmrs.PatientAddress
	 */
	public List<PatientAddress> getAddresses() {
		return this.addresses;
	}

	/**
	 * @param patientAddresses list of known addresses for patient
	 * @see org.openmrs.PatientAddress
	 */
	public void setAddresses(List<PatientAddress> addresses) {
		this.addresses = addresses;
	}

	/**
	 * @return all known names for patient
	 * @see org.openmrs.PatientName
	 */
	public List<PatientName> getNames() {
		return this.names;
	}

	/**
	 * @param names update all known names for patient
	 * @see org.openmrs.PatientName
	 */
	public void setNames(List<PatientName> names) {
		this.names = names;
	}

	/**
	 * @return all known identifiers for patient
	 * @see org.openmrs.PatientIdentifier
	 */
	public List<PatientIdentifier> getIdentifiers() {
		return this.identifiers;
	}

	/**
	 * @param patientIdentifiers update all known identifiers for patient
	 * @see org.openmrs.PatientIdentifier
	 */
	public void setIdentifiers(List<PatientIdentifier> identifiers) {
		this.identifiers = identifiers;
	}
	
	// Convenience methods

	public void addName(PatientName name) {
		name.setPatient(this);
		names.add(name);
	}

	public void removeName(PatientName name) {
		names.remove(name);
	}

}