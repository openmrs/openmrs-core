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

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSearchResult;
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
	 * Will be non-null if the name hit is not the preferred name. The name is matched against the
	 * fully specified name since version 1.7 when concept name tags were converted to concept name
	 * types
	 */
	private String preferredName;
	
	/**
	 * Synonyms don't exist. All synonyms are names with different tags. If the name hit is not the
	 * preferred name, preferredName will be non-null
	 *
	 * @deprecated not used anymore
	 */
	@Deprecated
	private String synonym = "";
	
	private Boolean retired;
	
	private String hl7Abbreviation;
	
	private String className;
	
	private Boolean isSet;
	
	private Boolean isNumeric;
	
	private Boolean isCodedDatatype;
	
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
	 * @param searchResult the search to use to construct this conceptListItem
	 */
	public ConceptListItem(ConceptSearchResult searchResult) {
		if (searchResult != null) {
			
			Concept concept = searchResult.getConcept();
			ConceptName conceptName = searchResult.getConceptName();
			//associate an index term to a concrete name which is fully specified or a synonym if any is found
			if (conceptName.isIndexTerm() && conceptName.getConcept().getName() != null) {
				conceptName = concept.getName();
			}
			Locale locale = conceptName.getLocale();
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
			name = "";
			shortName = "";
			description = "";
			if (conceptName != null) {
				conceptNameId = conceptName.getConceptNameId();
				if (conceptName.isIndexTerm() && concept.getName() == null) {
					name = WebUtil.escapeHTML(conceptName.getName())
					        + Context.getMessageSourceService().getMessage("Concept.no.fullySpecifiedName.found");
				} else {
					name = WebUtil.escapeHTML(conceptName.getName());
				}
				
				// if the name hit is not the preferred or fully specified one, put the fully specified one here
				if (!conceptName.isPreferred()) {
					ConceptName preferredNameObj = concept.getPreferredName(locale);
					if (preferredNameObj == null && !StringUtils.isBlank(locale.getCountry())
					        || !StringUtils.isBlank(locale.getVariant())) {
						preferredNameObj = concept.getPreferredName(new Locale(locale.getLanguage()));
					}
					if (preferredNameObj != null) {
						preferredName = preferredNameObj.getName();
					}
				}
			}
			if (conceptShortName != null) {
				shortName = WebUtil.escapeHTML(conceptShortName.getName());
			}
			ConceptDescription conceptDescription = concept.getDescription(locale, false);
			if (conceptDescription != null) {
				description = WebUtil.escapeHTML(conceptDescription.getDescription());
			}
			isCodedDatatype = concept.getDatatype().isCoded();
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
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConceptListItem) {
			ConceptListItem c2 = (ConceptListItem) obj;
			if (conceptId != null) {
				return conceptId.equals(c2.getConceptId());
			}
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (conceptId != null) {
			return 31 * conceptId.hashCode();
		} else {
			return super.hashCode();
		}
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
	
	public Boolean getIsCodedDatatype() {
		return isCodedDatatype;
	}
	
	public void setIsCodedDatatype(boolean isCodedDatatype) {
		this.isCodedDatatype = isCodedDatatype;
	}
}
