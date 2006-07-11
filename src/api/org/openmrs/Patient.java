package org.openmrs;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Patient
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class Patient implements java.io.Serializable {

	public static final long serialVersionUID = 93123L;
	protected final Log log = LogFactory.getLog(getClass());

	// Fields
	
	private Person person;

	private Integer patientId;
	private String gender;
	private String race;
	private Date birthdate;
	private Boolean birthdateEstimated = false;
	private String birthplace;
	private String citizenship;
	private Tribe tribe;
	private String mothersName;
	private Concept civilStatus;
	private Boolean dead = false;
	private Date deathDate;
	private String causeOfDeath;
	private String healthDistrict;
	private Integer healthCenter;
	private Set<PatientAddress> addresses;
	private Set<PatientName> names;
	private Set<PatientIdentifier> identifiers;

	private User creator; 
	private Date dateCreated; 
	private User changedBy;
	private Date dateChanged; 
	private Boolean voided = false; 
	private User voidedBy;
	private Date dateVoided; 
	private String voidReason;
	

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

	public int hashCode() {
		if (this.getPatientId() == null)
			return super.hashCode();
		int hash = 3;
		hash = 31 * hash + this.getPatientId().hashCode();
		return hash;
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
		this.birthdate = birthdate;
	}

	/**
	 * @return true if patient's birthdate is estimated
	 */
	public Boolean isBirthdateEstimated() {
		// if (this.birthdateEstimated == null) {
		// return new Boolean(false);
		// }
		return this.birthdateEstimated;
	}

	public Boolean getBirthdateEstimated() {
		return isBirthdateEstimated();
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
	 * @param tribe
	 *            patient's tribe
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
	public Concept getCivilStatus() {
		return this.civilStatus;
	}

	/**
	 * @param civilStatus
	 *            patient's civil(marriage) status
	 */
	public void setCivilStatus(Concept civilStatus) {
		this.civilStatus = civilStatus;
	}

	/**
	 * @return Returns the death status.
	 */
	public Boolean isDead() {
		return dead;
	}
	
	/**
	 * @return Returns the death status.
	 */
	public Boolean getDead() {
		return isDead();
	}

	/**
	 * @param dead The dead to set.
	 */
	public void setDead(Boolean dead) {
		this.dead = dead;
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
	 * @return list of known addresses for patient
	 * @see org.openmrs.PatientAddress
	 */
	public Set<PatientAddress> getAddresses() {
		if (addresses == null)
			addresses = new HashSet<PatientAddress>();
		return this.addresses;
	}

	/**
	 * @param patientAddresses
	 *            list of known addresses for patient
	 * @see org.openmrs.PatientAddress
	 */
	public void setAddresses(Set<PatientAddress> addresses) {
		this.addresses = addresses;
	}

	/**
	 * @return all known names for patient
	 * @see org.openmrs.PatientName
	 */
	public Set<PatientName> getNames() {
		if (names == null)
			names = new HashSet<PatientName>();
		return this.names;
	}

	/**
	 * @param names
	 *            update all known names for patient
	 * @see org.openmrs.PatientName
	 */
	public void setNames(Set<PatientName> names) {
		this.names = names;
	}

	/**
	 * @return all known identifiers for patient
	 * @see org.openmrs.PatientIdentifier
	 */
	public Set<PatientIdentifier> getIdentifiers() {
		if (identifiers == null)
			identifiers = new HashSet<PatientIdentifier>();
		return this.identifiers;
	}

	/**
	 * @param patientIdentifiers
	 *            update all known identifiers for patient
	 * @see org.openmrs.PatientIdentifier
	 */
	public void setIdentifiers(Set<PatientIdentifier> identifiers) {
		this.identifiers = identifiers;
	}

	// Convenience methods

	public void addName(PatientName name) {
		name.setPatient(this);
		if (names == null)
			names = new HashSet<PatientName>();
		if (!names.contains(name) && name != null)
			names.add(name);
	}

	public void removeName(PatientName name) {
		if (names != null)
			names.remove(name);
	}

	public void addAddress(PatientAddress address) {
		address.setPatient(this);
		if (addresses == null)
			addresses = new HashSet<PatientAddress>();
		if (!addresses.contains(address) && address != null)
			addresses.add(address);
	}

	public void removeAddress(PatientAddress address) {
		if (addresses != null)
			addresses.remove(address);
	}

	public void addIdentifier(PatientIdentifier patientIdentifier) {
		patientIdentifier.setPatient(this);
		if (identifiers == null)
			identifiers = new HashSet<PatientIdentifier>();
		if (!identifiers.contains(patientIdentifier)
				&& patientIdentifier != null)
			identifiers.add(patientIdentifier);
	}

	public void removeIdentifier(PatientIdentifier patientIdentifier) {
		if (identifiers != null)
			identifiers.remove(patientIdentifier);
	}

	/**
	 * Convenience method to get the "preferred" name for the patient.
	 * 
	 * @return Returns the "preferred" patient name.
	 */
	public PatientName getPatientName() {
		if (names != null && names.size() > 0) {
			return (PatientName) names.toArray()[0];
		} else {
			return null;
		}
	}

	/**
	 * Convenience method to get the "preferred" identifier for patient.
	 * 
	 * @return Returns the "preferred" patient identifier.
	 */
	public PatientIdentifier getPatientIdentifier() {
		if (identifiers != null && identifiers.size() > 0) {
			return (PatientIdentifier) identifiers.toArray()[0];
		} else {
			return null;
		}
	}
	
	public PatientIdentifier getPatientIdentifier(Integer identifierTypeId) {
		if (identifiers != null && identifiers.size() > 0) {
			PatientIdentifier found = null;
			for (PatientIdentifier id : identifiers) {
				if (id.getIdentifierType().getPatientIdentifierTypeId().equals(identifierTypeId)) {
					found = id;
					if (found.isPreferred())
						return found;
				}
			}
			return found;
		} else {
			return null;
		}
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

	public String toString() {
		return "Patient#" + patientId;
	}

	/**
	 * @return Returns the person.
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * @param person The person to set.
	 */
	public void setPerson(Person person) {
		this.person = person;
	}

	public User getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateVoided() {
		return dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	public Boolean getVoided() {
		return isVoided();
	}
	
	public Boolean isVoided() {
		return voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	public User getVoidedBy() {
		return voidedBy;
	}

	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	public String getVoidReason() {
		return voidReason;
	}

	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

}