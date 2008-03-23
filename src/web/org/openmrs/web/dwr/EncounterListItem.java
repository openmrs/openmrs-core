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
package org.openmrs.web.dwr;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.PersonName;
import org.openmrs.util.Format;

public class EncounterListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer encounterId;
	private String encounterType;
	private String PersonName;
	private String location;
	private String providerName;
	private String formName;
	private Date encounterDateTime;
	private String encounterDateString;
	private boolean voided = false;


	public EncounterListItem() { }
	
	public EncounterListItem(Encounter encounter) {

		if (encounter != null) {
			encounterId = encounter.getEncounterId();
			encounterDateTime = encounter.getEncounterDatetime();
			encounterDateString = Format.format(encounter.getEncounterDatetime());
			PersonName pn = encounter.getPatient().getPersonName();
			if (pn != null) {
				PersonName = "";
				if (pn.getGivenName() != null)
					PersonName += pn.getGivenName();
				if (pn.getMiddleName() != null)
					PersonName += " " + pn.getMiddleName();
				if (pn.getFamilyName() != null)
					PersonName += " " + pn.getFamilyName();
			}
			if (encounter.getProvider() != null)
				providerName = encounter.getProvider().getPersonName().toString();
			if (encounter.getLocation() != null)
				location = encounter.getLocation().getName();
			if (encounter.getEncounterType() != null)
				encounterType = encounter.getEncounterType().getName();
			if (encounter.getForm() != null)
				formName = encounter.getForm().getName();
			voided = encounter.isVoided();
		}
	}

	public Integer getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}

	public Date getEncounterDateTime() {
		return encounterDateTime;
	}

	public void setEncounterDateTime(Date encounterDateTime) {
		this.encounterDateTime = encounterDateTime;
	}

	public String getEncounterDateString() {
		return encounterDateString;
	}

	public void setEncounterDateString(String encounterDateString) {
		this.encounterDateString = encounterDateString;
	}

	
	public String getEncounterType() {
		return encounterType;
	}

	public void setEncounterType(String encounterType) {
		this.encounterType = encounterType;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPersonName() {
		return PersonName;
	}

	public void setPersonName(String PersonName) {
		this.PersonName = PersonName;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public boolean isVoided() {
		return voided;
	}

	public void setVoided(boolean voided) {
		this.voided = voided;
	}
	
	

}
