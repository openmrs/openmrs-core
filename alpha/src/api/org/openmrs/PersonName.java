package org.openmrs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.util.StringUtils;

/**
 * PersonName
 */
public class PersonName implements java.io.Serializable, Cloneable, Comparable<PersonName> {

	public static final long serialVersionUID = 4353L;

	// Fields

	private Integer personNameId;
	
	private Person person;
	private Boolean preferred = false;
	
	private String prefix;
	private String givenName;
	private String middleName;
	private String familyNamePrefix;
	private String familyName;
	private String familyName2;
	private String familyNameSuffix;
	private String degree;
	
	private Date dateCreated;
	private User creator;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;
	private User changedBy;
	private Date dateChanged;

	// Constructors

	/** default constructor */
	public PersonName() {
	}

	/** constructor with id */
	public PersonName(Integer personNameId) {
		this.personNameId = personNameId;
	}
	
	/**
	 * Constructor with the basic requirements
	 * @param givenName
	 * @param middleName
	 * @param familyName
	 */
	public PersonName(String givenName, String middleName, String familyName) {
		this.givenName  = givenName;
		this.middleName = middleName;
		this.familyName = familyName;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PersonName) {
			PersonName pname = (PersonName) obj;
			if (this.personNameId != null && pname.getPersonNameId() != null)
				return (this.personNameId.equals(pname.getPersonNameId())); 
			else {
				return (person.equals(pname.getPerson()) &&
						givenName.equals(pname.getGivenName()) &&
						middleName.equals(pname.getMiddleName()) &&
						familyName.equals(pname.getFamilyName()));
			}
				
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getPersonNameId() == null) return super.hashCode();
		return this.getPersonNameId().hashCode();
	}
	
	/**
	 * bitwise copy of the personName object.  
	 * NOTICE: THIS WILL NOT COPY THE PATIENT OBJECT.  The PersonName.person object in
	 * this object AND the cloned object will point at the same person
	 * @return New PersonName object
	 */
	public static PersonName newInstance(PersonName pn) {
		PersonName newName = new PersonName(new Integer(pn.getPersonNameId()));
		if (pn.getGivenName() != null)
			newName.setGivenName(new String(pn.getGivenName()));
		if (pn.getMiddleName() != null)
			newName.setMiddleName(new String(pn.getMiddleName()));
		if (pn.getFamilyName() != null)
			newName.setFamilyName(new String(pn.getFamilyName()));
		if (pn.getFamilyName2() != null)
			newName.setFamilyName2(new String(pn.getFamilyName2()));
		if (pn.getFamilyNamePrefix() != null)
			newName.setFamilyNamePrefix(new String(pn.getFamilyNamePrefix()));
		if (pn.getFamilyNameSuffix() != null)
			newName.setFamilyNameSuffix(new String(pn.getFamilyNameSuffix()));
		if (pn.getPrefix() != null)
			newName.setPrefix(new String(pn.getPrefix()));
		if (pn.getDegree() != null)
			newName.setDegree(new String(pn.getDegree()));
		if (pn.getVoidReason() != null)
			newName.setVoidReason(new String(pn.getVoidReason()));
		
		if (pn.getDateChanged() != null)
			newName.setDateChanged((Date)pn.getDateChanged().clone());
		if (pn.getDateCreated() != null)
			newName.setDateCreated((Date)pn.getDateCreated().clone());
		if (pn.getDateVoided() != null)
			newName.setDateVoided((Date)pn.getDateVoided().clone());

		if (pn.getPreferred() != null)
			newName.setPreferred(new Boolean(pn.getPreferred().booleanValue()));
		if (pn.getVoided() != null)
			newName.setVoided(new Boolean(pn.getVoided().booleanValue()));

		newName.setPerson(pn.getPerson());
		newName.setVoidedBy(pn.getVoidedBy());
		newName.setChangedBy(pn.getChangedBy());
		newName.setCreator(pn.getCreator());
    	
    	return newName;
	}
	
	// Property accessors
	
	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
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
	 * @param dateCreated The dateCreated to set.
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
	 * @param dateVoided The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * @return Returns the degree.
	 */
	public String getDegree() {
		return degree;
	}

	/**
	 * @param degree The degree to set.
	 */
	public void setDegree(String degree) {
		this.degree = degree;
	}

	/**
	 * @return Returns the familyName.
	 */
	public String getFamilyName() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return OpenmrsConstants.OBSCURE_PATIENTS_FAMILY_NAME;
		return familyName;
	}

	/**
	 * @param familyName The familyName to set.
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/**
	 * @return Returns the familyName2.
	 */
	public String getFamilyName2() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return null;
		return familyName2;
	}

	/**
	 * @param familyName2 The familyName2 to set.
	 */
	public void setFamilyName2(String familyName2) {
		this.familyName2 = familyName2;
	}

	/**
	 * @return Returns the familyNamePrefix.
	 */
	public String getFamilyNamePrefix() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return null;
		return familyNamePrefix;
	}

	/**
	 * @param familyNamePrefix The familyNamePrefix to set.
	 */
	public void setFamilyNamePrefix(String familyNamePrefix) {
		this.familyNamePrefix = familyNamePrefix;
	}

	/**
	 * @return Returns the familyNameSuffix.
	 */
	public String getFamilyNameSuffix() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return null;
		return familyNameSuffix;
	}

	/**
	 * @param familyNameSuffix The familyNameSuffix to set.
	 */
	public void setFamilyNameSuffix(String familyNameSuffix) {
		this.familyNameSuffix = familyNameSuffix;
	}

	/**
	 * @return Returns the givenName.
	 */
	public String getGivenName() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return OpenmrsConstants.OBSCURE_PATIENTS_GIVEN_NAME;
		return givenName;
	}

	/**
	 * @param givenName The givenName to set.
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/**
	 * @return Returns the middleName.
	 */
	public String getMiddleName() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return OpenmrsConstants.OBSCURE_PATIENTS_MIDDLE_NAME;
		return middleName;
	}

	/**
	 * @param middleName The middleName to set.
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
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

	/**
	 * @return Returns the personNameId.
	 */
	public Integer getPersonNameId() {
		return personNameId;
	}

	/**
	 * @param personNameId The personNameId to set.
	 */
	public void setPersonNameId(Integer personNameId) {
		this.personNameId = personNameId;
	}

	/**
	 * @return Returns the preferred.
	 */
	public Boolean isPreferred() {
		if (preferred == null)
			return new Boolean(false);
		return preferred;
	}
	
	public Boolean getPreferred() {
		return isPreferred();
	}

	/**
	 * @param preferred The preferred to set.
	 */
	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}

	/**
	 * @return Returns the prefix.
	 */
	public String getPrefix() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return null;
		return prefix;
	}

	/**
	 * @param prefix The prefix to set.
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean isVoided() {
		return voided;
	}
	
	public Boolean getVoided() {
		return isVoided();
	}

	/**
	 * @param voided The voided to set.
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
	 * @param voidedBy The voidedBy to set.
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
	 * @param voidReason The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy The changedBy to set.
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return Returns the dateChanged.
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged The dateChanged to set.
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	public String toString() {
		List<String> temp = new ArrayList<String>();
		if (getPrefix() != null) temp.add(getPrefix());
		if (getGivenName() != null) temp.add(getGivenName());
		if (getMiddleName() != null) temp.add(getMiddleName());
		if (getFamilyNamePrefix() != null) temp.add(getFamilyNamePrefix());
		if (getFamilyName() != null) temp.add(getFamilyName());
		if (getFamilyName2() != null) temp.add(getFamilyName2());
		if (getFamilyNameSuffix() != null) temp.add(getFamilyNameSuffix());
		if (getDegree() != null) temp.add(getDegree());
		
		String nameString = StringUtils.collectionToDelimitedString(temp, " ");
		
		return nameString.trim();
	}
	
	// TODO: the behavior of this method needs to be controlled by some sort of global property 
	public int compareTo(PersonName other) {
		int ret = OpenmrsUtil.compareWithNullAsGreatest(getFamilyName(), other.getFamilyName());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(getFamilyName2(), other.getFamilyName2());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(getGivenName(), other.getGivenName());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(getMiddleName(), other.getMiddleName());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(getFamilyNamePrefix(), other.getFamilyNamePrefix());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(getFamilyNameSuffix(), other.getFamilyNameSuffix());
		return ret;
	}
}