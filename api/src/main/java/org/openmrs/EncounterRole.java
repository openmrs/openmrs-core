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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;

/**
 * An EncounterRole a role specific to the encounter. While these could match up to existing
 * organizational roles (e.g., "Nurse"), they don't have to (e.g., "Lead Surgeon")
 *
 * @since 1.9
 */
@Entity
@Table(name = "encounter_role")
@BatchSize(size = 25)
@Audited
public class EncounterRole extends BaseChangeableOpenmrsMetadata {
	
	public static final String UNKNOWN_ENCOUNTER_ROLE_UUID = "a0b03050-c99b-11e0-9572-0800200c9a66";
	
	// Fields
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "encounter_role_id_seq")
	@GenericGenerator(
			name = "encounter_role_id_seq",
			strategy = "native",
			parameters = @Parameter(name = "sequence", value = "encounter_role_encounter_role_id_seq")
	)
	@Column(name = "encounter_role_id", nullable = false)
	private Integer encounterRoleId;
	
	// Constructors
	
	/** default constructor */
	public EncounterRole() {
	}
	
	/**
	 * @param encounterRoleId
	 * <strong>Should</strong> set encounter role id
	 */
	public EncounterRole(Integer encounterRoleId) {
		this.encounterRoleId = encounterRoleId;
	}
	
	// Property accessors
	
	/**
	 * @see Object#toString()
	 * <strong>Should</strong> not fail with empty object
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
	@Override
	public Integer getId() {
		
		return getEncounterRoleId();
	}
	
	/**
	 * @see OpenmrsObject#setId(Integer)
	 */
	@Override
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
