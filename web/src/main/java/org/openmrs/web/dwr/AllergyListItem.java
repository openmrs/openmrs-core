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
