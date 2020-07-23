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

import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

/**
 * A user-defined extension to the {@link OrderSet} class.
 * @see AttributeType
 * @since 2.4.0
 */
public class OrderSetAttributeType extends BaseAttributeType<OrderSet> implements AttributeType<OrderSet> {
	
	private Integer orderSetAttributeTypeId;

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getOrderSetAttributeTypeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setOrderSetAttributeTypeId(id);
	}

	/**
	 * @return the orderSetAttributeTypeId
	 */
	public Integer getOrderSetAttributeTypeId() {
		return orderSetAttributeTypeId;
	}

	/**
	 * @param orderSetAttributeTypeId the orderSetAttributeTypeId to set
	 */
	public void setOrderSetAttributeTypeId(Integer orderSetAttributeTypeId) {
		this.orderSetAttributeTypeId = orderSetAttributeTypeId;
	}

}
