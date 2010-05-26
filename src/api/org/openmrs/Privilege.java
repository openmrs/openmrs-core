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

/**
 * Privilege
 * 
 * @version 1.0
 */
public class Privilege extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 312L;
	
	// Fields
	private Integer privilegeId;
	
	private String privilege;
	
	// Constructors
	
	/** default constructor */
	public Privilege() {
	}
	
	/** constructor with id */
	public Privilege(Integer privilegeId) {
		this.privilegeId = privilegeId;
	}
	
	public Privilege(String privilege) {
		this.privilege = privilege;
	}
	
	public Privilege(String privilege, String description) {
		this.privilege = privilege;
		setDescription(description);
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Privilege))
			return false;
		return privilege.equals(((Privilege) obj).privilege);
	}
	
	public int hashCode() {
		if (this.getPrivilege() == null)
			return super.hashCode();
		return this.getPrivilege().hashCode();
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
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.privilege;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getPrivilegeId();
		
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setPrivilegeId(id);
		
	}

	/**
     * @param privilegeId the privilegeId to set
     */
    public void setPrivilegeId(Integer privilegeId) {
	    this.privilegeId = privilegeId;
    }

	/**
     * @return the privilegeId
     */
    public Integer getPrivilegeId() {
	    return privilegeId;
    }
}
