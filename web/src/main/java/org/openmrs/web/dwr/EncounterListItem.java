/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
	
	private String personName;
	
	private String location;
	
	private String providerName;
	
	private String formName;
	
	private Date encounterDateTime;
	
	private String encounterDateString;
	
	private String entererName;
	
	private boolean voided = false;
	
	private Integer formId;
	
	public EncounterListItem() {
	}
	
	public EncounterListItem(Encounter encounter) {
		
		if (encounter != null) {
			encounterId = encounter.getEncounterId();
			encounterDateTime = encounter.getEncounterDatetime();
			encounterDateString = Format.format(encounter.getEncounterDatetime());
			PersonName pn = encounter.getPatient().getPersonName();
			if (pn != null) {
				personName = "";
				if (pn.getGivenName() != null) {
					personName += pn.getGivenName();
				}
				if (pn.getMiddleName() != null) {
					personName += " " + pn.getMiddleName();
				}
				if (pn.getFamilyName() != null) {
					personName += " " + pn.getFamilyName();
				}
			}
			if (encounter.getProvider() != null) {
				providerName = encounter.getProvider().getPersonName().getFullName();
			}
			if (encounter.getLocation() != null) {
				location = encounter.getLocation().getName();
			}
			if (encounter.getEncounterType() != null) {
				encounterType = encounter.getEncounterType().getName();
			}
			if (encounter.getForm() != null) {
				formName = encounter.getForm().getName();
				formId = encounter.getForm().getFormId();
			}
			voided = encounter.isVoided();
			if (encounter.getCreator() != null) {
				PersonName entererPersonName = encounter.getCreator().getPersonName();
				if (entererPersonName != null) {
					entererName = "";
					if (entererPersonName.getGivenName() != null) {
						entererName += entererPersonName.getGivenName();
					}
					if (entererPersonName.getMiddleName() != null) {
						entererName += " " + entererPersonName.getMiddleName();
					}
					if (entererPersonName.getFamilyName() != null) {
						entererName += " " + entererPersonName.getFamilyName();
					}
				}
			}
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
		return personName;
	}
	
	public void setPersonName(String newPersonName) {
		this.personName = newPersonName;
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
	
	/**
	 * @return the entererName
	 * @since 1.9
	 */
	public String getEntererName() {
		return entererName;
	}
	
	/**
	 * @param entererName the entererName to set
	 * @since 1.9
	 */
	public void setEntererName(String entererName) {
		this.entererName = entererName;
	}
	
	public boolean isVoided() {
		return voided;
	}
	
	public void setVoided(boolean voided) {
		this.voided = voided;
	}
	
	/**
	 * @return the formId
	 * @since 1.9
	 */
	public Integer getFormId() {
		return formId;
	}
	
	/**
	 * @param formId the formId to set
	 * @since 1.9
	 */
	public void setFormId(Integer formId) {
		this.formId = formId;
	}
	
}
