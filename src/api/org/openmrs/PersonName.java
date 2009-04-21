/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.springframework.util.StringUtils;

/**
 * A Person can have zero to n PersonName(s).
 */
@Root(strict = false)
public class PersonName extends BaseOpenmrsMetadata implements java.io.Serializable, Cloneable, Comparable<PersonName> {
	
	public static final long serialVersionUID = 4353L;
	
	private static Log log = LogFactory.getLog(PersonName.class);
	
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
	
	private Boolean voided = false;
	
	private User voidedBy;
	
	private Date dateVoided;
	
	private String voidReason;
	
	// Constructors
	
	/** default constructor */
	public PersonName() {
	}
	
	/** constructor with id */
	public PersonName(Integer personNameId) {
		this.personNameId = personNameId;
	}
	
	/**
	 * Convenience constructor with the basic requirements
	 * 
	 * @param givenName String this person's first name
	 * @param middleName String this person's middle name
	 * @param familyName String this person's last name
	 */
	public PersonName(String givenName, String middleName, String familyName) {
		this.givenName = givenName;
		this.middleName = middleName;
		this.familyName = familyName;
	}
	
	/**
	 * Compares two objects for similarity
	 * 
	 * @param obj PersonName to compare to
	 * @return boolean true/false whether or not they are the same objects
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @should not fail if either has a null person property
	 * @should return false if this has a missing person property
	 * @should return false if obj has a missing person property
	 * @should return true if properties are equal and have null person
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PersonName) {
			PersonName pname = (PersonName) obj;
			if (this.personNameId != null && pname.getPersonNameId() != null)
				return (this.personNameId.equals(pname.getPersonNameId()));
			else {
				return (OpenmrsUtil.nullSafeEquals(getPerson(), pname.getPerson())
				        && OpenmrsUtil.nullSafeEquals(getGivenName(), pname.getGivenName())
				        && OpenmrsUtil.nullSafeEquals(getMiddleName(), pname.getMiddleName()) && OpenmrsUtil.nullSafeEquals(
				    getFamilyName(), pname.getFamilyName()));
			}
			
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getPersonNameId() == null)
			return super.hashCode();
		return this.getPersonNameId().hashCode();
	}
	
	/**
	 * Compares this PersonName object to the given otherName. This method differs from
	 * {@link #equals(Object)} in that this method compares the inner fields of each name for
	 * equality. Note: Null/empty fields on <code>otherName</code> /will not/ cause a false value to
	 * be returned
	 * 
	 * @param otherName PersonName with which to compare
	 * @return boolean true/false whether or not they are the same names
	 */
	@SuppressWarnings("unchecked")
	public boolean equalsContent(PersonName otherName) {
		boolean returnValue = true;
		
		// these are the methods to compare. All are expected to be Strings
		String[] methods = { "getGivenName", "getMiddleName", "getFamilyName" };
		
		Class nameClass = this.getClass();
		
		// loop over all of the selected methods and compare this and other
		for (String methodName : methods) {
			try {
				Method method = nameClass.getMethod(methodName, new Class[] {});
				
				String thisValue = (String) method.invoke(this);
				String otherValue = (String) method.invoke(otherName);
				
				if (otherValue != null && otherValue.length() > 0)
					returnValue &= otherValue.equals(thisValue);
				
			}
			catch (NoSuchMethodException e) {
				log.warn("No such method for comparison " + methodName, e);
			}
			catch (IllegalAccessException e) {
				log.error("Error while comparing names", e);
			}
			catch (InvocationTargetException e) {
				log.error("Error while comparing names", e);
			}
			
		}
		
		return returnValue;
	}
	
	/**
	 * bitwise copy of the personName object. NOTICE: THIS WILL NOT COPY THE PATIENT OBJECT. The
	 * PersonName.person object in this object AND the cloned object will point at the same person
	 * 
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
			newName.setDateChanged((Date) pn.getDateChanged().clone());
		if (pn.getDateCreated() != null)
			newName.setDateCreated((Date) pn.getDateCreated().clone());
		if (pn.getDateVoided() != null)
			newName.setDateVoided((Date) pn.getDateVoided().clone());
		
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
	 * @return Returns the dateVoided.
	 */
	@Element(required = false)
	public Date getDateVoided() {
		return dateVoided;
	}
	
	/**
	 * @param dateVoided The dateVoided to set.
	 */
	@Element(required = false)
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	/**
	 * @return Returns the degree.
	 */
	@Element(data = true, required = false)
	public String getDegree() {
		return degree;
	}
	
	/**
	 * @param degree The degree to set.
	 */
	@Element(data = true, required = false)
	public void setDegree(String degree) {
		this.degree = degree;
	}
	
