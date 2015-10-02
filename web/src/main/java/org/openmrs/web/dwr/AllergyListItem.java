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

import java.util.Calendar;
import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.allergyapi.Allergy;
import org.openmrs.allergyapi.Allergen;
import org.openmrs.allergyapi.AllergenType;
import org.openmrs.allergyapi.AllergyProperties;
import org.openmrs.allergyapi.AllergySeverity;
import org.openmrs.allergyapi.AllergyReaction;

/**
 *
 */
public class AllergyListItem {
	
	private Integer allergyId;
	
	private Integer allergyConceptId;
	
	private String allergen;
	
	private Date start;
	
	private Date end;
	
	private String type;
	
	private String severity;
	
	private Integer reactionConceptId;
	
	private String reaction;
	
	public AllergyListItem(Allergy allergy) {
		this.allergyId = allergy.getAllergyId();
		this.allergyConceptId = allergy.getAllergen().getCodedAllergen().getConceptId();
		this.allergen = allergy.getAllergen().toString();
		// new allergy api does not support dates
		Date tmpDate = new Date();
		this.start = tmpDate;
		this.end = tmpDate;
		this.start.setYear(tmpDate.getYear() - 20);
		this.end.setYear(tmpDate.getYear() + 60);
		// new allergy api does not support dates
		this.type = (allergy.getAllergen() == null) ? null : allergy.getAllergen().toString();
		this.severity = (allergy.getSeverity() == null) ? null : allergy.getSeverity().getName().getName();
		if (allergy.getReactions().size() > 0) {
			AllergyReaction reaction = allergy.getReactions().get(0);
			this.reactionConceptId = reaction.getReaction().getConceptId();
			this.reaction = reaction.getReaction().getName().getName();
		}
	}
	
	/**
	 * @return the allergyId
	 */
	public Integer getAllergyId() {
		return allergyId;
	}
	
	/**
	 * @param allergyId the allergyId to set
	 */
	public void setAllergyId(Integer allergyId) {
		this.allergyId = allergyId;
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
