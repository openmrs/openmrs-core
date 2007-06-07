package org.openmrs;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Field
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class Field implements java.io.Serializable {

	public static final long serialVersionUID = 4454L;

	// Fields

	private Integer fieldId;
	private String name;
	private String description;
	private FieldType fieldType;
	private Concept concept;
	private String tableName;
	private String attributeName;
	private String defaultValue;
	private Boolean selectMultiple = false;
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private Set<FieldAnswer> fieldAnswers;
	private Set<Form> forms;

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
		if (obj == null || !(obj instanceof Field) || fieldId == null)
			return false;
		
		Field field = (Field) obj;
		return (this.fieldId.equals(field.getFieldId()));
	}

	public int hashCode() {
		if (this.getFieldId() == null)
			return super.hashCode();
		return this.getFieldId().hashCode();
	}

	// Property accessors

	/**
	 * @return Returns the fieldId.
	 */
	public Integer getFieldId() {
		return fieldId;
	}

	/**
	 * @param fieldId
	 *            The fieldId to set.
	 */
	public void setFieldId(Integer fieldId) {
		this.fieldId = fieldId;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the fieldType.
	 */
	public FieldType getFieldType() {
		return fieldType;
	}

	/**
	 * @param fieldType
	 *            The fieldType to set.
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
	 * @param concept
	 *            The concept to set.
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
	 * @param tableName
	 *            The tableName to set.
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
	 * @param attributeName
	 *            The attributeName to set.
	 */
	public void setAttributeName(String attributeName) {
		// this.dirty = true;
		this.attributeName = attributeName;
	}
	
	/**
	 * @return Returns the default value.
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * @param defaultValue
	 * 			The defaultValue to set.
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Boolean isSelectMultiple() {
		return selectMultiple;
	}
	/**
	 * @return Returns the selectMultiple.
	 */
	public Boolean getSelectMultiple() {
		return isSelectMultiple();
	}

	/**
	 * @param selectMultiple
	 *            The selectMultiple to set.
	 */
	public void setSelectMultiple(Boolean selectMultiple) {
		this.selectMultiple = selectMultiple;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
	 */
	public void setCreator(User creator) {

		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated
	 *            The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy
	 *            The changedBy to set.
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return Returns the dateChanged.
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged
	 *            The dateChanged to set.
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * @return Returns the fieldAnswers.
	 */
	public Set<FieldAnswer> getAnswers() {
		return fieldAnswers;
	}

	/**
	 * @param fieldAnswers
	 *            The fieldAnswers to set.
	 */
	public void setAnswers(Set<FieldAnswer> fieldAnswers) {
		this.fieldAnswers = fieldAnswers;
	}

	/**
	 * Adds a field answer to the list of field answers
	 * 
	 * @param FieldAnswer
	 *            to be added
	 */
	public void addAnswer(FieldAnswer fieldAnswer) {
		if (fieldAnswers == null)
			fieldAnswers = new HashSet<FieldAnswer>();
		if (!fieldAnswers.contains(fieldAnswer) && fieldAnswer != null)
			fieldAnswers.add(fieldAnswer);
	}

	/**
	 * Removes a field answer from the list of field answers
	 * 
	 * @param FieldAnswer
	 *            to be removed
	 */
	public void removeAnswer(FieldAnswer fieldAnswer) {
		if (fieldAnswers != null) {
			fieldAnswers.remove(fieldAnswer);
		}
	}

	public Set<Form> getForms() {
		return forms;
	}

	public void setForms(Set<Form> forms) {
		this.forms = forms;
	}
}