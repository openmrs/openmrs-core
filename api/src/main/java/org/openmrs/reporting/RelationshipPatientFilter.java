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
				sb.append(
				    msa.getMessage("reporting.whoAreEither", new Object[] { relType.getaIsToB(), relType.getbIsToA() },
				        Context.getLocale())).append(" ");
			} else {
				if (includeAtoB) {
					sb.append(
					    msa.getMessage("reporting.whoAre", new Object[] { relType.getaIsToB(), relType.getbIsToA() },
					        Context.getLocale())).append(" ");
				}
				if (includeBtoA) {
					sb.append(
					    msa.getMessage("reporting.whoAre", new Object[] { relType.getaIsToB(), relType.getbIsToA() },
					        Context.getLocale())).append(" ");
				}
			}
		} else {
			sb.append(msa.getMessage("reporting.withAnyRelationshipTo")).append(" ");
		}
		if (getPerson() != null) {
			sb.append(getPerson().toString());
		} else {
			sb.append(msa.getMessage("reporting.anyone"));
		}
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
		if (getPerson() != null) {
			sb.append("p").append(getPerson().getPersonId()).append(".");
		}
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
