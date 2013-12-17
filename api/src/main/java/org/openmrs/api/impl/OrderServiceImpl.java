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
package org.openmrs.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.OrderNumberGenerator;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;

/**
 * Default implementation of the Order-related services class. This method should not be invoked by
 * itself. Spring injection is used to inject this implementation into the ServiceContext. Which
 * implementation is injected is determined by the spring application context file:
 * /metadata/api/spring/applicationContext.xml
 * 
 * @see org.openmrs.api.OrderService
 */
public class OrderServiceImpl extends BaseOpenmrsService implements OrderService, OrderNumberGenerator {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected OrderDAO dao;
	
	public OrderServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.api.OrderService#setOrderDAO(org.openmrs.api.db.OrderDAO)
	 */
	public void setOrderDAO(OrderDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.OrderNumberGenerator#getNewOrderNumber()
	 */
	@Override
	public synchronized String getNewOrderNumber() {
		GlobalProperty globalProperty = Context.getAdministrationService().getGlobalPropertyObject(
		    OpenmrsConstants.GLOBAL_PROPERTY_NEXT_ORDER_NUMBER);
		if (globalProperty == null) {
			globalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_NEXT_ORDER_NUMBER, "1",
			        "The next order number available for assignment");
		}
		
		String orderNumber = "ORD-" + globalProperty.getValue();
		
		globalProperty.setPropertyValue(String.valueOf(Long.parseLong(globalProperty.getPropertyValue()) + 1));
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		Context.getAdministrationService().saveGlobalProperty(globalProperty);
		Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		
		return orderNumber;
	}
}
