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

import org.openmrs.BaseOpenmrsMetadata;

/**
 * Active lists can be of two types: allergies and problems. So the active list type is the metadata
 * that says what kind of item is.
 */
public class ActiveListType extends BaseOpenmrsMetadata {
	
	/**
	 * the unique Identifier for the ActiveListType object
	 */
	private Integer activeListTypeId;
	
	public ActiveListType() {
	}
	
	/**
	 * constructs an ActiveListType object with a given id
	 * 
	 * @param id the activeListTypeId to set
	 */
	public ActiveListType(Integer id) {
		this.activeListTypeId = id;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getActiveListTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setActiveListTypeId(id);
	}
	
	/**
	 * @return the activeListTypeId
	 */
	public Integer getActiveListTypeId() {
		return activeListTypeId;
	}
	
	/**
	 * @param activeListTypeId the activeListTypeId to set
	 */
	public void setActiveListTypeId(Integer activeListTypeId) {
		this.activeListTypeId = activeListTypeId;
	}
}
