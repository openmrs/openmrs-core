package org.openmrs;

import java.util.Date;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

/**
 * Field 
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class Field implements java.io.Serializable {

	public static final long serialVersionUID = 4454L;

	// Fields

	private Integer fieldId;
	private String name;
	private String description;
	private String tableName;
	private String attributeName;
	private Boolean selectMultiple;
	private Date dateCreated;
	private Date dateChanged;
	private Concept concept;
	private FieldType fieldType;
	private User creator;
	private User changedBy;
	private Set<FieldAnswer> fieldAnswers;
	private Set<FormField> formFields;
	boolean dirty;

	// Constructors

	/** default constructor */
	public Field() {
	}

	/** constructor with id */
	public Field(Integer fieldId) {
		this.fieldId = fieldId;
	}

	/** 
	 * Compares two Field objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Field))
			return false;
		
		Field field = (Field) obj;
		return (this.fieldId.equals(field.getFieldId()));
	}
	
	public int hashCode() {
		if (this.getFieldId() == null) return super.hashCode();
		return this.getFieldId().hashCode();
	}

	/**
	 * 
	 * @return boolean whether or not this field object has been modified
	 */
	public boolean isDirty() {
		if (dirty == true)
			return true;
		else {
			if (fieldAnswers != null)
				for (Iterator i = fieldAnswers.iterator(); i.hasNext();) {
					FieldAnswer fieldAnswer = (FieldAnswer)i.next();
					if (fieldAnswer.isDirty())
						return true;
				}
			if (formFields != null)
				for (Iterator i = formFields.iterator(); i.hasNext();) {
					FieldAnswer formField = (FieldAnswer)i.next();
					if (formField.isDirty())
						return true;
				}
		}
		return false;
	}
	
	public void setClean() {
		dirty = false;
	}
	
	// Property accessors
		
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
		this.dirty = true;
		this.attributeName = attributeName;
	}

	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy The changedBy to set.
	 */
	public void setChangedBy(User changedBy) {
		this.dirty = true;
		this.changedBy = changedBy;
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
		this.dirty = true;
		this.concept = concept;
	}

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
		this.dirty = true;
		this.creator = creator;
	}

	/**
	 * @return Returns the dateChanged.
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged The dateChanged to set.
	 */
	public void setDateChanged(Date dateChanged) {
		this.dirty = true;
		this.dateChanged = dateChanged;
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
		this.dirty = true;
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.dirty = true;
		this.description = description;
	}

	/**
	 * @return Returns the fieldAnswers.
	 */
	public Set<FieldAnswer> getAnswers() {
		return fieldAnswers;
	}

	/**
	 * @param fieldAnswers The fieldAnswers to set.
	 */
	public void setAnswers(Set<FieldAnswer> fieldAnswers) {
		this.dirty = true;
		this.fieldAnswers = fieldAnswers;
	}
	
	/**
	 * Adds a field answer to the list of field answers
	 * @param FieldAnswer to be added
	 */
	public void addAnswer(FieldAnswer fieldAnswer) {
		this.dirty = true;
		if (fieldAnswers == null)
			fieldAnswers = new HashSet<FieldAnswer>();
		if (!fieldAnswers.contains(fieldAnswer) && fieldAnswer != null)
			fieldAnswers.add(fieldAnswer);
	}
	
	/**
	 * Removes a field answer from the list of field answers
	 * @param FieldAnswer to be removed  
	 */
	public void removeAnswer(FieldAnswer fieldAnswer) {
		if (fieldAnswers != null) {
			this.dirty = true;
			fieldAnswers.remove(fieldAnswer);
		}
	}

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
		this.dirty = true;
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
		this.dirty = true;
		this.fieldType = fieldType;
	}

	/**
	 * @return Returns the formFields.
	 */
	public Set<FormField> getFormFields() {
		return formFields;
	}

	/**
	 * @param formFields The formFields to set.
	 */
	public void setFormFields(Set<FormField> formFields) {
		this.dirty = true;
		this.formFields = formFields;
	}
	
	/**
	 * Adds a FormField to the list of form fields
	 * @param FormField to be added
	 */
	public void addFormField(FormField formField) {
		this.dirty = true;
		if (formFields == null)
			formFields = new HashSet<FormField>();
		if (!formFields.contains(formField) && formField != null)
			this.formFields.add(formField);
	}
	
	/**
	 * Removes a FormField from the list of form fields
	 * @param FormField formField to be removed
	 */
	public void removeFormField(FormField formField) {
		if (formFields != null) {
			this.dirty = true;
			this.formFields.remove(formField);
		}
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.dirty = true;
		this.name = name;
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
		this.dirty = true;
		this.selectMultiple = selectMultiple;
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
		this.dirty = true;
		this.tableName = tableName;
	}
}