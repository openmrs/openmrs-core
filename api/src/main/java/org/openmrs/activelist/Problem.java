/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.activelist;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Patient;

/**
 * This keeps track of patient's problems. Problems can be two types: RULE_OUT and HISTORY_OF
 * which are placed into ProblemModifier enum.
 */
public class Problem extends ActiveListItem implements Comparable<Problem> {
	
	public static final ActiveListType ACTIVE_LIST_TYPE = new ActiveListType(2);
	
	private ProblemModifier modifier;
	
	// so users can sort problem list to their liking (until someone else comes around and changes it)
	private Double sortWeight;
	
	/**
	 * no argument constructor to construct a problem
	 */
	public Problem() {
		this.activeListType = new ActiveListType(2);
	}
	
	/**
	 * Constructs a problem with a given id
	 *
	 * @param activeListId the activeListId to set. this parameter is the id of the ActiveListItem
	 */
	public Problem(Integer activeListId) {
		super(activeListId);
		this.activeListType = new ActiveListType(2);
	}
	
	/**
	 * Convenience constructor to construct a problem with a given Patient, Concept, start date of the problem, modifier type, comments and sort weigh
	 *
	 * @param person the person that this problem is set for
	 * @param concept the concept to set
	 * @param startDate the startDate to set, when the problem occurred
	 * @param comments additional comments
	 * @param sortWeight the sort value
	 */
	public Problem(Patient person, Concept concept, Date startDate, ProblemModifier modifier, String comments,
	    Double sortWeight) {
		super(person, new ActiveListType(2), concept, startDate);
		this.comments = comments;
		this.modifier = modifier;
		this.sortWeight = sortWeight;
	}
	
	/**
	 * @return the problem modifier
	 */
	public ProblemModifier getModifier() {
		return modifier;
	}
	
	/**
	 * Set the enum value of modifier
	 *
	 * @param modifier the modifier to set
	 */
	public void setModifier(ProblemModifier modifier) {
		this.modifier = modifier;
	}
	
	/**
	 * Set the String value of modifier
	 *
	 * @param modifier the modifier to set
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
	 * Set the sort weight of the problem
	 *
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
	 * @param problem the concept to set
	 */
	public void setProblem(Concept problem) {
		setConcept(problem);
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Note: this comparator imposes orderings that are inconsistent with equals.
	 */
	@SuppressWarnings("squid:S1210")
	@Override
	public int compareTo(Problem item) {
		Double mySW = this.sortWeight;
		Double theirSW = item.getSortWeight();
		
		if ((mySW == null) && (theirSW == null)) {
			return 0;
		}
		if ((mySW == null) && (theirSW != null)) {
			return -1;
		}
		if ((mySW != null) && (theirSW == null)) {
			return 1;
		}
		
		return mySW.compareTo(theirSW);
	}
}
