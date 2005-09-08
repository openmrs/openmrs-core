package org.openmrs;

import java.util.Date;
import java.util.Set;

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
	private Set fields;
	private User user;

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
	public Set getFields() {
		return this.fields;
	}

	public void setFields(Set fields) {
		this.fields = fields;
	}

	/**
	 * 
	 */
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}