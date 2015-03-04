/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
