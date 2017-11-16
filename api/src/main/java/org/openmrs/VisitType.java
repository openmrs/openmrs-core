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

/**
 * Represents the assortment of visit types available to an implementation. These could include
 * items like "Initial HIV Clinic Visit", "Return TB Clinic Visit", and "Hospitalization".
 * 
 * @since 1.9
 */
public class VisitType extends BaseChangeableOpenmrsMetadata{
	
	private static final long serialVersionUID = 1L;
	
	private Integer visitTypeId;
	
	/** default constructor */
	public VisitType() {
	}
	
	/**
	 * Constructor with id
	 * 
	 * @should set visit type id with given parameter
	 */
	public VisitType(Integer visitTypeId) {
		this.visitTypeId = visitTypeId;
	}
	
	/**
	 * Required values constructor. This is the minimum number of values that must be non-null in
	 * order to have a successful save to the database
	 * 
	 * @param name the name of this visit type
	 * @param description a short description of why this visit type exists
	 */
	public VisitType(String name, String description) {
		setName(name);
		setDescription(description);
	}
	
	/**
	 * @return Returns the visitTypeId.
	 */
	public Integer getVisitTypeId() {
		return visitTypeId;
	}
	
	/**
	 * @param visitTypeId the visitTypeId to set.
	 */
	public void setVisitTypeId(Integer visitTypeId) {
		this.visitTypeId = visitTypeId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getVisitTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setVisitTypeId(id);
	}
	
}
