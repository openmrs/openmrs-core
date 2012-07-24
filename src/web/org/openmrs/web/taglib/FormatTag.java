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
package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.Date;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Obs;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;
import org.springframework.web.util.JavaScriptUtils;

/**
 * <pre>
 * Prints out a pretty-formatted versions of an OpenMRS object
 * TODO: add the other openmrs domain objects
 * TODO: add a size=compact|NORMAL|full|? option
 * </pre>
 */
public class FormatTag extends TagSupport {
	
	private static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private String var;
	
	private Integer conceptId;
	
	private Concept concept;
	
	private Obs obsValue;
	
	private Integer userId;
	
	private User user;
	
	private Integer personId;
	
	private Person person;
	
	private Integer encounterId;
	
	private Encounter encounter;
	
	private Integer encounterTypeId;
	
	private EncounterType encounterType;
	
	private Integer locationId;
	
	private Location location;
	
	private Integer locationTagId;
	
	private LocationTag locationTag;
	
	private Boolean javaScriptEscape = Boolean.FALSE;
	
	@Override
	public int doStartTag() {
		StringBuilder sb = new StringBuilder();
		if (conceptId != null)
			concept = Context.getConceptService().getConcept(conceptId);
		if (concept != null) {
			if (concept.getName() != null)
				sb.append(concept.getName().getName());
		}
		
		if (obsValue != null)
			sb.append(obsValue.getValueAsString(Context.getLocale()));
		
		if (userId != null)
			user = Context.getUserService().getUser(userId);
		if (user != null)
			printUser(sb, user);
		
		if (personId != null)
			person = Context.getPersonService().getPerson(personId);
		if (person != null)
			printPerson(sb, person);
		
		if (encounterId != null)
			encounter = Context.getEncounterService().getEncounter(encounterId);
		if (encounter != null) {
			printMetadata(sb, encounter.getEncounterType());
			sb.append(" @");
			printMetadata(sb, encounter.getLocation());
			sb.append(" | ");
			printDate(sb, encounter.getEncounterDatetime());
			sb.append(" | ");
			printPerson(sb, encounter.getProvider());
		}
		
		if (encounterTypeId != null)
			encounterType = Context.getEncounterService().getEncounterType(encounterTypeId);
		if (encounterType != null) {
			printMetadata(sb, encounterType);
		}
		
		if (locationId != null)
			location = Context.getLocationService().getLocation(locationId);
		if (location != null) {
			printMetadata(sb, location);
		}
		
		if (locationTagId != null)
			locationTag = Context.getLocationService().getLocationTag(locationTagId);
		if (locationTag != null) {
			printMetadata(sb, locationTag);
		}
		
		if (StringUtils.hasText(var)) {
			if (javaScriptEscape)
				pageContext.setAttribute(var, JavaScriptUtils.javaScriptEscape(sb.toString()));
			else
				pageContext.setAttribute(var, sb.toString());
		} else {
			try {
				if (javaScriptEscape)
					pageContext.getOut().write(JavaScriptUtils.javaScriptEscape(sb.toString()));
				else
					pageContext.getOut().write(sb.toString());
			}
			catch (IOException e) {
				log.error("Failed to write to pageContext.getOut()", e);
			}
		}
		return SKIP_BODY;
	}
	
	/**
	 * formats a date and prints it to sb
	 * 
	 * @param sb
	 * @param date
	 */
	private void printDate(StringBuilder sb, Date date) {
		sb.append(Context.getDateFormat().format(date));
	}
	
	/**
	 * formats any OpenmrsMetadata and prints it to sb
	 * 
	 * @param sb
	 * @param metadata
	 */
	private void printMetadata(StringBuilder sb, OpenmrsMetadata metadata) {
		if (metadata != null)
			sb.append(metadata.getName());
	}
	
	/**
	 * formats a user and prints it to sb
	 * 
	 * @param sb
	 * @param u
	 */
	private void printUser(StringBuilder sb, User u) {
		sb.append("<span class=\"user\">");
		sb.append("<span class=\"username\">");
		sb.append(u.getUsername());
		sb.append("</span>");
		if (u.getPerson() != null) {
			sb.append("<span class=\"personName\">");
			sb.append(" (").append(u.getPersonName()).append(")");
			sb.append("</span>");
		}
		sb.append("</span>");
	}
	
	/**
	 * formats a person and prints it to sb
	 * 
	 * @param sb
	 * @param p
	 */
	private void printPerson(StringBuilder sb, Person p) {
		sb.append(p.getPersonName());
	}
	
	@Override
	public int doEndTag() {
		reset();
		return EVAL_PAGE;
	}
	
	private void reset() {
		var = null;
		conceptId = null;
		concept = null;
		obsValue = null;
		userId = null;
		user = null;
		personId = null;
		person = null;
		encounterId = null;
		encounter = null;
		encounterTypeId = null;
		encounterType = null;
		locationId = null;
		location = null;
		locationTagId = null;
		locationTag = null;
	}
	
	public Integer getConceptId() {
		return conceptId;
	}
	
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public Obs getObsValue() {
		return obsValue;
	}
	
	public void setObsValue(Obs obsValue) {
		this.obsValue = obsValue;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public Integer getEncounterId() {
		return encounterId;
	}
	
	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}
	
	public Encounter getEncounter() {
		return encounter;
	}
	
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}
	
	public Integer getEncounterTypeId() {
		return encounterTypeId;
	}
	
	public void setEncounterTypeId(Integer encounterTypeId) {
		this.encounterTypeId = encounterTypeId;
	}
	
	public EncounterType getEncounterType() {
		return encounterType;
	}
	
	public void setEncounterType(EncounterType encounterType) {
		this.encounterType = encounterType;
	}
	
	public Integer getLocationId() {
		return locationId;
	}
	
	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Integer getLocationTagId() {
		return locationTagId;
	}
	
	public void setLocationTagId(Integer locationTagId) {
		this.locationTagId = locationTagId;
	}
	
	public LocationTag getLocationTag() {
		return locationTag;
	}
	
	public void setLocationTag(LocationTag locationTag) {
		this.locationTag = locationTag;
	}
	
	public String getVar() {
		return var;
	}
	
	public void setVar(String var) {
		this.var = var;
	}
	
	public Integer getPersonId() {
		return personId;
	}
	
	public void setPersonId(Integer personId) {
		this.personId = personId;
	}
	
	public Person getPerson() {
		return person;
	}
	
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * @return the javaScriptEscape
	 */
	public Boolean getJavaScriptEscape() {
		return javaScriptEscape;
	}
	
	/**
	 * @param javaScriptEscape the javaScriptEscape to set
	 */
	public void setJavaScriptEscape(Boolean javaScriptEscape) {
		this.javaScriptEscape = javaScriptEscape;
	}
}
