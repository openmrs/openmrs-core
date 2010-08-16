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
package org.openmrs;

import java.util.Date;

import org.openmrs.util.OpenmrsUtil;

/**
 * Represent a concept derived from multiple observations or non-observational data
 * 
 * @version 1.0
 */
public class ConceptDerived extends Concept implements java.io.Serializable {
	
	private static final long serialVersionUID = 6892891255251824009L;
	
	private String rule;
	
	private String language;
	
	private Date compileDate;
	
	private String compileStatus;
	
	private String className;
	
	/**
	 * Default Constructor
	 */
	public ConceptDerived() {
	}
	
	/**
	 * @param conceptId
	 */
	public ConceptDerived(Integer conceptId) {
		super(conceptId);
	}
	
	public ConceptDerived(Concept c) {
		this.setAnswers(c.getAnswers(true));
		this.setChangedBy(c.getChangedBy());
		this.setConceptClass(c.getConceptClass());
		this.setConceptId(c.getConceptId());
		this.setConceptSets(c.getConceptSets());
		this.setCreator(c.getCreator());
		this.setDatatype(c.getDatatype());
		this.setDateChanged(c.getDateChanged());
		this.setDateCreated(c.getDateCreated());
		this.setSet(c.isSet());
		this.setNames(c.getNames());
		this.setDescriptions(c.getDescriptions());
		this.setConceptMappings(c.getConceptMappings());
		this.setRetired(c.isRetired());
		this.setVersion(c.getVersion());
	}
	
	/**
	 * Returns the rule for the derived concept
	 * 
	 * @return rule for the derived concept
	 */
	public String getRule() {
		return rule;
	}
	
	/**
	 * Updates rule for derived concept
	 * 
	 * @param rule new rule for derived concept
	 */
	public void setRule(String rule) {
		this.rule = rule;
	}
	
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
	
	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	
	/**
	 * Date that rule was last compiled
	 * 
	 * @return date that rule was last compiled
	 */
	public Date getCompileDate() {
		return compileDate;
	}
	
	/**
	 * Updates date on which rule was last compiled
	 * 
	 * @param compileDate date on which rule was compiled
	 */
	public void setCompileDate(Date compileDate) {
		this.compileDate = compileDate;
	}
	
	/**
	 * Result status of last compilation of rule
	 * 
	 * @return result status of last compilation of rule
	 */
	public String getCompileStatus() {
		return compileStatus;
	}
	
	/**
	 * Sets result status of last compilation of rule
	 * 
	 * @param compileStatus result status of last compilation of rule
	 */
	public void setCompileStatus(String compileStatus) {
		this.compileStatus = compileStatus;
	}
	
	/**
	 * Returns the full name (including package) of class implementing the rule
	 * 
	 * @return full name (including package) of class implementing the rule
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Updates the name of the class that implements this rule
	 * 
	 * @param className name of class that implements this rule
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * Overrides parent method and returns true if this Concept.getDatatype() equals "Complex"..
	 * 
	 * @see org.openmrs.Concept#isComplex()
	 */
	@Override
    public boolean isRule() {
		if (getDatatype() == null || getDatatype().getHl7Abbreviation() == null)
			return false;
		
		return getDatatype().isRule();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (obj instanceof ConceptDerived) {
			ConceptDerived c = (ConceptDerived) obj;
			return (this.getConceptId().equals(c.getConceptId()));
		} else if (obj instanceof Concept) {
			// use the reverse .equals in case we have hibernate proxies - #1511
			return OpenmrsUtil.nullSafeEquals(((Concept) obj).getConceptId(), this.getConceptId());
		}
		return obj == this;
	}

	/**
	 * @see org.openmrs.Concept#hashCode()
	 */
	@Override
	public int hashCode() {
		if (getConceptId() == null)
			return super.hashCode();
		int hash = 6;
		if (getConceptId() != null)
			hash = hash + getConceptId().hashCode() * 31;
		return hash;
	}

}
