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

		if ( fieldGenTag != null ) {
			String initialValue = "";
			OrderType ot = (OrderType)this.fieldGenTag.getVal();
			if ( ot != null ) if ( ot.getOrderTypeId() != null ) initialValue = ot.getOrderTypeId().toString();

			String optionHeader = "";
			if ( this.fieldGenTag.getParameterMap() != null ) {
				optionHeader = (String) this.fieldGenTag.getParameterMap().get("optionHeader");
			}
			if ( optionHeader == null ) optionHeader = "";
			
			//HttpServletRequest request = (HttpServletRequest)this.fieldGenTag.getPageContext().getRequest();
			

			OrderService os = Context.getOrderService();
			List<OrderType> orderTypes = os.getOrderTypes();
			if ( orderTypes == null ) orderTypes = new ArrayList<OrderType>();
			
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
			setParameter("orderTypes", orderTypes);
		}
	}
}
