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

import org.openmrs.activelist.Allergy;

/**
 *
 */
public class AllergyListItem {
	
	private Integer activeListId;
	
	private Integer allergyConceptId;
	
	private String allergen;
	
	private Date start;
	
	private Date end;
	
	private String type;
	
	private String severity;
	
	private Integer reactionConceptId;
	
	private String reaction;
	
	public AllergyListItem(Allergy allergy) {
		this.activeListId = allergy.getActiveListId();
		this.allergyConceptId = allergy.getAllergen().getConceptId();
		this.allergen = allergy.getAllergen().getName().getName();
		this.start = allergy.getStartDate();
		this.end = allergy.getEndDate();
		this.type = (allergy.getAllergyType() == null) ? null : allergy.getAllergyType().name();
		this.severity = (allergy.getSeverity() == null) ? null : allergy.getSeverity().name();
		if (allergy.getReaction() != null) {
			this.reactionConceptId = allergy.getReaction().getConceptId();
			this.reaction = allergy.getReaction().getName().getName();
		}
	}
	
	public Integer getActiveListId() {
		return activeListId;
	}
	
	public void setAllergyConceptId(Integer allergyConceptId) {
		this.allergyConceptId = allergyConceptId;
	}
	
	public Integer getAllergyConceptId() {
		return allergyConceptId;
	}
	
	public void setAllergen(String allergen) {
		this.allergen = allergen;
	}
	
	public String getAllergen() {
		return allergen;
	}
	
	public void setStart(Date start) {
		this.start = start;
	}
	
	public Date getStart() {
		return start;
	}
	
	public void setEnd(Date end) {
		this.end = end;
	}
	
	public Date getEnd() {
		return end;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	
	public String getSeverity() {
		return severity;
	}
	
	public void setReactionConceptId(Integer reactionConceptId) {
		this.reactionConceptId = reactionConceptId;
	}
	
	public Integer getReactionConceptId() {
		return reactionConceptId;
	}
	
	public void setReaction(String reaction) {
		this.reaction = reaction;
	}
	
	public String getReaction() {
		return reaction;
	}
}
