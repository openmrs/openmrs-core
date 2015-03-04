/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import org.openmrs.Cohort;
import org.openmrs.Person;
import org.openmrs.RelationshipType;
import org.openmrs.report.EvaluationContext;

/**
 *
 */
public class RelationshipPatientFilter extends CachingPatientFilter {
	
	private Person person;
	
	private RelationshipType relationshipType;
	
	private boolean includeAtoB = true;
	
	private boolean includeBtoA = true;
	
	public RelationshipPatientFilter() {
	}
	
	/**
	 * @see org.openmrs.reporting.CachingPatientFilter#filterImpl(org.openmrs.report.EvaluationContext)
	 */
	@Override
	public Cohort filterImpl(EvaluationContext context) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @see org.openmrs.reporting.AbstractReportObject#getDescription()
	 */
	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Patients ");
		RelationshipType relType = getRelationshipType();
		if (relType != null) {
			if (includeAtoB && includeBtoA) {
				sb.append("who are either " + relType.getaIsToB() + " or " + relType.getbIsToA() + " of ");
			} else {
				if (includeAtoB)
					sb.append("who are " + relType.getaIsToB() + " to " + relType.getbIsToA() + " ");
				if (includeBtoA)
					sb.append("who are " + relType.getbIsToA() + " to " + relType.getaIsToB() + " ");
			}
		} else {
			sb.append("with any relationship to ");
		}
		if (getPerson() != null)
			sb.append(getPerson().toString());
		else
			sb.append("anyone");
		return sb.toString();
	}
	
	/**
	 * @see org.openmrs.reporting.CachingPatientFilter#getCacheKey()
	 */
	@Override
	public String getCacheKey() {
		StringBuilder sb = new StringBuilder();
		if (getRelationshipType() != null) {
			sb.append("t" + getRelationshipType().getRelationshipTypeId() + ".");
			sb.append(includeAtoB + ".");
			sb.append(includeBtoA + ".");
		}
		if (getPerson() != null)
			sb.append("p" + getPerson().getPersonId() + ".");
		return sb.toString();
	}
	
	/**
	 * @see org.openmrs.reporting.CachingPatientFilter#isReadyToRun()
	 */
	@Override
	public boolean isReadyToRun() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public Person getPerson() {
		return person;
	}
	
	public void setPerson(Person person) {
		this.person = person;
	}
	
	public RelationshipType getRelationshipType() {
		return relationshipType;
	}
	
	public void setRelationshipType(RelationshipType relationshipType) {
		this.relationshipType = relationshipType;
	}
	
	public boolean isIncludeAtoB() {
		return includeAtoB;
	}
	
	public void setIncludeAtoB(boolean includeAtoB) {
		this.includeAtoB = includeAtoB;
	}
	
	public boolean isIncludeBtoA() {
		return includeBtoA;
	}
	
	public void setIncludeBtoA(boolean includeBtoA) {
		this.includeBtoA = includeBtoA;
	}
	
}
