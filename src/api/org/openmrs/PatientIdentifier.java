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
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;

/**
 * A <code>Patient</code> can have zero to n identifying PatientIdentifier(s).  
 * 
 * PatientIdentifiers are anything from medical record numbers, to social
 * security numbers, to driver's licenses.  The type of identifier is defined by 
 * the PatientIdentifierType.
 * 
 * A PatientIdentifier also contains a Location.
 * 
 * @see org.openmrs.PatientIdentifierType
 */
public class PatientIdentifier implements java.io.Serializable, Comparable<PatientIdentifier> {

	public static final long serialVersionUID = 1123121L;

	private static Log log = LogFactory.getLog(PatientIdentifier.class);

	// Fields

	private Patient patient;
	private String identifier;
	private PatientIdentifierType identifierType;
	private Location location;
	private User creator;
	private Date dateCreated;
	private Boolean preferred = false;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;

	/** default constructor */
	public PatientIdentifier() {
	}
	
	/**
	 * Convenience constructor for creating a basic identifier
	 * @param identifier String identifier
	 * @param type PatientIdentifierType
	 * @param location Location of the identifier
	 */
	public PatientIdentifier(String identifier, PatientIdentifierType type, Location location) {
		this.identifier = identifier;
		this.identifierType = type;
		this.location = location;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PatientIdentifier) {
			PatientIdentifier p = (PatientIdentifier)obj;
			boolean ret = true;
			if (patient != null && p.getPatient() != null)
				ret = ret && patient.equals(p.getPatient());
			if (identifier != null && p.getIdentifier() != null)
				ret = ret && identifier.equals(p.getIdentifier());
			if (identifierType != null && p.getIdentifierType() != null)
				ret = ret && identifierType.equals(p.getIdentifierType());
			// location is no longer part of the key for identifier
			//if (location != null && p.getLocation() != null)
			//	ret = ret && location.equals(p.getLocation());
			return ret;
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getPatient() == null && this.getIdentifier() == null && this.getIdentifierType() == null) return super.hashCode();
		int hash = 5;
		if (getPatient() != null)
			hash += 31 * hash + this.getPatient().hashCode();
		if (getIdentifier() != null)
			hash += 31 * hash + this.getIdentifier().hashCode();
		if (getIdentifierType() != null)
			hash += 31 * hash + this.getIdentifierType().hashCode();
		return hash;
	}
	
	/**
	 * Compares this PatientIdentifier object to the given otherIdentifier. This method
	 * differs from {@link #equals(Object)} in that this method compares the
	 * inner fields of each identifier for equality.
	 * 
	 * Note: Null/empty fields on <code>otherIdentifier</code> /will not/ cause a
	 * false value to be returned
	 * 
	 * @param otherIdentifier PatientiIdentifier with which to compare
	 * @return boolean true/false whether or not they are the same names
	 */
	@SuppressWarnings("unchecked")
    public boolean equalsContent(PatientIdentifier otherIdentifier) {
		boolean returnValue = true;

		// these are the methods to compare.
		String[] methods = { "getIdentifier", "getIdentifierType", "getLocation" };

		Class identifierClass = this.getClass();

		// loop over all of the selected methods and compare this and other
		for (String methodName : methods) {
			try {
				Method method = identifierClass.getMethod(methodName,
				                                       new Class[] {});

				Object thisValue = method.invoke(this);
				Object otherValue = method.invoke(otherIdentifier);

				if (otherValue != null)
					returnValue &= otherValue.equals(thisValue);

			} catch (NoSuchMethodException e) {
				log.warn("No such method for comparison " + methodName, e);
			} catch (IllegalAccessException e) {
				log.error("Error while comparing identifiers", e);
			} catch (InvocationTargetException e) {
				log.error("Error while comparing identifiers", e);
			}

		}

		return returnValue;
	}

	//property accessors

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
	 * @return Returns the identifier.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier The identifier to set.
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return Returns the identifierType.
	 */
	public PatientIdentifierType getIdentifierType() {
		return identifierType;
	}

	/**
	 * @param identifierType The identifierType to set.
	 */
	public void setIdentifierType(PatientIdentifierType identifierType) {
		this.identifierType = identifierType;
	}

	/**
	 * @return Returns the location.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location The location to set.
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @return Returns the patient.
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient The patient to set.
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
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
	
	public String toString() {
		return this.identifier;
	}

	/**
	 * @return Returns the preferred.
	 */
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
	 * @return the preferred status
	 */
	public Boolean isPreferred() {
		return preferred;
	}

	/**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(PatientIdentifier other) {
    	int retValue = 0;
	    if (other != null) {
	    	retValue = isVoided().compareTo(other.isVoided());
	    	if (retValue == 0)
	    		retValue = other.isPreferred().compareTo(isPreferred());
	    	if (retValue == 0)
	    		retValue = OpenmrsUtil.compareWithNullAsLatest(getDateCreated(), other.getDateCreated());
	    	if (retValue == 0)
	    		retValue = OpenmrsUtil.compareWithNullAsGreatest(getIdentifierType().getPatientIdentifierTypeId(), other.getIdentifierType().getPatientIdentifierTypeId());
	    	if (retValue == 0)
	    		retValue = OpenmrsUtil.compareWithNullAsGreatest(getIdentifier(), other.getIdentifier());
	    	
	    	// if we've gotten this far, just check all identifier values.  If they are
	    	// equal, leave the objects at 0.  If not, arbitrarily pick retValue=1 
	    	// and return that (they are not equal).
	    	if (retValue == 0 && !equalsContent(other))
	    		retValue = 1;
	    }
	    
	    return retValue;
    }
}