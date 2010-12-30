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

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.util.OpenmrsUtil;

/**
 * TODO
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
	
	protected ActiveListItem(Integer activeListId) {
		this.activeListId = activeListId;
	}
	
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
	
	public void setActiveListId(Integer activeListId) {
		this.activeListId = activeListId;
	}
	
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
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ActiveListItem) && OpenmrsUtil.nullSafeEquals(((ActiveListItem) obj).getId(), getId());
	}
}
