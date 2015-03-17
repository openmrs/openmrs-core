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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Drug;

public class ConceptDrugListItem {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Integer drugId;
	
	private Integer conceptId;
	
	private ConceptListItem concept;
	
	private String fullName;
	
	private String name;
	
	private String units;
	
	public ConceptDrugListItem() {
	}
	
	public ConceptDrugListItem(Drug drug, Locale locale) {
		if (drug != null) {
			drugId = drug.getDrugId();
			if (drug.getConcept() != null) {
				conceptId = drug.getConcept().getConceptId();
				concept = new ConceptListItem(drug.getConcept(), null, locale); // ABK: no concept-name associated with drug
			}
			name = drug.getName();
			fullName = drug.getFullName(locale);
			units = drug.getUnits();
		}
	}
	
	public ConceptDrugListItem(Integer drugId, Integer conceptId, String name) {
		this.drugId = drugId;
		this.conceptId = conceptId;
		this.name = name;
		this.fullName = name;
	}
	
	public Integer getDrugId() {
		return drugId;
	}
	
	public void setDrugId(Integer drugId) {
		this.drugId = drugId;
	}
	
	public Integer getConceptId() {
		return conceptId;
	}
	
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}
	
	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public ConceptListItem getConcept() {
		return concept;
	}
	
	public void setConcept(ConceptListItem concept) {
		this.concept = concept;
	}
	
}
