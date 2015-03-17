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
