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

import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;

/**
 * A value for a user-defined {@link VisitAttributeType} that is stored on a {@link Visit}.
 * @see Attribute
 * @since 1.9
 */
public class VisitAttribute extends BaseAttribute<VisitAttributeType, Visit> implements Attribute<VisitAttributeType, Visit> {
	
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
