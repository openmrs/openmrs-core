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
import org.openmrs.Person;
import org.openmrs.Relationship;

import org.openmrs.api.context.Context;

public class RelationshipListItem {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Integer relationshipId;
	
	private String personA;
	
	private String personB;
	
	private String aIsToB;
	
	private String bIsToA;
	
	private Integer personAId;
	
	private Integer personBId;
	
	private String personAType;
	
	private String personBType;
	
	private String startDate = null;
	
	private String endDate = null;
	
	public RelationshipListItem() {
	}
	
	public RelationshipListItem(Relationship r) {
		relationshipId = r.getRelationshipId();
		aIsToB = r.getRelationshipType().getaIsToB();
		bIsToA = r.getRelationshipType().getbIsToA();
		
		Person p = r.getPersonA();
		personA = p.getPersonName().getFullName();
		personAId = p.getPersonId();
		try {
			personAType = p.isPatient() ? "Patient" : "User";
		}
		catch (Exception ex) {
			personAType = "User";
		}
		
		p = r.getPersonB();
		personB = p.getPersonName().getFullName();
		personBId = p.getPersonId();
		try {
			personBType = p.isPatient() ? "Patient" : "User";
		}
		catch (Exception ex) {
			personAType = "User";
		}
		
		Date startDate = r.getStartDate();
		if (startDate != null) {
			this.startDate = Context.getDateFormat().format(startDate);
		}
		Date endDate = r.getEndDate();
		if (endDate != null) {
			this.endDate = Context.getDateFormat().format(endDate);
		}
	}
	
	public String toString() {
		return relationshipId + "," + personA + "," + personB + "," + aIsToB + "," + bIsToA + "," + personAId + ","
		        + personBId;
	}
	
	public String getPersonA() {
		return personA;
	}
	
	public void setPersonA(String fromName) {
		this.personA = fromName;
	}
	
	public Integer getPersonAId() {
		return personAId;
	}
	
	public void setPersonAId(Integer fromPersonId) {
		this.personAId = fromPersonId;
	}
	
	public Integer getRelationshipId() {
		return relationshipId;
	}
	
	public void setRelationshipId(Integer relationshipId) {
		this.relationshipId = relationshipId;
	}
	
	public String getPersonB() {
		return personB;
	}
	
	public void setPersonB(String toName) {
		this.personB = toName;
	}
	
	public Integer getPersonBId() {
		return personBId;
	}
	
	public void setPersonBId(Integer toPersonId) {
		this.personBId = toPersonId;
	}
	
	public String getaIsToB() {
		return aIsToB;
	}
	
	public void setaIsToB(String isToB) {
		aIsToB = isToB;
	}
	
	public String getbIsToA() {
		return bIsToA;
	}
	
	public void setbIsToA(String isToA) {
		bIsToA = isToA;
	}
	
	public String getPersonAType() {
		return personAType;
	}
	
	public void setPersonAType(String personAType) {
		this.personAType = personAType;
	}
	
	public String getPersonBType() {
		return personBType;
	}
	
	public void setPersonBType(String personBType) {
		this.personBType = personBType;
	}
	
	public String getStartDate() {
		return startDate;
	}
	
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public String getEndDate() {
		return endDate;
	}
	
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
}
