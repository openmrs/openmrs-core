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

import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;

/**
 * A value for a user-defined {@link VisitAttributeType} that is stored on a {@link Visit}.
 * @see Attribute
 */
public class VisitAttribute extends BaseAttribute<Visit> implements Attribute<Visit> {
	
	private Integer visitAttributeId;
	
	// BaseAttribute<Visit> has an "owner" property of type Visit, which we re-expose as "visit"
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getVisitAttributeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setVisitAttributeId(id);
	}
	
	/**
	 * @return the visit
	 */
	public Visit getVisit() {
		return getOwner();
	}
	
	/**
	 * @param visit the visit to set
	 */
	public void setVisit(Visit visit) {
		setOwner(visit);
	}
	
	/**
	 * @return the visitAttributeId
	 */
	public Integer getVisitAttributeId() {
		return visitAttributeId;
	}
	
	/**
	 * @param visitAttributeId the visitAttributeId to set
	 */
	public void setVisitAttributeId(Integer visitAttributeId) {
		this.visitAttributeId = visitAttributeId;
	}
	
}
