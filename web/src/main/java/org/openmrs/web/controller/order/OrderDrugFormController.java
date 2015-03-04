/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.order;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.DrugEditor;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

public class OrderDrugFormController extends OrderFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Allows for Integers to be used as values in input tags. Normally, only strings and lists are
	 * expected
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		binder.registerCustomEditor(Drug.class, new DrugEditor());
		binder.registerCustomEditor(Double.class, new CustomNumberEditor(Double.class, true));
		binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor(false));
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		String view;
		DrugOrder order = (DrugOrder) obj;
		
		// TODO: for now, orderType will have to be hard-coded?
		if (order.getOrderType() == null) {
			order.setOrderType(Context.getOrderService().getOrderType(OpenmrsConstants.ORDERTYPE_DRUG));
		}
		
		boolean ok = executeCommand(order, request);
		if (ok) {
			int patientId = order.getPatient().getPatientId();
			view = getSuccessView() + "?patientId=" + patientId;
		} else {
			return showForm(request, response, errors);
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		OrderService os = Context.getOrderService();
		
		DrugOrder order = null;
		
		if (Context.isAuthenticated()) {
			Integer orderId = ServletRequestUtils.getIntParameter(request, "orderId");
			if (orderId != null)
				order = (DrugOrder) os.getOrder(orderId);
		}
		
		// if this is a new order, let's see if the user has picked a type yet
		if (order == null) {
			order = new DrugOrder();
			Integer orderTypeId = ServletRequestUtils.getIntParameter(request, "orderTypeId");
			if (orderTypeId != null) {
				OrderType ot = os.getOrderType(orderTypeId);
				order.setOrderType(ot);
			}
			
			Integer patientId = ServletRequestUtils.getIntParameter(request, "patientId");
			if (patientId != null) {
				Patient patient = Context.getPatientService().getPatient(patientId);
				if (patient != null) {
					order.setPatient(patient);
				}
			}
		}
		
		return order;
	}
}
