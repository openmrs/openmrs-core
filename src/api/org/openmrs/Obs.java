package org.openmrs;

import java.util.Date;
import java.util.Locale;

import org.openmrs.util.Format;

/**
 * Obs
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class Obs implements java.io.Serializable {

	public static final long serialVersionUID = 112342333L;

	// Fields

	protected Integer obsId;
	protected Concept concept;
	protected Date obsDatetime;
	protected Integer obsGroupId;
	protected String accessionNumber;
	protected Concept valueCoded;
	protected Drug valueDrug;
	protected Integer valueGroupId;
	protected Date valueDatetime;
	protected Double valueNumeric;
	protected String valueModifier;
	protected String valueText;
	protected String comment;
	protected Integer patientId;
	protected Patient patient;
	protected Order order;
	protected Location location;
	protected Encounter encounter;
	protected Date dateStarted;
	protected Date dateStopped;
	protected User creator;
	protected Date dateCreated;
	protected Boolean voided = false;
	protected User voidedBy;
	protected Date dateVoided;
	protected String voidReason;

	// Constructors

	/** default constructor */
	public Obs() {
	}

	/** constructor with id */
	public Obs(Integer obsId) {
		this.obsId = obsId;
	}

	/**
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Obs) {
			Obs o = (Obs) obj;
			if (this.getObsId() != null && o.getObsId() != null)
				return (this.getObsId().equals(o.getObsId()));
			/*
			 * return (this.getConcept().equals(o.getConcept()) &&
			 * this.getPatient().equals(o.getPatient()) &&
			 * this.getEncounter().equals(o.getEncounter()) &&
			 * this.getLocation().equals(o.getLocation()));
			 */
		}
		return false;
	}

	public int hashCode() {
		if (this.getObsId() == null)
			return super.hashCode();
		return this.getObsId().hashCode();
	}

	/**
	 * determine if the current observation is complex --overridden in extending
	 * ComplexObs class
	 */
	public boolean isComplexObs() {
		return false;
	}

	// Property accessors

	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @param concept
	 *            The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated
	 *            The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided
	 *            The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * @return Returns the encounter.
	 */
	public Encounter getEncounter() {
		return encounter;
	}

	/**
	 * @param encounter
	 *            The encounter to set.
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	/**
	 * @return Returns the location.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            The location to set.
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @return Returns the obsDatetime.
	 */
	public Date getObsDatetime() {
		return obsDatetime;
	}

	/**
	 * @param obsDatetime
	 *            The obsDatetime to set.
	 */
	public void setObsDatetime(Date obsDatetime) {
		this.obsDatetime = obsDatetime;
	}

	/**
	 * @return Returns the obsGroupId.
	 */
	public Integer getObsGroupId() {
		return obsGroupId;
	}

	/**
	 * @param obsGroupId
	 *            The obsGroupId to set.
	 */
	public void setObsGroupId(Integer obsGroupId) {
		this.obsGroupId = obsGroupId;
	}

	/**
	 * @return Returns the obsId.
	 */
	public Integer getObsId() {
		return obsId;
	}

	/**
	 * @param obsId
	 *            The obsId to set.
	 */
	public void setObsId(Integer obsId) {
		this.obsId = obsId;
	}

	/**
	 * @return Returns the order.
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * @param order
	 *            The order to set.
	 */
	public void setOrder(Order order) {
		this.order = order;
	}

	/**
	 * @return Returns the patient.
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient
	 *            The patient to set.
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	/**
	 * This converts the value_numeric to a value_boolean, essentially
	 * @return Boolean of the obs value
	 */
	public Boolean getValueAsBoolean() {
		return (getValueNumeric() == null ? null : getValueNumeric() != 0);
	}

	/**
	 * @return Returns the valueCoded.
	 */
	public Concept getValueCoded() {
		return valueCoded;
	}

	/**
	 * @param valueCoded
	 *            The valueCoded to set.
	 */
	public void setValueCoded(Concept valueCoded) {
		this.valueCoded = valueCoded;
	}
	
	/**
	 * @return Returns the valueDrug
	 */
	public Drug getValueDrug() {
		return valueDrug;
	}

	/**
	 * @param valueDrug
	 *            The valueDrug to set.
	 */
	public void setValueDrug(Drug valueDrug) {
		this.valueDrug = valueDrug;
	}

	/**
	 * @return Returns the valueDatetime.
	 */
	public Date getValueDatetime() {
		return valueDatetime;
	}

	/**
	 * @param valueDatetime
	 *            The valueDatetime to set.
	 */
	public void setValueDatetime(Date valueDatetime) {
		this.valueDatetime = valueDatetime;
	}

	/**
	 * @return Returns the valueGroupId.
	 */
	public Integer getValueGroupId() {
		return valueGroupId;
	}

	/**
	 * @param valueGroupId
	 *            The valueGroupId to set.
	 */
	public void setValueGroupId(Integer valueGroupId) {
		this.valueGroupId = valueGroupId;
	}

	/**
	 * @return Returns the valueModifier.
	 */
	public String getValueModifier() {
		return valueModifier;
	}

	/**
	 * @param valueModifier
	 *            The valueModifier to set.
	 */
	public void setValueModifier(String valueModifier) {
		this.valueModifier = valueModifier;
	}

	/**
	 * @return Returns the valueNumeric.
	 */
	public Double getValueNumeric() {
		return valueNumeric;
	}

	/**
	 * @param valueNumeric
	 *            The valueNumeric to set.
	 */
	public void setValueNumeric(Double valueNumeric) {
		this.valueNumeric = valueNumeric;
	}

	/**
	 * @return Returns the valueText.
	 */
	public String getValueText() {
		return valueText;
	}

	/**
	 * @param valueText
	 *            The valueText to set.
	 */
	public void setValueText(String valueText) {
		this.valueText = valueText;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean isVoided() {
		return voided;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean getVoided() {
		return voided;
	}

	/**
	 * @param voided
	 *            The voided to set.
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * @return Returns the voidedBy.
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy
	 *            The voidedBy to set.
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return Returns the voidReason.
	 */
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * @param voidReason
	 *            The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	/**
	 * @return Returns the accessionNumber.
	 */
	public String getAccessionNumber() {
		return accessionNumber;
	}

	/**
	 * @param accessionNumber
	 *            The accessionNumber to set.
	 */
	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	/**
	 * @return Returns the dateStarted.
	 */
	public Date getDateStarted() {
		return dateStarted;
	}

	/**
	 * @param dateStarted
	 *            The dateStarted to set.
	 */
	public void setDateStarted(Date dateStarted) {
		this.dateStarted = dateStarted;
	}

	/**
	 * @return Returns the dateStopped.
	 */
	public Date getDateStopped() {
		return dateStopped;
	}

	/**
	 * @param dateStopped
	 *            The dateStopped to set.
	 */
	public void setDateStopped(Date dateStopped) {
		this.dateStopped = dateStopped;
	}

	/***************************************************************************
	 * Convenience methods
	 **************************************************************************/

	/**
	 * Convenience method for obtaining the observation's value as a string
	 * 
	 * @param locale
	 *            locale for locale-specific depictions of value
	 */
	public String getValueAsString(Locale locale) {
		//branch on hl7 abbreviations
		if (getConcept() != null) {
			String abbrev = getConcept().getDatatype().getHl7Abbreviation();
			if (abbrev.equals("BIT"))
				return getValueAsBoolean().toString();
			else if (abbrev.equals("CWE")) {
				if (getValueCoded() == null)
					return "";
				if (getValueDrug() != null)
					return getValueDrug().getFullName(locale);
				else
					return getValueCoded().getName(locale).getName();
			}
			else if (abbrev.equals("NM") || abbrev.equals("SN"))
				return getValueNumeric().toString();
			else if (abbrev.equals("DT") || abbrev.equals("TM") || abbrev.equals("TS"))
				return (getValueDatetime() == null ? "" : Format.format(getValueDatetime(), locale));
			else if (abbrev.equals("ST"))
				return getValueText();
		}
		
		// if the datatype is 'unknown', default to just returning what is not null
		if (getValueNumeric() != null)
			return getValueNumeric().toString();
		else if (getValueCoded() != null) {
			if (getValueDrug() != null)
				return getValueDrug().getFullName(locale);
			else
				return getValueCoded().getName(locale).getName();
		}
		else if (getValueDatetime() != null)
			return Format.format(getValueDatetime(), locale);
		else if (getValueText() != null)
			return getValueText();
		
		return "";
	}
	
	public String toString() {
		if (obsId == null)
			return "null";
		
		return "Obs #" + obsId.toString();
	}

}