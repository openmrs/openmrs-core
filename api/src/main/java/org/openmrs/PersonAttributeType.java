/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.io.Serializable;
import java.util.Comparator;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.Field;
import org.openmrs.util.OpenmrsUtil;

/**
 * PersonAttributeType
 */
public class PersonAttributeType extends BaseChangeableOpenmrsMetadata implements java.io.Serializable, Comparable<PersonAttributeType> {
	
	public static final long serialVersionUID = 2112313431211L;
	
	private Integer personAttributeTypeId;
	
	private String format;
	
	private Integer foreignKey;
	
	private Double sortWeight;

	@Field
	private Boolean searchable = false;
	
	private Privilege editPrivilege;
	
	/** default constructor */
	public PersonAttributeType() {
	}
	
	/** constructor with id */
	public PersonAttributeType(Integer myPersonAttributeTypeId) {
		this.personAttributeTypeId = myPersonAttributeTypeId;
	}
	
	// Property accessors
	
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
	 * @return the sortWeight
	 */
	public Double getSortWeight() {
		return sortWeight;
	}
	
	/**
	 * @param sortWeight the formOrder to set
	 */
	public void setSortWeight(Double sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	/**
	 * @return Returns the PersonAttributeTypeId.
	 */
	public Integer getPersonAttributeTypeId() {
		return personAttributeTypeId;
	}
	
	/**
	 * @param newPersonAttributeTypeId The PersonAttributeTypeId to set.
	 */
	public void setPersonAttributeTypeId(Integer newPersonAttributeTypeId) {
		this.personAttributeTypeId = newPersonAttributeTypeId;
	}
	
	/**
	 * @return the searchable status
	 * 
	 * @deprecated as of 2.0, use {@link #getSearchable()}
	 */
	@Deprecated
	@JsonIgnore
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
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
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
	@Override
	public Integer getId() {
		return getPersonAttributeTypeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setPersonAttributeTypeId(id);
		
	}
	
	/**
	*
	* @deprecated since 1.12. Use DefaultComparator instead.
	* Note: this comparator imposes orderings that are inconsistent with equals.
	*/
	@Override
	@SuppressWarnings("squid:S1210")
	public int compareTo(PersonAttributeType other) {
		DefaultComparator patDefaultComparator = new DefaultComparator();
		return patDefaultComparator.compare(this, other);
	}
	
	/**
	 Provides a default comparator.
	 @since 1.12
	 **/
	public static class DefaultComparator implements Comparator<PersonAttributeType>, Serializable {

		private static final long serialVersionUID = 1L;
		
		@Override
		public int compare(PersonAttributeType pat1, PersonAttributeType pat2) {
			return OpenmrsUtil.compareWithNullAsGreatest(pat1.getPersonAttributeTypeId(), pat2.getPersonAttributeTypeId());
			
		}
	}
	
}