	/**
	 * @return Returns the familyName.
	 */
	@Element(data = true, required = false)
	public String getFamilyName() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return OpenmrsConstants.OBSCURE_PATIENTS_FAMILY_NAME;
		return familyName;
	}
	
	/**
	 * @param familyName The familyName to set.
	 */
	@Element(data = true, required = false)
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	/**
	 * @return Returns the familyName2.
	 */
	@Element(data = true, required = false)
	public String getFamilyName2() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return null;
		return familyName2;
	}
	
	/**
	 * @param familyName2 The familyName2 to set.
	 */
	@Element(data = true, required = false)
	public void setFamilyName2(String familyName2) {
		this.familyName2 = familyName2;
	}
	
	/**
	 * @return Returns the familyNamePrefix.
	 */
	@Element(data = true, required = false)
	public String getFamilyNamePrefix() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return null;
		return familyNamePrefix;
	}
	
	/**
	 * @param familyNamePrefix The familyNamePrefix to set.
	 */
	@Element(data = true, required = false)
	public void setFamilyNamePrefix(String familyNamePrefix) {
		this.familyNamePrefix = familyNamePrefix;
	}
	
	/**
	 * @return Returns the familyNameSuffix.
	 */
	@Element(data = true, required = false)
	public String getFamilyNameSuffix() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return null;
		return familyNameSuffix;
	}
	
	/**
	 * @param familyNameSuffix The familyNameSuffix to set.
	 */
	@Element(data = true, required = false)
	public void setFamilyNameSuffix(String familyNameSuffix) {
		this.familyNameSuffix = familyNameSuffix;
	}
	
	/**
	 * @return Returns the givenName.
	 */
	@Element(data = true, required = false)
	public String getGivenName() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return OpenmrsConstants.OBSCURE_PATIENTS_GIVEN_NAME;
		return givenName;
	}
	
	/**
	 * @param givenName The givenName to set.
	 */
	@Element(data = true, required = false)
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	/**
	 * @return Returns the middleName.
	 */
	@Element(data = true, required = false)
	public String getMiddleName() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return OpenmrsConstants.OBSCURE_PATIENTS_MIDDLE_NAME;
		return middleName;
	}
	
	/**
	 * @param middleName The middleName to set.
	 */
	@Element(data = true, required = false)
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	
	/**
	 * @return Returns the person.
	 */
	@Element(required = true)
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @param person The person to set.
	 */
	@Element(required = true)
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * @return Returns the personNameId.
	 */
	@Attribute(required = true)
	public Integer getPersonNameId() {
		return personNameId;
	}
	
	/**
	 * @param personNameId The personNameId to set.
	 */
	@Attribute(required = true)
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
	
	@Attribute(required = true)
	public Boolean getPreferred() {
		return isPreferred();
	}
	
	/**
	 * @param preferred The preferred to set.
	 */
	@Attribute(required = true)
	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}
	
	/**
	 * @return Returns the prefix.
	 */
	@Element(data = true, required = false)
	public String getPrefix() {
		if (OpenmrsConstants.OBSCURE_PATIENTS)
			return null;
		return prefix;
	}
	
	/**
	 * @param prefix The prefix to set.
	 */
	@Element(data = true, required = false)
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	/**
	 * @return Returns the voided.
	 */
	public Boolean isVoided() {
		return voided;
	}
	
	/**
	 * @see #isVoided()
	 */
	@Attribute(required = true)
	public Boolean getVoided() {
		return isVoided();
	}
	
	/**
	 * @param voided The voided to set.
	 */
	@Attribute(required = true)
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	/**
	 * @return Returns the voidedBy.
	 */
	@Element(required = false)
	public User getVoidedBy() {
		return voidedBy;
	}
	
	/**
	 * @param voidedBy The voidedBy to set.
	 */
	@Element(required = false)
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}
	
	/**
	 * @return Returns the voidReason.
	 */
	@Element(data = true, required = false)
	public String getVoidReason() {
		return voidReason;
	}
	
	/**
	 * @param voidReason The voidReason to set.
	 */
	@Element(data = true, required = false)
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		List<String> temp = new ArrayList<String>();
		if (getPrefix() != null)
			temp.add(getPrefix());
		if (getGivenName() != null)
			temp.add(getGivenName());
		if (getMiddleName() != null)
			temp.add(getMiddleName());
		if (getFamilyNamePrefix() != null)
			temp.add(getFamilyNamePrefix());
		if (getFamilyName() != null)
			temp.add(getFamilyName());
		if (getFamilyName2() != null)
			temp.add(getFamilyName2());
		if (getFamilyNameSuffix() != null)
			temp.add(getFamilyNameSuffix());
		if (getDegree() != null)
			temp.add(getDegree());
		
		String nameString = StringUtils.collectionToDelimitedString(temp, " ");
		
		return nameString.trim();
	}
	
	/**
	 * TODO: the behavior of this method needs to be controlled by some sort of global property
	 * because an implementation can define how they want their names to look (which fields to
	 * show/hide)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(PersonName other) {
		int ret = isVoided().compareTo(other.isVoided());
		if (ret == 0)
			ret = other.isPreferred().compareTo(isPreferred());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(getFamilyName(), other.getFamilyName());
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
		if (ret == 0 && getDateCreated() != null)
			ret = OpenmrsUtil.compareWithNullAsLatest(getDateCreated(), other.getDateCreated());
		
		// if we've gotten this far, just check all name values. If they are
		// equal, leave the objects at 0. If not, arbitrarily pick retValue=1
		// and return that (they are not equal).
		if (ret == 0 && !equalsContent(other))
			ret = 1;
		
		return ret;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getPersonNameId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setPersonNameId(id);
		
	}
}
