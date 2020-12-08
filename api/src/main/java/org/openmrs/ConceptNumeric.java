/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.HashSet;
import java.util.TreeSet;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.Indexed;

/**
 * The ConceptNumeric extends upon the Concept object by adding some number range values
 * 
 * @see Concept
 */
@Indexed
public class ConceptNumeric extends Concept {
	
	public static final long serialVersionUID = 47323L;
	
	// Fields
	
	private Double hiAbsolute;
	
	private Double hiCritical;
	
	private Double hiNormal;
	
	private Double lowAbsolute;
	
	private Double lowCritical;
	
	private Double lowNormal;
	
	private String units;
	
	private Boolean allowDecimal = false;
	
	/**
	 * displayPrecision, represents the number of significant digits
	 * to be used for display of a numeric value
	 */
	private Integer displayPrecision;
	
	// Constructors
	
	/** default constructor */
	public ConceptNumeric() {
	}
	
	/**
	 * Generic constructor taking the primary key
	 * 
	 * @param conceptId key for this numeric concept
	 */
	public ConceptNumeric(Integer conceptId) {
		setConceptId(conceptId);
	}
	
	/**
	 * Optional constructor for turning a Concept into a ConceptNumeric <br>
	 * <br>
	 * Note: This cannot copy over numeric specific values
	 * 
	 * @param c
	 * <strong>Should</strong> make deep copy of collections
	 * <strong>Should</strong> change reference to the parent object  for objects in answers collection
	 * <strong>Should</strong> change reference to the parent object  for objects in conceptSets collection
	 * <strong>Should</strong> change reference to the parent object  for objects in names collection
	 * <strong>Should</strong> change reference to the parent object  for objects in descriptions collection
	 * <strong>Should</strong> change reference to the parent object  for objects in conceptMappings collection
	 */
	public ConceptNumeric(Concept c) {
		this.setChangedBy(c.getChangedBy());
		this.setConceptClass(c.getConceptClass());
		this.setConceptId(c.getConceptId());
		this.setCreator(c.getCreator());
		this.setDatatype(c.getDatatype());
		this.setDateChanged(c.getDateChanged());
		this.setDateCreated(c.getDateCreated());
		this.setSet(c.getSet());
		this.setRetired(c.getRetired());
		this.setRetiredBy(c.getRetiredBy());
		this.setRetireReason(c.getRetireReason());
		this.setVersion(c.getVersion());
		this.setUuid(c.getUuid());
		
		this.setNames(new HashSet<>(c.getNames()));
		for (ConceptName cName : this.getNames()) {
			cName.setConcept(this);
		}
		
		this.setAnswers(new HashSet<>(c.getAnswers(true)));
		for (ConceptAnswer cAnswer : this.getAnswers()) {
			cAnswer.setConcept(this);
		}
		
		this.setConceptSets(new TreeSet<>(c.getConceptSets()));
		for (ConceptSet cSet : this.getConceptSets()) {
			cSet.setConceptSet(this);
		}
		
		this.setDescriptions(new HashSet<>(c.getDescriptions()));
		for (ConceptDescription cDescription : this.getDescriptions()) {
			cDescription.setConcept(this);
		}
		
		this.setConceptMappings(new HashSet<>(c.getConceptMappings()));
		for (ConceptMap cMap : this.getConceptMappings()) {
			cMap.setConcept(this);
		}
		
		this.hiAbsolute = null;
		this.hiCritical = null;
		this.hiNormal = null;
		this.lowAbsolute = null;
		this.lowCritical = null;
		this.lowNormal = null;
		this.units = "";
		this.allowDecimal = false;
	}
	
	// Property accessors
	
	public Double getHiAbsolute() {
		return this.hiAbsolute;
	}
	
	public void setHiAbsolute(Double hiAbsolute) {
		this.hiAbsolute = hiAbsolute;
	}
	
	public Double getHiCritical() {
		return this.hiCritical;
	}
	
	public void setHiCritical(Double hiCritical) {
		this.hiCritical = hiCritical;
	}
	
	public Double getHiNormal() {
		return this.hiNormal;
	}
	
	public void setHiNormal(Double hiNormal) {
		this.hiNormal = hiNormal;
	}
	
	public Double getLowAbsolute() {
		return this.lowAbsolute;
	}
	
	public void setLowAbsolute(Double lowAbsolute) {
		this.lowAbsolute = lowAbsolute;
	}
	
	public Double getLowCritical() {
		return this.lowCritical;
	}
	
	public void setLowCritical(Double lowCritical) {
		this.lowCritical = lowCritical;
	}
	
	public Double getLowNormal() {
		return this.lowNormal;
	}
	
	public void setLowNormal(Double lowNormal) {
		this.lowNormal = lowNormal;
	}
	
	public String getUnits() {
		return this.units;
	}
	
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * This method will <i>always</i> return true for ConceptNumeric objects that have a datatype of
	 * Numeric
	 * 
	 * @see org.openmrs.Concept#isNumeric()
	 */
	@Override
	public boolean isNumeric() {
		return "Numeric".equals(getDatatype().getName());
	}
	
	/**
	 * @return displayPrecision to be used for the display of a numeric value
	 */
	public Integer getDisplayPrecision() {
		return displayPrecision;
	}

	/**
	 * @param displayPrecision sets displayPrecision to be used for the display of a numeric value
	 */
	public void setDisplayPrecision(Integer displayPrecision) {
		this.displayPrecision = displayPrecision;
	}
	
	public Boolean getAllowDecimal() {
		return allowDecimal == null ? Boolean.FALSE : allowDecimal;
	}
	
	public void setAllowDecimal(Boolean allowDecimal) {
		this.allowDecimal = allowDecimal;
	}
	
	/**
	 * @deprecated as of 2.0, use {@link #getAllowDecimal()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isAllowDecimal() {
		return getAllowDecimal();
	}
}
