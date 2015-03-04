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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;

public class PatientListItem extends PersonListItem {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Integer patientId;
	
	private String identifier = "";
	
	private Boolean identifierCheckDigit = false;
	
	private String identifierTypeName = "";
	
	private String otherIdentifiers = "";
	
	public PatientListItem() {
	}
	
	public PatientListItem(Patient patient) {
		this(patient, null);
	}
	
	public PatientListItem(Patient patient, String searchName) {
		super(patient, searchName);
		
		if (patient != null) {
			
			patientId = patient.getPatientId();
			
			// get patient's identifiers
			boolean first = true;
			for (PatientIdentifier pi : patient.getIdentifiers()) {
				if (first) {
					identifier = pi.getIdentifier();
					identifierCheckDigit = pi.getIdentifierType().hasCheckDigit();
					first = false;
				} else {
					if (!"".equals(otherIdentifiers)) {
						otherIdentifiers += ",";
					}
					otherIdentifiers += " " + pi.getIdentifier();
				}
			}
			
		}
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof PatientListItem) {
			PatientListItem pi = (PatientListItem) obj;
			if (pi.getPatientId() == null || patientId == null) {
				return false;
			}
			return pi.getPatientId().equals(patientId);
		}
		return false;
	}
	
	public int hashCode() {
		if (patientId == null) {
			return super.hashCode();
		}
		return patientId.hashCode();
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public String getOtherIdentifiers() {
		return otherIdentifiers;
	}
	
	public void setOtherIdentifiers(String otherIdentifiers) {
		this.otherIdentifiers = otherIdentifiers;
	}
	
	public Integer getPatientId() {
		return patientId;
	}
	
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	
	/**
	 * @return Returns the identifierIdentifierCheckdigit.
	 */
	public Boolean getIdentifierCheckDigit() {
		return identifierCheckDigit;
	}
	
	/**
	 * @param identifierIdentifierCheckdigit The identifierIdentifierCheckdigit to set.
	 */
	public void setIdentifierCheckDigit(Boolean identifierCheckDigit) {
		this.identifierCheckDigit = identifierCheckDigit;
	}
	
	/**
	 * @param identifierTypeName the identifierTypeName to set
	 */
	public void setIdentifierTypeName(String identifierTypeName) {
		this.identifierTypeName = identifierTypeName;
	}
	
	/**
	 * @return the identifierTypeName
	 */
	public String getIdentifierTypeName() {
		return identifierTypeName;
	}
	
}
