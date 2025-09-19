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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.envers.Audited;

/**
 * Field
 *
 * @version 1.0
 */
@Audited
@Entity
@Table(name = "field")
public class Field extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 4454L;
	
	// Fields
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "field_id")
	private Integer fieldId;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "field_type", nullable = false)
	private FieldType fieldType;
	
	@ManyToOne
	@JoinColumn(name = "concept_id")
	private Concept concept;
	
	@Column(name = "table_name", length = 50)
	private String tableName;
	
	@Column(name = "attribute_name", length = 50)
	private String attributeName;
	
	@Column(name = "default_value", length = 65535)
	private String defaultValue;
	
	@Column(name = "select_multiple", nullable = false)
	private Boolean selectMultiple = false;
	
	// Constructors
	
	/** default constructor */
	public Field() {
	}
	
	/** constructor with id */
	public Field(Integer fieldId) {
		this.fieldId = fieldId;
	}
	
	// Property accessors
	
	/**
	 * @return Returns the fieldId.
	 */
	public Integer getFieldId() {
		return fieldId;
	}
	
	/**
	 * @param fieldId The fieldId to set.
	 */
	public void setFieldId(Integer fieldId) {
		this.fieldId = fieldId;
	}
	
	/**
	 * @return Returns the fieldType.
	 */
	public FieldType getFieldType() {
		return fieldType;
	}
	
	/**
	 * @param fieldType The fieldType to set.
	 */
	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}
	
	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * @return Returns the tableName.
	 */
	public String getTableName() {
		return tableName;
	}
	
	/**
	 * @param tableName The tableName to set.
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	/**
	 * @return Returns the attributeName.
	 */
	public String getAttributeName() {
		return attributeName;
	}
	
	/**
	 * @param attributeName The attributeName to set.
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	/**
	 * @return Returns the default value.
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * @param defaultValue The defaultValue to set.
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	/**
	 * @deprecated as of 2.0, use {@link #getSelectMultiple()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isSelectMultiple() {
		return getSelectMultiple();
	}
	
	/**
	 * @return Returns the selectMultiple.
	 */
	public Boolean getSelectMultiple() {
		return selectMultiple;
	}
	
	/**
	 * @param selectMultiple The selectMultiple to set.
	 */
	public void setSelectMultiple(Boolean selectMultiple) {
		this.selectMultiple = selectMultiple;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		
		return getFieldId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setFieldId(id);
		
	}
	
}
