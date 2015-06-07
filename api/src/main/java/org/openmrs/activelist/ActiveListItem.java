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

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;

/**
 * Active list item is the abstraction of an entry within a clinical list. Active lists can be of two types which are  allergies and problems.
 */
public abstract class ActiveListItem extends BaseOpenmrsData {
	
	protected Integer activeListId;
	
	protected Person person;
	
	protected ActiveListType activeListType;
	
	protected Concept concept; // the value being stored in this (would be equivalent to value_coded in an obs)
	
	protected Date startDate;
	
	protected Date endDate; //(optional)
	
	protected Obs startObs; //(optional) obs that triggered this entry to be created
	
	protected Obs stopObs; //(optional) obs that triggered this entry to be created
	
	protected String comments; //(optional) this is where you'd specify what OTHER NON-CODED means
	
	protected ActiveListItem() {
	}
	
	/**
	 * Construct an Active List item with a given id
	 *
	 * @param activeListId the activeListId to set. This parameter is the id of the active list item.
	 */
	protected ActiveListItem(Integer activeListId) {
		this.activeListId = activeListId;
	}
	
	/**
	 * Convenience constructor to construct an active list item with a given person, active list type, concept and start date
	 *
	 * @param person the person to set this active list item
	 * @param activeListType the type of activeList
	 * @param concept the concept to set
	 * @param startDate the startDate to set, when the active list item occurred
	 */
	protected ActiveListItem(Person person, ActiveListType activeListType, Concept concept, Date startDate) {
		this.person = person;
		this.activeListType = activeListType;
		this.concept = concept;
		this.startDate = startDate;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getActiveListId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setActiveListId(id);
	}
	
	/**
	 * @param activeListId the active list id to set
	 */
	public void setActiveListId(Integer activeListId) {
		this.activeListId = activeListId;
	}
	
	/**
	 * @return the activeListId
	 */
	public Integer getActiveListId() {
		return activeListId;
	}
	
	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @param person the person to set
	 */
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * @return the activeListType
	 */
	public ActiveListType getActiveListType() {
		return activeListType;
	}
	
	/**
	 * @param activeListType the activeListType to set
	 */
	public void setActiveListType(ActiveListType activeListType) {
		this.activeListType = activeListType;
	}
	
	/**
	 * @return the concept
	 */
	protected Concept getConcept() {
		return concept;
	}
	
	/**
	 * @param concept the concept to set
	 */
	protected void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}
	
	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}
	
	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	/**
	 * @return the startObs
	 */
	public Obs getStartObs() {
		return startObs;
	}
	
	/**
	 * @param startObs the startObs to set
	 */
	public void setStartObs(Obs startObs) {
		this.startObs = startObs;
	}
	
	/**
	 * @return the stopObs
	 */
	public Obs getStopObs() {
		return stopObs;
	}
	
	/**
	 * @param stopObs the stopObs to set
	 */
	public void setStopObs(Obs stopObs) {
		this.stopObs = stopObs;
	}
	
	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}
	
	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}
}
