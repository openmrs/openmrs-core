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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
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

	public RelationshipListItem() { }
	
	public RelationshipListItem(Relationship r) {
		relationshipId = r.getRelationshipId();
		aIsToB = r.getRelationshipType().getaIsToB();
		bIsToA = r.getRelationshipType().getbIsToA();
		
		Person p = r.getPersonA();
		personA = p.getPersonName().toString();
		personAId = p.getPersonId();
		try {
			Patient pat = Context.getPatientService().getPatient(p.getPersonId());
			personAType = pat != null ? "Patient" : "User";
		} catch (Exception ex) { personAType = "User"; }
		
		p = r.getPersonB();
		personB = p.getPersonName().toString();
		personBId = p.getPersonId();
		try {
			Patient pat = Context.getPatientService().getPatient(p.getPersonId());
			personBType = pat != null ? "Patient" : "User";
		} catch (Exception ex) { personAType = "User"; }
	}
	
	public String toString() {
		return relationshipId + "," + personA + "," + personB + "," + aIsToB + "," + bIsToA + "," + personAId + "," + personBId;
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

}
