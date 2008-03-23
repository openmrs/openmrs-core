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
import org.openmrs.Drug;

public class ConceptDrugListItem {

	protected final Log log = LogFactory.getLog(getClass());

	private Integer drugId;

	private Integer conceptId;
	
	private ConceptListItem concept;

	private String fullName;

	private String name;

	public ConceptDrugListItem() {
	}

	public ConceptDrugListItem(Drug drug, Locale locale) {
		if (drug != null) {
			drugId = drug.getDrugId();
			if (drug.getConcept() != null) {
				conceptId = drug.getConcept().getConceptId();
				concept = new ConceptListItem(drug.getConcept(), locale);
			}
			name = drug.getName();
			fullName = drug.getFullName(locale);
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