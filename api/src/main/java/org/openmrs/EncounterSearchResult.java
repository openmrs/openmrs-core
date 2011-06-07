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

import org.springframework.beans.BeanUtils;

/**
 * An Object of this class represents a search result returned when searching for encounters and the
 * results need to be sorted by patient name while supporting paging, it represents a mini encounter
 * object
 * 
 * @since 1.8
 */
public class EncounterSearchResult implements java.io.Serializable {
	
	private static final long serialVersionUID = -8682914049020452646L;
	
	private Integer encounterId;
	
	private Date encounterDatetime;
	
	private Patient patient;
	
	private Location location;
	
	private Form form;
	
	private EncounterType encounterType;
	
	private Person provider;
	
	private Boolean voided = Boolean.FALSE;
	
	public EncounterSearchResult() {
	}
	
	public EncounterSearchResult(Encounter enc) {
		BeanUtils.copyProperties(enc, this);
	}
	
	/**
	 * @return the encounterId
	 */
	public Integer getEncounterId() {
		return encounterId;
	}
	
	/**
	 * @param encounterId the encounterId to set
	 */
	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}
	
	/**
	 * @return the encounterDatetime
	 */
	public Date getEncounterDatetime() {
		return encounterDatetime;
	}
	
	/**
	 * @param encounterDatetime the encounterDatetime to set
	 */
	public void setEncounterDatetime(Date encounterDatetime) {
		this.encounterDatetime = encounterDatetime;
	}
	
	/**
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	
	/**
	 * @return the form
	 */
	public Form getForm() {
		return form;
	}
	
	/**
	 * @param form the form to set
	 */
	public void setForm(Form form) {
		this.form = form;
	}
	
	/**
	 * @return the encounterType
	 */
	public EncounterType getEncounterType() {
		return encounterType;
	}
	
	/**
	 * @param encounterType the encounterType to set
	 */
	public void setEncounterType(EncounterType encounterType) {
		this.encounterType = encounterType;
	}
	
	/**
	 * @return the provider
	 */
	public Person getProvider() {
		return provider;
	}
	
	/**
	 * @param provider the provider to set
	 */
	public void setProvider(Person provider) {
		this.provider = provider;
	}
	
	/**
	 * @return the voided
	 */
	public Boolean getVoided() {
		return voided;
	}
	
	/**
	 * @param voided the voided to set
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EncounterSearchResult))
			return false;
		
		EncounterSearchResult rhs = (EncounterSearchResult) obj;
		if (this.encounterId != null && rhs.encounterId != null)
			return this.encounterId.equals(rhs.encounterId);
		
		return this == obj;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.encounterId == null)
			return super.hashCode();
		int hash = 3;
		hash = hash + 31 * this.encounterId;
		return hash;
	}
}
