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
 * An EncounterRole a role specific to the encounter. While these could match up to existing
 * organizational roles (e.g., "Nurse"), they don't have to (e.g., "Lead Surgeon")
 *
 * @since 1.9
 */
public class EncounterRole extends BaseOpenmrsMetadata {
	
	public static final String UNKNOWN_ENCOUNTER_ROLE_UUID = "a0b03050-c99b-11e0-9572-0800200c9a66";
	
	// Fields
	private Integer encounterRoleId;
	
	// Constructors
	
	/** default constructor */
	public EncounterRole() {
	}
	
	/**
	 * @param encounterRoleId
	 * @should set encounter role id
	 */
	public EncounterRole(Integer encounterRoleId) {
		this.encounterRoleId = encounterRoleId;
	}
	
	// Property accessors
	
	/**
	 * @see Object#toString()
	 * @should not fail with empty object
	 */
	@Override
	public String toString() {
		String ret = "";
		ret += encounterRoleId == null ? "(no ID) " : encounterRoleId.toString() + " ";
		return "EncounterRole: [" + ret + "]";
	}
	
	/**
	 * @see OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getEncounterRoleId();
	}
	
	/**
	 * @see OpenmrsObject#setId(Integer)
	 */
	public void setId(Integer id) {
		setEncounterRoleId(id);
		
	}
	
	/**
	 * @param encounterRoleId The encounterId to set.
	 */
	private void setEncounterRoleId(Integer encounterRoleId) {
		this.encounterRoleId = encounterRoleId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getEncounterRoleId() {
		return encounterRoleId;
	}
}
