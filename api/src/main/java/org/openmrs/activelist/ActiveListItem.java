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

/**
 * The Active list Item table holds the general properties while the allergy and problem tables add on specific properties relevant to those objects. Active lists can be of two types which are  allergies and problems.
 */
public abstract class ActiveListItem extends BaseOpenmrsData {
	
	protected Integer activeListId;
	
	protected Person person;
	
	protected ActiveListType activeListType;
	
	protected Concept concept; // the value being stored in this (would be equivalent to value_coded in an obs)
	
	protected Date startDate;
	
	protected Date endDate; //(optional)
	
	protected Obs startObs; //(optional) obs that triggered this entry to be created

	protected Obs startObs; //(optional) obs that triggered this entry to be created
	
	protected Obs stopObs; //(optional) obs that triggered this entry to be created
	
	protected String comments; //(optional) this is where you'd specify what OTHER NON-CODED means
	/**
	 * non argument constructor
	 */
	protected ActiveListItem() {
	}
	/**
	 * one argument constructor 
	 * @param activeListId the activeListId to set. the unique Identifier for the object
	 */
	protected ActiveListItem(Integer activeListId) {
		this.activeListId = activeListId;
	}
	/**
	 * four argument constructor
	 * @param person the person to set
	 * @param activeListId the activeListId to set
	 * @param concept the concept to set
	 * @param startDate the startDate to set
	 */
	protected ActiveListItem(Person person, ActiveListType activeListType, Concept concept, Date startDate) {
		this.person = person;
		this.activeListType = activeListType;
		this.concept = concept;
		this.startDate = startDate;
	}
	
	/**
	 * get the value activeListId-the unique Identifier for the object
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getActiveListId();
	}
	
	/**
	 * set the value activeListId-the unique Identifier for the object
	 * @param id the activeListId to set
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setActiveListId(id);
	}
	/**
	 * set the value activeListId-the unique Identifier for the object
	 * @param activeListId the activeListId to set
	 */
	public void setActiveListId(Integer activeListId) {
		this.activeListId = activeListId;
	}
	/**
	 * get the value activeListId-the unique Identifier for the object
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
	 * set the value person
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
	 * setbthe activeListType
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
	 * set the concept
	 * @param concept the concept to set
	 */
	protected void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * get the start date
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}
	
	/**
	 * set the start date
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
	 * set the end date
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	/**
	 * get the reference to the start obs
	 * @return the startObs
	 */
	public Obs getStartObs() {
		return startObs;
	}
	
	/**
	 * set the reference to the start obs
	 * @param startObs the startObs to set
	 */
	public void setStartObs(Obs startObs) {
		this.startObs = startObs;
	}
	
	/**
	 * get the reference to the stop obs
	 * @return the stopObs
	 */
	public Obs getStopObs() {
		return stopObs;
	}
	
	/**
	 * set the reference to the stop obs
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
	 * set comments
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}
}
