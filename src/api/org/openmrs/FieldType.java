package org.openmrs;

import java.util.Date;

/**
 * FieldType 
 */
public class FieldType implements java.io.Serializable {

	public static final long serialVersionUID = 35467L;

	// Fields

	private Integer fieldTypeId;
	private String name;
	private String description;
	private Boolean isSet = false;
	private Date dateCreated;
	private User creator;

	// Constructors

	/** default constructor */
	public FieldType() {
	}

	/** constructor with id */
	public FieldType(Integer fieldTypeId) {
		this.fieldTypeId = fieldTypeId;
	}
	
	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof FieldType) {
			FieldType f = (FieldType)obj;
			return (fieldTypeId.equals(f.getFieldTypeId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getFieldTypeId() == null) return super.hashCode();
		return this.getFieldTypeId().hashCode();
	}

	// Property accessors

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
		this.creator = creator;
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
		this.description = description;
	}

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
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	

}