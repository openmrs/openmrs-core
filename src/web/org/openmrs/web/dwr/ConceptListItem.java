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

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptWord;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebUtil;

public class ConceptListItem {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Integer conceptId;
	
	private Integer conceptNameId;
	
	private String name;
	
	private String shortName;
	
	private String description;
	
	/**
	 * Will be non-null if the name hit is not the preferred name
	 */
	private String preferredName;
	
	/**
	 * Synonyms don't exist. All synonyms are names with different tags. If the name hit is not the
	 * preferred name, preferredName will be non-null
	 * 
	 * @deprecated not used anymore
	 */
	private String synonym = "";
	
	private Boolean retired;
	
	private String hl7Abbreviation;
	
	private String className;
	
	private Boolean isSet;
	
	private Boolean isNumeric;
	
	private Double hiAbsolute;
	
	private Double hiCritical;
	
	private Double hiNormal;
	
	private Double lowAbsolute;
	
	private Double lowCritical;
	
	private Double lowNormal;
	
	private String units;
	
	public ConceptListItem() {
	}
	
	/**
	 * Most common constructor
	 * 
	 * @param word
	 */
	public ConceptListItem(ConceptWord word) {
		if (word != null) {
			
			Concept concept = word.getConcept();
			ConceptName conceptName = word.getConceptName();
			Locale locale = word.getLocale();
			initialize(concept, conceptName, locale);
		}
	}
	
	/**
	 * @param concept
	 * @param conceptName
	 * @param locale
	 */
	public ConceptListItem(Concept concept, ConceptName conceptName, Locale locale) {
		initialize(concept, conceptName, locale);
	}
	
	/**
	 * Populate all of the attributes of this class
	 * 
	 * @param concept
	 * @param conceptName
	 * @param locale
	 */
	private void initialize(Concept concept, ConceptName conceptName, Locale locale) {
		if (concept != null) {
			conceptId = concept.getConceptId();
			ConceptName conceptShortName = concept.getShortNameInLocale(locale);
			name = shortName = description = "";
			if (conceptName != null) {
				conceptNameId = conceptName.getConceptNameId();
				name = WebUtil.escapeHTML(conceptName.getName());
				
				// if the name hit is not the preferred one, put the preferred one here
				if (!conceptName.isPreferred()) {
					ConceptName preferredNameObj = concept.getPreferredName(locale);
					preferredName = preferredNameObj.getName();
				}
			}
			if (conceptShortName != null) {
				shortName = WebUtil.escapeHTML(conceptShortName.getName());
			}
			ConceptDescription conceptDescription = concept.getDescription(locale, false);
			if (conceptDescription != null) {
				description = WebUtil.escapeHTML(conceptDescription.getDescription());
			}
			retired = concept.isRetired();
			hl7Abbreviation = concept.getDatatype().getHl7Abbreviation();
			className = concept.getConceptClass().getName();
			isSet = concept.isSet();
			isNumeric = concept.isNumeric();
			if (isNumeric) {
				// TODO: There's probably a better way to do this, but just doing "(ConceptNumeric) concept" throws "java.lang.ClassCastException: org.openmrs.Concept$$EnhancerByCGLIB$$85e62ac7"
				ConceptNumeric num = Context.getConceptService().getConceptNumeric(concept.getConceptId());
				hiAbsolute = num.getHiAbsolute();
				hiCritical = num.getHiCritical();
				hiNormal = num.getHiNormal();
				lowAbsolute = num.getLowAbsolute();
				lowCritical = num.getLowCritical();
				lowNormal = num.getLowNormal();
				units = num.getUnits();
			}
		}
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ConceptListItem) {
			ConceptListItem c2 = (ConceptListItem) obj;
			if (conceptId != null)
				return conceptId.equals(c2.getConceptId());
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (conceptId != null)
			return 31 * conceptId.hashCode();
		else
			return super.hashCode();
	}
	
	public Integer getConceptId() {
		return conceptId;
	}
	
	public Integer getConceptNameId() {
		return conceptNameId;
	}
	
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public String getPreferredName() {
		return preferredName;
	}
	
	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}
	
	@Deprecated
	public String getSynonym() {
		return synonym;
	}
	
	@Deprecated
	public void setSynonym(String synonym) {
		this.synonym = synonym;
	}
	
	public Boolean getRetired() {
		return retired;
	}
	
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
	public String getHl7Abbreviation() {
		return hl7Abbreviation;
	}
	
	public void setHl7Abbreviation(String hl7Abbreviation) {
		this.hl7Abbreviation = hl7Abbreviation;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public Boolean getIsSet() {
		return isSet;
	}
	
	public Double getHiAbsolute() {
		return hiAbsolute;
	}
	
	public void setHiAbsolute(Double hiAbsolute) {
		this.hiAbsolute = hiAbsolute;
	}
	
	public Double getHiCritical() {
		return hiCritical;
	}
	
	public void setHiCritical(Double hiCritical) {
		this.hiCritical = hiCritical;
	}
	
	public Double getHiNormal() {
		return hiNormal;
	}
	
	public void setHiNormal(Double hiNormal) {
		this.hiNormal = hiNormal;
	}
	
	public Double getLowAbsolute() {
		return lowAbsolute;
	}
	
	public void setLowAbsolute(Double lowAbsolute) {
		this.lowAbsolute = lowAbsolute;
	}
	
	public Double getLowCritical() {
		return lowCritical;
	}
	
	public void setLowCritical(Double lowCritical) {
		this.lowCritical = lowCritical;
	}
	
	public Double getLowNormal() {
		return lowNormal;
	}
	
	public void setLowNormal(Double lowNormal) {
		this.lowNormal = lowNormal;
	}
	
	public String getUnits() {
		return units;
	}
	
	public void setUnits(String units) {
		this.units = units;
	}
	
	public Boolean getIsNumeric() {
		return isNumeric;
	}
	
	public void setIsNumeric(Boolean isNumeric) {
		this.isNumeric = isNumeric;
	}
	
}
