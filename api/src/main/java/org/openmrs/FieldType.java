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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * FieldType
 */
@Entity
@Table(name = "field_type")
@Audited
@AttributeOverrides(value = {
	@AttributeOverride(name = "name", column = @Column(name = "name", length = 50, nullable = false)),
	@AttributeOverride(name = "retired", column = @Column(name = "retired", columnDefinition = "boolean default false"))
})
public class FieldType extends BaseChangeableOpenmrsMetadata {

	/**
	 * The constant serialVersionUID.
	 */
	public static final long serialVersionUID = 35467L;
	
	// Fields

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "field_type_id_seq")
	@GenericGenerator(
		name = "field_type_id_seq",
		strategy = "native",
		parameters = @Parameter(name = "sequence", value = "field_type_id_field_type_id_seq")
	)
	@Column(name = "field_type_id", nullable = false)
	private Integer fieldTypeId;
	
	@Column(name = "is_set", length = 1, nullable = false)
	private Boolean isSet = false;
	
	// Constructors
	
	/** default constructor */
	public FieldType() {
	}
	
	/** constructor with id */
	public FieldType(Integer fieldTypeId) {
		this.fieldTypeId = fieldTypeId;
	}
	
	// Property accessors
	
	/**
	 * @return Returns the fieldTypeId.
	 */
	public Integer getFieldTypeId() {
		return fieldTypeId;
	}
	
	/**
	 * @param fieldTypeId The fieldTypeId to set.
	 */
	public void setFieldTypeId(Integer fieldTypeId) {
		this.fieldTypeId = fieldTypeId;
	}
	
	/**
	 * @return Returns the isSet.
	 */
	public Boolean getIsSet() {
		return isSet;
	}
	
	/**
	 * @param isSet The isSet to set.
	 */
	public void setIsSet(Boolean isSet) {
		this.isSet = isSet;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		
		return getFieldTypeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setFieldTypeId(id);
		
	}
	
}
