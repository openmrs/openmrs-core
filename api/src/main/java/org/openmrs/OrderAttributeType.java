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
 * The OrderAttributeType, extension to the {@link Order} class.
 * @see AttributeType
 * @since 2.5.0
 */
public class OrderAttributeType extends BaseAttributeType<Order> implements AttributeType<Order> {

	private Integer orderAttributeTypeId;

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getOrderAttributeTypeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setOrderAttributeTypeId(id);
	}

	/**
	 * @return the order attribute Id
	 */
	public Integer getOrderAttributeTypeId() {
		return orderAttributeTypeId;
	}

	/**
	 * @param orderAttributeTypeId the order attribute type Id to set
	 */
	public void setOrderAttributeTypeId(Integer orderAttributeTypeId) {
		this.orderAttributeTypeId = orderAttributeTypeId;
	}
}
