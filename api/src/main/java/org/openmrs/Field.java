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

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Field
 *
 * @version 1.0
 */
public class Field extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 4454L;
	
	// Fields
	
	private Integer fieldId;
	
	private FieldType fieldType;
	
	private Concept concept;
	
	private String tableName;
	
	private String attributeName;
	
	private String defaultValue;
	
	private Boolean selectMultiple = false;
	
	private Set<FieldAnswer> answers;
	
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
	 * @return Returns the fieldAnswers.
	 */
	public Set<FieldAnswer> getAnswers() {
		return answers;
	}
	
	/**
	 * @param fieldAnswers The fieldAnswers to set.
	 */
	public void setAnswers(Set<FieldAnswer> fieldAnswers) {
		this.answers = fieldAnswers;
	}
	
	/**
	 * Adds a field answer to the list of field answers
	 *
	 * @param fieldAnswer FieldAnswer to be added
	 */
	public void addAnswer(FieldAnswer fieldAnswer) {
		if (answers == null) {
			answers = new HashSet<>();
		}
		if (!answers.contains(fieldAnswer) && fieldAnswer != null) {
			answers.add(fieldAnswer);
		}
	}
	
	/**
	 * Removes a field answer from the list of field answers
	 *
	 * @param fieldAnswer FieldAnswer to be removed
	 */
	public void removeAnswer(FieldAnswer fieldAnswer) {
		if (answers != null) {
			answers.remove(fieldAnswer);
		}
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
