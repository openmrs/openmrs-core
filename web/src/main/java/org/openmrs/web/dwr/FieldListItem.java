/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptName;
import org.openmrs.Field;
import org.openmrs.api.context.Context;

/**
 * A mini/simplified Field object. Used as the return object from DWR methods to allow javascript
 * and other consumers to easily use all methods. This class guarantees that all objects in this
 * class will be initialized (copied) off of the Person object.
 *
 * @see Field
 * @see DWRFormService
 */
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
	
	public FieldListItem() {
	}
	
	public FieldListItem(Field field, Locale locale) {
		
		if (field != null) {
			fieldId = field.getFieldId();
			name = field.getName();
			description = field.getDescription();
			if (field.getFieldType() != null) {
				fieldTypeName = field.getFieldType().getName();
				fieldTypeId = field.getFieldType().getFieldTypeId();
			}
			if (field.getConcept() != null) {
				ConceptName cn = field.getConcept().getName(locale);
				concept = new ConceptListItem(field.getConcept(), cn, locale);
			}
			table = field.getTableName();
			attribute = field.getAttributeName();
			selectMultiple = field.isSelectMultiple() ? "yes" : "no";
			//if (field.getCreator() != null)
			//	creator = field.getCreator().getFirstName() + " " + field.getCreator().getLastName();
			//if (field.getChangedBy() != null)
			//	changedBy = field.getChangedBy().getFirstName() + " " + field.getChangedBy().getLastName();
			List<Field> fields = new Vector<Field>();
			fields.add(field);
			numForms = Context.getFormService().getForms(null, null, null, null, null, null, fields).size();
			defaultValue = field.getDefaultValue();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FieldListItem) {
			FieldListItem f2 = (FieldListItem) obj;
			if (fieldId != null) {
				return fieldId.equals(f2.getFieldId());
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (fieldId != null) {
			return 57 * fieldId.hashCode();
		} else {
			return super.hashCode();
		}
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
