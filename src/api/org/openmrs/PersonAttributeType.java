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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * PersonAttributeType 
 */
@Root(strict=false)
public class PersonAttributeType implements java.io.Serializable {

	public static final long serialVersionUID = 2112313431211L;

	private Integer personAttributeTypeId;
	private String name;
	private String format;
	private Integer foreignKey;
	private String description;
	
	private Boolean searchable = false;
	
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
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
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
	@Element(required=true)
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	@Element(required=true)
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	@Element(required=true)
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	@Element(required=true)
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the description.
	 */
	@Element(data=true,required=false)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	@Element(data=true,required=false)
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the name.
	 */
	@Element(data=true,required=false)
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	@Element(data=true,required=false)
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the format.
	 */
	@Element(data=true,required=false)
	public String getFormat() {
		return format;
	}

	/**
	 * @param format The format to set.
	 */
	@Element(data=true,required=false)
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the foreignKey
	 */
	@Attribute(required=false)
	public Integer getForeignKey() {
		return foreignKey;
	}

	/**
	 * @param foreignKey the foreignKey to set
	 */
	@Attribute(required=false)
	public void setForeignKey(Integer foreignKey) {
		this.foreignKey = foreignKey;
	}

	/**
	 * @return Returns the PersonAttributeTypeId.
	 */
	@Attribute(required=false)
	public Integer getPersonAttributeTypeId() {
		return personAttributeTypeId;
	}

	/**
	 * @param PersonAttributeTypeId The PersonAttributeTypeId to set.
	 */
	@Attribute(required=false)
	public void setPersonAttributeTypeId(Integer PersonAttributeTypeId) {
		this.personAttributeTypeId = PersonAttributeTypeId;
	}
	
	/**
	 * @return the changedBy
	 */
	@Element(required=false)
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy the changedBy to set
	 */
	@Element(required=false)
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return the dateChanged
	 */
	@Element(required=false)
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged the dateChanged to set
	 */
	@Element(required=false)
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
	@Attribute(required=false)
	public Boolean getSearchable() {
		return searchable;
	}

	/**
	 * @param searchable the searchable to set
	 */
	@Attribute(required=false)
	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.name;
	}
	
}