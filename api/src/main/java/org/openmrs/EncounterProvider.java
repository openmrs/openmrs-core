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
 * Mapping Class between Encounters and Providers which allows many to many relationship.
 * 
 * @since 1.9
 */
public class EncounterProvider extends BaseChangeableOpenmrsData {
	
	public static final long serialVersionUID = 1L;
	
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
	@Override
	public Integer getId() {
		return getEncounterProviderId();
	}
	
	/**
	 * @see OpenmrsObject#setId(Integer)
	 */
	@Override
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
	
	/**
	 * @return copied encounter provider
	 *
	 * @should copy all EncounterProvider data
	 */
	public EncounterProvider copy() {
		EncounterProvider target = new EncounterProvider();
		target.setChangedBy(getChangedBy());
		target.setCreator(getCreator());
		target.setDateChanged(getDateChanged());
		target.setDateCreated(getDateCreated());
		target.setDateVoided(getDateVoided());
		target.setVoided(getVoided());
		target.setVoidedBy(getVoidedBy());
		target.setVoidReason(getVoidReason());
		target.setEncounter(getEncounter());
		target.setEncounterRole(getEncounterRole());
		target.setProvider(getProvider());
		return target;
	}
}
