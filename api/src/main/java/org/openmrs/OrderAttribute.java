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
 * A value for a user-defined {@link OrderAttributeType} that is stored in an {@link Order}.
 * @see Attribute
 * @since 2.4.0
 */
public class OrderAttribute extends BaseAttribute<OrderAttributeType, Order> implements Attribute<OrderAttributeType, Order> {

	private Integer orderAttributeId;

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getOrderAttributeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setOrderAttributeId(id);
	}

	/**
	 * @return the order attribute Id
	 */
	public Integer getOrderAttributeId() {
		return orderAttributeId;
	}

	/**
	 * @param orderAttributeId the order attribute Id to set
	 */
	public void setOrderAttributeId(Integer orderAttributeId) {
		this.orderAttributeId = orderAttributeId;
	}
}