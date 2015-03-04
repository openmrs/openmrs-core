/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib.fieldgen;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.OrderType;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;

public class OrderTypeHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
	private String defaultUrl = "orderType.field";
	
	public void run() {
		setUrl(defaultUrl);
		
		if (fieldGenTag != null) {
			String initialValue = "";
			OrderType ot = (OrderType) this.fieldGenTag.getVal();
			if (ot != null)
				if (ot.getOrderTypeId() != null)
					initialValue = ot.getOrderTypeId().toString();
			
			String optionHeader = "";
			if (this.fieldGenTag.getParameterMap() != null) {
				optionHeader = (String) this.fieldGenTag.getParameterMap().get("optionHeader");
			}
			if (optionHeader == null)
				optionHeader = "";
			
			//HttpServletRequest request = (HttpServletRequest)this.fieldGenTag.getPageContext().getRequest();
			
			OrderService os = Context.getOrderService();
			List<OrderType> orderTypes = os.getAllOrderTypes();
			if (orderTypes == null)
				orderTypes = new ArrayList<OrderType>();
			
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
			setParameter("orderTypes", orderTypes);
		}
	}
}
