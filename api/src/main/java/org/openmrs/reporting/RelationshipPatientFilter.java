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
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
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
		MessageSourceService msa = Context.getMessageSourceService();
		StringBuilder sb = new StringBuilder();
		sb.append(msa.getMessage("reporting.patients")).append(" ");
		RelationshipType relType = getRelationshipType();
		if (relType != null) {
			if (includeAtoB && includeBtoA) {
				
				sb.append(msa.getMessage("reporting.whoAreEither",
				    new Object[] { relType.getaIsToB(), relType.getbIsToA() }, Context.getLocale()));
				sb.append(" ");
			} else {
				if (includeAtoB) {
					sb.append(msa.getMessage("reporting.whoAre", new Object[] { relType.getaIsToB(), relType.getbIsToA() },
					    Context.getLocale()));
					sb.append(" ");
				}
				if (includeBtoA) {
					sb.append(msa.getMessage("reporting.whoAre", new Object[] { relType.getaIsToB(), relType.getbIsToA() },
					    Context.getLocale()));
					sb.append(" ");
				}
			}
		} else {
			sb.append(msa.getMessage("reporting.withAnyRelationshipTo")).append(" ");
		}
		if (getPerson() != null)
			sb.append(getPerson().toString());
		else
			sb.append(msa.getMessage("reporting.anyone"));
		return sb.toString();
	}
	
	/**
	 * @see org.openmrs.reporting.CachingPatientFilter#getCacheKey()
	 */
	@Override
	public String getCacheKey() {
		StringBuilder sb = new StringBuilder();
		if (getRelationshipType() != null) {
			sb.append("t").append(getRelationshipType().getRelationshipTypeId()).append(".");
			sb.append(includeAtoB).append(".");
			sb.append(includeBtoA).append(".");
		}
		if (getPerson() != null)
			sb.append("p").append(getPerson().getPersonId()).append(".");
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
