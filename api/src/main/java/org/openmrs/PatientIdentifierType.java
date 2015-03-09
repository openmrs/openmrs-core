/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * PatientIdentifierType
 */
public class PatientIdentifierType extends BaseOpenmrsMetadata implements Serializable {
	
	public static final long serialVersionUID = 211231L;
	
	/**
	 * Enumerates the possible ways that location may be applicable for a particular Patient
	 * Identifer Type
	 */
	public enum LocationBehavior {
		/**
		 * Indicates that location is required for the current identifier type
		 */
		REQUIRED,
		/**
		 * Indicates that location is not used for the current identifier type
		 */
		NOT_USED
	}
	
	/**
	 * Enumeration for the way to handle uniqueness among identifiers for a given identifier type
	 */
	public enum UniquenessBehavior {
		
		/**
		 * Indicates that identifiers should be globally unique
		 */
		UNIQUE,
		
		/**
		 * Indicates that duplicates identifiers are allowed
		 */
		NON_UNIQUE,
		
		/**
		 * Indicates that identifiers should be unique only across a location if the identifier's
		 * location property is not null
		 */
		LOCATION
	}
	
	// Fields	
	private Integer patientIdentifierTypeId;
	
	private String format;
	
	private Boolean required = Boolean.FALSE;
	
	private String formatDescription;
	
	private Boolean checkDigit = Boolean.FALSE;
	
	private String validator;
	
	private LocationBehavior locationBehavior;
	
	private UniquenessBehavior uniquenessBehavior;
	
	/** default constructor */
	public PatientIdentifierType() {
	}
	
	/** constructor with id */
	public PatientIdentifierType(Integer patientIdentifierTypeId) {
		this.patientIdentifierTypeId = patientIdentifierTypeId;
	}
	
	// Property accessors
	
	/**
	 * @return Returns the formatDescription.
	 */
	public String getFormatDescription() {
		return formatDescription;
	}
	
	/**
	 * @param formatDescription The formatDescription to set.
	 */
	public void setFormatDescription(String formatDescription) {
		this.formatDescription = formatDescription;
	}
	
	/**
	 * @return Returns the required.
	 */
	public Boolean getRequired() {
		return required;
	}
	
	/**
	 * @param required The required to set.
	 */
	public void setRequired(Boolean required) {
		this.required = required;
	}
	
	/**
	 * @return Returns the locationBehavior
	 */
	public LocationBehavior getLocationBehavior() {
		return locationBehavior;
	}
	
	/**
	 * @param locationBehavior The locationBehavior to set
	 */
	public void setLocationBehavior(LocationBehavior locationBehavior) {
		this.locationBehavior = locationBehavior;
	}
	
	/**
	 * @return the uniquenessBehavior
	 * @since 1.10
	 */
	public UniquenessBehavior getUniquenessBehavior() {
		return uniquenessBehavior;
	}
	
	/**
	 * @param uniquenessBehavior the uniquenessBehavior to set
	 * @since 1.10
	 */
	public void setUniquenessBehavior(UniquenessBehavior uniquenessBehavior) {
		this.uniquenessBehavior = uniquenessBehavior;
	}
	
	/**
	 * @return Returns the format.
	 */
	public String getFormat() {
		return format;
	}
	
	/**
	 * @param format The format to set.
	 */
	public void setFormat(String format) {
		this.format = format;
	}
	
	/**
	 * @return Returns the patientIdentifierTypeId.
	 */
	public Integer getPatientIdentifierTypeId() {
		return patientIdentifierTypeId;
	}
	
	/**
	 * @param patientIdentifierTypeId The patientIdentifierTypeId to set.
	 */
	public void setPatientIdentifierTypeId(Integer patientIdentifierTypeId) {
		this.patientIdentifierTypeId = patientIdentifierTypeId;
	}
	
	/**
	 * @return Returns the checkdigit.
	 * @deprecated No need to know if it has a check digit now that any validator algorithm can be
	 *             chosen.
	 */
	@Deprecated
	public Boolean getCheckDigit() {
		return hasCheckDigit();
	}
	
	/**
	 * @return Returns the checkdigit.
	 * @deprecated No need to know if it has a check digit now that any validator algorithm can be
	 *             chosen.
	 */
	@Deprecated
	public Boolean hasCheckDigit() {
		return checkDigit;
	}
	
	/**
	 * @param checkDigit The checkdigit to set.
	 * @deprecated No need for this field now that any validator algorithm can be chosen.
	 */
	@Deprecated
	public void setCheckDigit(Boolean checkDigit) {
		this.checkDigit = checkDigit;
	}
	
	public String getValidator() {
		return validator;
	}
	
	public void setValidator(String validator) {
		this.validator = validator;
	}
	
	/**
	 * @return Whether this identifier type has a validator.
	 */
	public boolean hasValidator() {
		return StringUtils.isNotEmpty(validator);
	}
	
	/**
	 * TODO: make this return a more debug-worth string instead of just the name. Check the webapp
	 * to make sure it is not depending on this
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getPatientIdentifierTypeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setPatientIdentifierTypeId(id);
		
	}
}
