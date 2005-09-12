package org.openmrs;

import java.util.Date;

/**
 * FieldType 
 */
public class FieldType implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer fieldTypeId;
	private String name;
	private String description;
	private Boolean isSet;
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

	// Property accessors

	/**
	 * 
	 */
	public Integer getFieldTypeId() {
		return this.fieldTypeId;
	}

	public void setFieldTypeId(Integer fieldTypeId) {
		this.fieldTypeId = fieldTypeId;
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
	public Boolean getIsSet() {
		return this.isSet;
	}

	public void setIsSet(Boolean isSet) {
		this.isSet = isSet;
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
	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

}