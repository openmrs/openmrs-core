/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_2;

import org.openmrs.Order;

/**
 * Backing object for FulfillerDetailsResource2_2
 */
public class FulfillerDetails2_2 {
	
	private Order.FulfillerStatus fulfillerStatus;
	
	private String fulfillerComment;
	
	private Order order;
	
	public FulfillerDetails2_2() {
		
	}
	
	public Order.FulfillerStatus getFulfillerStatus() {
		return fulfillerStatus;
	}
	
	public String getFulfillerComment() {
		return fulfillerComment;
	}
	
	public Order getOrder() {
		return order;
	}
	
	public void setFulfillerStatus(Order.FulfillerStatus fulfillerStatus) {
		this.fulfillerStatus = fulfillerStatus;
	}
	
	public void setFulfillerComment(String fulfillerComment) {
		this.fulfillerComment = fulfillerComment;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
}
