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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * PersonAttributeType
 */
@Root(strict = false)
public class PersonAttributeType extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 2112313431211L;
	
	private Integer personAttributeTypeId;
	
	private String format;
	
	private Integer foreignKey;
	
	private Integer sortWeight;
	
	private Boolean searchable = false;
	
	private Privilege editPrivilege;
	
	/** default constructor */
	public PersonAttributeType() {
	}
	
	/** constructor with id */
	public PersonAttributeType(Integer PersonAttributeTypeId) {
		this.personAttributeTypeId = PersonAttributeTypeId;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getPersonAttributeTypeId() == null)
			return super.hashCode();
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
			PersonAttributeType p = (PersonAttributeType) obj;
			if (p != null)
				return (personAttributeTypeId.equals(p.getPersonAttributeTypeId()));
		}
		return false;
	}
	
	// Property accessors
	
	/**
	 * @return Returns the format.
	 */
	@Element(data = true, required = false)
	public String getFormat() {
		return format;
	}
	
	/**
	 * @param format The format to set.
	 */
	@Element(data = true, required = false)
	public void setFormat(String format) {
		this.format = format;
	}
	
	/**
	 * @return the foreignKey
	 */
	@Attribute(required = false)
	public Integer getForeignKey() {
		return foreignKey;
	}
	
	/**
	 * @param foreignKey the foreignKey to set
	 */
	@Attribute(required = false)
	public void setForeignKey(Integer foreignKey) {
		this.foreignKey = foreignKey;
	}
	
	/**
	 * @return the sortWeight
	 */
	public Integer getSortWeight() {
		return sortWeight;
	}
	
	/**
	 * @param sortWeight the formOrder to set
	 */
	public void setSortWeight(Integer sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	/**
	 * @return Returns the PersonAttributeTypeId.
	 */
	@Attribute(required = false)
	public Integer getPersonAttributeTypeId() {
		return personAttributeTypeId;
	}
	
	/**
	 * @param PersonAttributeTypeId The PersonAttributeTypeId to set.
	 */
	@Attribute(required = false)
	public void setPersonAttributeTypeId(Integer PersonAttributeTypeId) {
		this.personAttributeTypeId = PersonAttributeTypeId;
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
	@Attribute(required = false)
	public Boolean getSearchable() {
		return searchable;
	}
	
	/**
	 * @param searchable the searchable to set
	 */
	@Attribute(required = false)
	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}
	
	/**
	 * The privilege required in order to edit this attribute
	 * 
	 * @return Returns the required privilege
	 * @since 1.5
	 */
	public Privilege getEditPrivilege() {
		return editPrivilege;
	}
	
	/**
	 * The privilege required in order to edit this attribute If <code>editPrivilege</code> is null,
	 * no extra permissions are required to edit this type
	 * 
	 * @param editPrivilege
	 * @since 1.5
	 */
	public void setEditPrivilege(Privilege editPrivilege) {
		this.editPrivilege = editPrivilege;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getPersonAttributeTypeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setPersonAttributeTypeId(id);
		
	}
	
}
