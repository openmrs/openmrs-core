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
package org.openmrs;

import java.util.Date;

/**
 * PersonAttributeType 
 */
public class PersonAttributeType implements java.io.Serializable {

	public static final long serialVersionUID = 2112313431211L;

	private Integer personAttributeTypeId;
	private String name;
	private String format;
	private Integer foreignKey;
	private String description;
	
	private Boolean searchable;
	
	private User creator;
	private Date dateCreated;
	
	private User changedBy;
	private Date dateChanged;

	/** default constructor */
	public PersonAttributeType() {}

	/** constructor with id */
	public PersonAttributeType(Integer PersonAttributeTypeId) {
		this.personAttributeTypeId = PersonAttributeTypeId;
	}
	
	public int hashCode() {
		if (this.getPersonAttributeTypeId() == null) return super.hashCode();
		return 7 * this.getPersonAttributeTypeId().hashCode();
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PersonAttributeType) {
			PersonAttributeType p = (PersonAttributeType)obj;
			if (p != null)
				return (personAttributeTypeId.equals(p.getPersonAttributeTypeId()));
		}
		return false;
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

	/**
	 * @return Returns the format.
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format The format to set.
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the foreignKey
	 */
	public Integer getForeignKey() {
		return foreignKey;
	}

	/**
	 * @param foreignKey the foreignKey to set
	 */
	public void setForeignKey(Integer foreignKey) {
		this.foreignKey = foreignKey;
	}

	/**
	 * @return Returns the PersonAttributeTypeId.
	 */
	public Integer getPersonAttributeTypeId() {
		return personAttributeTypeId;
	}

	/**
	 * @param PersonAttributeTypeId The PersonAttributeTypeId to set.
	 */
	public void setPersonAttributeTypeId(Integer PersonAttributeTypeId) {
		this.personAttributeTypeId = PersonAttributeTypeId;
	}
	
	/**
	 * @return the changedBy
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy the changedBy to set
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return the dateChanged
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged the dateChanged to set
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @return the searchable status
	 */
	public Boolean isSearchable() {
		return getSearchable();
	}
	
	/**
	 * @return the searchable status
	 */
	public Boolean getSearchable() {
		return searchable;
	}

	/**
	 * @param searchable the searchable to set
	 */
	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}

	public String toString() {
		return this.name;
	}
	
}