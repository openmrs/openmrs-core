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
	private String creator = "";
	private String changedBy = "";
	private FieldListItem field = null;
	private Integer numForms = 0;
	
	public FieldListItem() { }
		
	public FieldListItem(Field field) {

		if (field != null) {
			fieldId = field.getFieldId();
			name = field.getName();
			description = field.getDescription();
			if (field.getFieldType() != null) {
				fieldTypeName = field.getFieldType().getName();
				fieldTypeId = field.getFieldType().getFieldTypeId();
			}
			if (field.getConcept() != null)
				concept = new ConceptListItem(field.getConcept(), new Locale("en", "US"));  //TODO fix locale here
			table = field.getTableName();
			attribute = field.getAttributeName();
			selectMultiple = field.isSelectMultiple() == true ? "yes" : "no";
			if (field.getCreator() != null)
				creator = field.getCreator().getFirstName() + " " + field.getCreator().getLastName();
			if (field.getChangedBy() != null)
				changedBy = field.getChangedBy().getFirstName() + " " + field.getChangedBy().getLastName();
			numForms = field.getForms().size();
		}
	}

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

	public FieldListItem getField() {
		return field;
	}

	public void setField(FieldListItem field) {
		this.field = field;
	}

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

}
