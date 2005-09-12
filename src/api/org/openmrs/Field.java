package org.openmrs;

import java.util.Date;
import java.util.Set;

/**
 * Field 
 */
public class Field implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

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
	private Set fieldAnswers;
	private Set formFields;

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
	 * 
	 */
	public Integer getFieldId() {
		return this.fieldId;
	}

	public void setFieldId(Integer fieldId) {
		this.fieldId = fieldId;
	}

	/**
	 * 
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 */
	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * 
	 */
	public String getAttributeName() {
		return this.attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * 
	 */
	public Boolean getSelectMultiple() {
		return this.selectMultiple;
	}

	public void setSelectMultiple(Boolean selectMultiple) {
		this.selectMultiple = selectMultiple;
	}

	/**
	 * 
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * 
	 */
	public Date getDateChanged() {
		return this.dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * 
	 */
	public Concept getConcept() {
		return this.concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * 
	 */
	public FieldType getFieldType() {
		return this.fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	/**
	 * 
	 */
	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * 
	 */
	public User getChangedBy() {
		return this.changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * 
	 */
	public Set getFieldAnswers() {
		return this.fieldAnswers;
	}

	public void setFieldAnswers(Set fieldAnswers) {
		this.fieldAnswers = fieldAnswers;
	}

	/**
	 * 
	 */
	public Set getFormFields() {
		return this.formFields;
	}

	public void setFormFields(Set formFields) {
		this.formFields = formFields;
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Field))
			return false;
		
		Field field = (Field) obj;
		return (this.fieldId == field.getFieldId());
	}

}