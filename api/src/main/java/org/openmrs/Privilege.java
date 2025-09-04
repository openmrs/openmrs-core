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

import jakarta.persistence.Cacheable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

/**
 * Privilege
 * 
 * @version 1.0
 */
@Entity
@Table(name = "privilege")
@Audited
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Privilege extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 312L;
	
	// Fields
	@Id
	@Column(name = "privilege", length = 250)
	private String privilege;
	
	// Constructors
	
	/** default constructor */
	public Privilege() {
	}
	
	/** constructor with id */
	public Privilege(String privilege) {
		this.privilege = privilege;
	}
	
	public Privilege(String privilege, String description) {
		this.privilege = privilege;
		setDescription(description);
	}
	
	// Property accessors
	
	/**
	 * @return Returns the privilege.
	 */
	public String getPrivilege() {
		return privilege;
	}
	
	/**
	 * @param privilege The privilege to set.
	 */
	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}
	
	@Override
	public String getName() {
		return this.getPrivilege();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.privilege;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		throw new UnsupportedOperationException();
		
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		throw new UnsupportedOperationException();
		
	}
}
