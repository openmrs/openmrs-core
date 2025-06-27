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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

/**
 * PatientIdentifierType
 */
@Entity
@Table(name = "patient_identifier_type")
@Audited
@AttributeOverrides({
	@AttributeOverride(name = "name", column = @Column(name = "name", nullable = false, length = 50)),
	@AttributeOverride(name = "description", column = @Column(name = "description", length = 65535))
})
public class PatientIdentifierType extends BaseChangeableOpenmrsMetadata {
	
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
	@DocumentId
	@Id
	@GeneratedValue(generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@Column(name = "patient_identifier_type_id")
	private Integer patientIdentifierTypeId;

	@Column(name = "format")
	private String format;

	@GenericField
	@Column(name = "required", nullable = false)
	private Boolean required = Boolean.FALSE;

	@Column(name = "format_description", length = 250)
	private String formatDescription;

	@Column(name = "validator", length = 200)
	private String validator;

	@Enumerated(EnumType.STRING)
	@Column(name = "location_behavior", length = 50)
	private LocationBehavior locationBehavior;

	@Enumerated(EnumType.STRING)
	@Column(name = "uniqueness_behavior", length = 50)
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getPatientIdentifierTypeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setPatientIdentifierTypeId(id);
		
	}
}
