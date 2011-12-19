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
 * Mapping Class between Encounters and Providers which allows many to many relationship.
 * 
 * @since 1.9
 */
public class EncounterProvider extends BaseOpenmrsData {
	
	private Integer encounterProviderId;
	
	private Encounter encounter;
	
	private Provider provider;
	
	private EncounterRole encounterRole;
	
	public void setEncounterProviderId(Integer encounterProviderId) {
		this.encounterProviderId = encounterProviderId;
	}
	
	public Integer getEncounterProviderId() {
		return this.encounterProviderId;
	}
	
	/**
	 * @see OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getEncounterProviderId();
	}
	
	/**
	 * @see OpenmrsObject#setId(Integer)
	 */
	public void setId(Integer id) {
		setEncounterProviderId(id);
	}
	
	/**
	 * @return the encounter
	 * @see Encounter
	 */
	public Encounter getEncounter() {
		return this.encounter;
	}
	
	/**
	 * @param encounter the encounter to set
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}
	
	/**
	 * @return the provider
	 * @see Provider
	 */
	public Provider getProvider() {
		return this.provider;
	}
	
	/**
	 * @param provider the provider to set
	 */
	public void setProvider(Provider provider) {
		this.provider = provider;
	}
	
	/**
	 * @return the encounterRole
	 * @see EncounterRole
	 */
	public EncounterRole getEncounterRole() {
		return this.encounterRole;
	}
	
	/**
	 * @param encounterRole the encounterRole to set
	 */
	public void setEncounterRole(EncounterRole encounterRole) {
		this.encounterRole = encounterRole;
	}
}
