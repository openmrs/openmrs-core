package org.openmrs;

import java.util.Collection;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Defines a Patient in the system.  A patient is simply an extension
 * of a person and all that that implies.
 * 
 * @author Burke Mamlin
 * @author Ben Wolfe
 * @version 2.0
 */
public class Patient extends Person implements java.io.Serializable {

	public static final long serialVersionUID = 93123L;
	protected static final Log log = LogFactory.getLog(Patient.class);

	// Fields
	
	//private Person person;

	private Integer patientId;
	private Tribe tribe;
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

	public Patient(Person person) {
		super(person);
		this.patientId = person.getPersonId();
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
		super.setPersonId(patientId);
		this.patientId = patientId;
	}
	
	/**
	 * Overrides the parent setPersonId(Integer) so that we can be sure patient id
	 * is also set correctly.
	 * 
	 * @see org.openmrs.Person#setPersonId(java.lang.Integer)
	 */
	public void setPersonId(Integer personId) {
		super.setPersonId(personId);
		this.patientId = personId;
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

	/**
	 * Will add this PatientIdentifier if the patient doesn't contain it already
	 * @param patientIdentifier
	 */
	/**
	 * Will only add PatientIdentifiers in this list that this
	 * patient does not have already
	 *  
	 * @param patientIdentifiers
	 */
	public void addIdentifiers(Collection<PatientIdentifier> patientIdentifiers) {
		for (PatientIdentifier identifier : patientIdentifiers)
			addIdentifier(identifier);
	}
	
	/**
	 * Will add this PatientIdentifier if the patient doesn't contain it already
	 * @param patientIdentifier
	 */
	public void addIdentifier(PatientIdentifier patientIdentifier) {
		patientIdentifier.setPatient(this);
		if (identifiers == null)
			identifiers = new HashSet<PatientIdentifier>();
		if (patientIdentifier != null && !identifiers.contains(patientIdentifier))
			identifiers.add(patientIdentifier);
	}

	public void removeIdentifier(PatientIdentifier patientIdentifier) {
		if (identifiers != null)
			identifiers.remove(patientIdentifier);
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
	
	/**
	 * Return's the first (preferred) patient identifier matching <code>identifierTypeId</code>
	 * 
	 * @param identifierTypeId
	 * @return preferred patient identifier
	 */
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
	
	/**
	 * Return's the first (preferred) patient identifier matching <code>identifierTypeName</code>
	 * 
	 * @param identifierTypeName
	 * @return preferred patient identifier
	 */
	public PatientIdentifier getPatientIdentifier(String identifierTypeName) {
		if (identifiers != null && identifiers.size() > 0) {
			PatientIdentifier found = null;
			for (PatientIdentifier id : identifiers) {
				if (id.getIdentifierType().getName().equals(identifierTypeName)) {
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
	
	/**
	 * Returns only the non-voided identifiers for this patient
	 * 
	 * @return list identifiers
	 */
	public List<PatientIdentifier> getActiveIdentifiers() {
		List<PatientIdentifier> ids = new Vector<PatientIdentifier>();
		if (identifiers != null) {
			for (PatientIdentifier pi : identifiers) {
				if (pi.isVoided() == false)
					ids.add(pi);
			}
		}
		return ids;
	}
	
	public String toString() {
		return "Patient#" + patientId;
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