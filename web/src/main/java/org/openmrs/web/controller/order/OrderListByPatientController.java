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
package org.openmrs.web.controller.order;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.OrderService.ORDER_STATUS;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class OrderListByPatientController extends SimpleFormController {
	
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
		//binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
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
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		if (Context.isAuthenticated()) {
			String[] orderList = ServletRequestUtils.getStringParameters(request, "orderId");
			OrderService os = Context.getOrderService();
			
			String success = "";
			String error = "";
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String deleted = msa.getMessage("general.deleted");
			String notDeleted = msa.getMessage("general.cannot.delete");
			String ord = msa.getMessage("Order.title");
			String voidReason = ServletRequestUtils.getRequiredStringParameter(request, "voidReason");
			if (!StringUtils.hasLength(voidReason)) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "general.voidReason.empty");
				return showForm(request, response, errors);
			}
			for (String p : orderList) {
				try {
					os.voidOrder(os.getOrder(Integer.valueOf(p)), voidReason);
					if (!success.equals(""))
						success += "<br/>";
					success += ord + " " + p + " " + deleted;
				}
				catch (APIException e) {
					log.warn("Error deleting order", e);
					if (!error.equals(""))
						error += "<br/>";
					error += ord + " " + p + " " + notDeleted;
				}
			}
			
			view = getSuccessView();
			if (ServletRequestUtils.getIntParameter(request, "patientId") != null)
				view += "?patientId=" + ServletRequestUtils.getIntParameter(request, "patientId");
			if (!success.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
			if (!error.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
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
		
		//default empty Object
		List<DrugOrder> orderList = new Vector<DrugOrder>();
		Integer patientId = ServletRequestUtils.getIntParameter(request, "patientId");
		boolean showAll = ServletRequestUtils.getBooleanParameter(request, "showAll", false);
		
		//only fill the Object is the user has authenticated properly
		if (Context.isAuthenticated()) {
			if (patientId != null) {
				// this is the default
				this.setFormView("/admin/orders/orderListByPatient");
				PatientService ps = Context.getPatientService();
				Patient p = ps.getPatient(patientId);
				
				if (p != null) {
					OrderService os = Context.getOrderService();
					orderList = os.getDrugOrdersByPatient(p, ORDER_STATUS.ANY, true);
				} else {
					log.error("Could not get a patient corresponding to patientId [" + patientId
					        + "], thus could not get drug orders.");
					throw new ServletException();
				}
			} else {
				if (showAll) {
					this.setFormView("/admin/orders/orderDrugList");
					OrderService os = Context.getOrderService();
					//orderList = os.getDrugOrders();
					orderList = os.getOrders(DrugOrder.class, null, null, ORDER_STATUS.ANY, null, null, null);
				} else {
					this.setFormView("/admin/orders/choosePatient");
				}
			}
			
		}
		
		return orderList;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		
		Map<String, Object> refData = new HashMap<String, Object>();
		
		// Load international concept names so we can show the correct drug name
		Map<Integer, String> conceptNames = new HashMap<Integer, String>();
		
		List<Order> orderList = (List<Order>) obj;
		
		for (Order order : orderList) {
			Concept c = order.getConcept();
			String cName = c.getBestName(Context.getLocale()).getName();
			conceptNames.put(c.getConceptId(), cName);
		}
		
		refData.put("conceptNames", conceptNames);
		
		// Load information about this patient that we might need
		Integer patientId = ServletRequestUtils.getIntParameter(request, "patientId");
		Patient p = null;
		
		if (Context.isAuthenticated()) {
			if (patientId != null) {
				PatientService ps = Context.getPatientService();
				p = ps.getPatient(patientId);
				
				Set<PersonName> PersonNames = p.getNames();
				Iterator i = PersonNames.iterator();
				PersonName pm = (PersonName) i.next();
				
				refData.put("patient", p);
				refData.put("PersonName", pm);
			}
		}
		
		return refData;
	}
}
