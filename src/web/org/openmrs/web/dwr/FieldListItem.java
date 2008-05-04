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
package org.openmrs.web.dwr;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Field;

public class FieldListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer fieldId;
	private String name = "";
	private String description = "";
	private Integer fieldTypeId;
	private String fieldTypeName = "";
	private ConceptListItem concept = null;
	private String table = "";
	private String attribute = "";
	private String selectMultiple;
	private Integer numForms = 0;
	private String defaultValue = "";
	
	//private String creator = "";
	//private String changedBy = "";
	
	public FieldListItem() { }
		
	public FieldListItem(Field field, Locale locale) {

		if (field != null) {
			fieldId = field.getFieldId();
			name = field.getName();
			description = field.getDescription();
			if (field.getFieldType() != null) {
				fieldTypeName = field.getFieldType().getName();
				fieldTypeId = field.getFieldType().getFieldTypeId();
			}
			if (field.getConcept() != null)
				concept = new ConceptListItem(field.getConcept(), locale);
			table = field.getTableName();
			attribute = field.getAttributeName();
			selectMultiple = field.isSelectMultiple() == true ? "yes" : "no";
			//if (field.getCreator() != null)
			//	creator = field.getCreator().getFirstName() + " " + field.getCreator().getLastName();
			//if (field.getChangedBy() != null)
			//	changedBy = field.getChangedBy().getFirstName() + " " + field.getChangedBy().getLastName();
			numForms = field.getForms().size();
			defaultValue = field.getDefaultValue();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FieldListItem) {
			FieldListItem f2 = (FieldListItem)obj;
			if (fieldId != null)
				return fieldId.equals(f2.getFieldId());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (fieldId != null)
			return 57 * fieldId.hashCode();
		else
			return super.hashCode();
	}

	/*
	public String getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String c) {
		this.creator = c;
	}
	*/

	public Integer getFieldId() {
		return fieldId;
	}

	public void setFieldId(Integer fieldId) {
		this.fieldId = fieldId;
	}
	
	public String getSelectMultiple() {
		return selectMultiple;
	}

	public void setSelectMultiple(String selectMultiple) {
		this.selectMultiple = selectMultiple;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public ConceptListItem getConcept() {
		return concept;
	}

	public void setConcept(ConceptListItem concept) {
		this.concept = concept;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public Integer getFieldTypeId() {
		return fieldTypeId;
	}

	public void setFieldTypeId(Integer fieldTypeId) {
		this.fieldTypeId = fieldTypeId;
	}

	public String getFieldTypeName() {
		return fieldTypeName;
	}

	public void setFieldTypeName(String fieldTypeName) {
		this.fieldTypeName = fieldTypeName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNumForms() {
		return numForms;
	}

	public void setNumForms(Integer numForms) {
		this.numForms = numForms;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

}
