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

import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

/**
 * A user-defined extension to the {@link Visit} class.
 * @see AttributeType 
 */
public class VisitAttributeType extends BaseAttributeType<Visit> implements AttributeType<Visit> {
	
	private Integer visitAttributeTypeId;
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getVisitAttributeTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setVisitAttributeTypeId(id);
	}
	
	/**
	 * @return the visitAttributeTypeId
	 */
	public Integer getVisitAttributeTypeId() {
		return visitAttributeTypeId;
	}
	
	/**
	 * @param visitAttributeTypeId the visitAttributeTypeId to set
	 */
	public void setVisitAttributeTypeId(Integer visitAttributeTypeId) {
		this.visitAttributeTypeId = visitAttributeTypeId;
	}
	
}
