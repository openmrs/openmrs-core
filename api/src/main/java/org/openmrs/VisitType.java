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

/**
 * Represents the assortment of visit types available to an implementation. These could include
 * items like "Initial HIV Clinic Visit", "Return TB Clinic Visit", and "Hospitalization".
 * 
 * @since 1.9
 */
public class VisitType extends BaseOpenmrsMetadata implements java.io.Serializable {
	
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
