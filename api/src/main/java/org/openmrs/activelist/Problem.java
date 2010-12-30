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
package org.openmrs.activelist;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.util.OpenmrsUtil;

/**
 * TODO
 */
public class Problem extends ActiveListItem implements Comparable<Problem> {
	
	public static final ActiveListType ACTIVE_LIST_TYPE = new ActiveListType(2);
	
	private ProblemModifier modifier;
	
	// so users can sort problem list to their liking (until someone else comes around and changes it)
	private Double sortWeight;
	
	public Problem() {
		this.activeListType = new ActiveListType(2);
	}
	
	public Problem(Integer activeListId) {
		super(activeListId);
		this.activeListType = new ActiveListType(2);
	}
	
	public Problem(Patient person, Concept concept, Date startDate, ProblemModifier modifier, String comments,
	    Double sortWeight) {
		super(person, new ActiveListType(2), concept, startDate);
		this.comments = comments;
		this.modifier = modifier;
		this.sortWeight = sortWeight;
	}
	
	/**
	 * @return the status
	 */
	public ProblemModifier getModifier() {
		return modifier;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setModifier(ProblemModifier modifier) {
		this.modifier = modifier;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setModifier(String modifier) {
		this.modifier = StringUtils.isBlank(modifier) ? null : ProblemModifier.getValue(modifier);
	}
	
	/**
	 * @return the sortWeight
	 */
	public Double getSortWeight() {
		return sortWeight;
	}
	
	/**
	 * @param sortWeight the sortWeight to set
	 */
	public void setSortWeight(Double sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	/**
	 * is actually "concept" in ActiveList
	 * 
	 * @return the problem
	 */
	public Concept getProblem() {
		return getConcept();
	}
	
	/**
	 * Set the problem concept
	 * 
	 * @param problem
	 */
	public void setProblem(Concept problem) {
		setConcept(problem);
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Problem) && OpenmrsUtil.nullSafeEquals(((Problem) obj).getActiveListId(), getActiveListId());
	}
	
	@Override
	public int hashCode() {
		return 41 * ((getActiveListId() == null) ? super.hashCode() : getActiveListId());
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Problem item) {
		Double mySW = this.sortWeight;
		Double theirSW = item.getSortWeight();
		
		if ((mySW == null) && (theirSW == null))
			return 0;
		if ((mySW == null) && (theirSW != null))
			return -1;
		if ((mySW != null) && (theirSW == null))
			return 1;
		
		return mySW.compareTo(theirSW);
	}
}
