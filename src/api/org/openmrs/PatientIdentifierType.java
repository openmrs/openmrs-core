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


/**
 * PatientIdentifierType
 */
public class PatientIdentifierType extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 211231L;
	
	// Fields
	
	private Integer patientIdentifierTypeId;
	
	private String format;
	
	private Boolean required = Boolean.FALSE;
	
	private String formatDescription;
	
	private Boolean checkDigit = Boolean.FALSE;
	
	private String validator;
	
	/** default constructor */
	public PatientIdentifierType() {
	}
	
	/** constructor with id */
	public PatientIdentifierType(Integer patientIdentifierTypeId) {
		this.patientIdentifierTypeId = patientIdentifierTypeId;
	}
	
	public int hashCode() {
		if (this.getPatientIdentifierTypeId() == null)
			return super.hashCode();
		return this.getPatientIdentifierTypeId().hashCode();
	}
	
	/**
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PatientIdentifierType) {
			PatientIdentifierType p = (PatientIdentifierType) obj;
			if (p != null)
				return (patientIdentifierTypeId.equals(p.getPatientIdentifierTypeId()));
		}
		return false;
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
	public Boolean getCheckDigit() {
		return hasCheckDigit();
	}
	
	/**
	 * @return Returns the checkdigit.
	 * @deprecated No need to know if it has a check digit now that any validator algorithm can be
	 *             chosen.
	 */
	public Boolean hasCheckDigit() {
		return checkDigit;
	}
	
	/**
	 * @param checkDigit The checkdigit to set.
	 * @deprecated No need for this field now that any validator algorithm can be chosen.
	 */
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
		return validator != null && !validator.equals("");
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
