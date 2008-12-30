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
package org.openmrs.hl7.web.controller;

import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Service;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class Hl7DeletedFormController extends SimpleFormController {
	
	/**
	 * Logger for this class and subclasses
	 */
	protected static final Log log = LogFactory.getLog(Hl7DeletedFormController.class);
	
	/**
	 * Allows for Integers to be used as values in input tags.
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
	}
	
	/**
	 * This is called prior to displaying a form
	 */
	protected List<HL7InArchive> formBackingObject(HttpServletRequest request) throws ServletException {
		List<HL7InArchive> hl7InArchive = new Vector<HL7InArchive>();
		
		// Get all admin deleted messages in the Hl7_in_Archive
		if (Context.isAuthenticated()) {
			hl7InArchive = Context.getHL7Service().getHL7InArchiveByState(HL7Constants.HL7_STATUS_DELETED);
		}
		return hl7InArchive;
	}
	
	/**
	 * This method pushes a message that had been deleted previously back into the queue to be
	 * processed
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
	                                BindException errors) throws Exception {
		HttpSession httpSession = request.getSession();
		String view = getFormView();
		StringBuffer success = new StringBuffer();
		StringBuffer error = new StringBuffer();
		MessageSourceAccessor msa = getMessageSourceAccessor();
		
		String[] queueForm = request.getParameterValues("queueId");
		
		HL7Service hL7Service = Context.getHL7Service();
		
		if (queueForm != null) {
			for (String queueId : queueForm) {
				// Argument to pass to the success/error message
				Object[] args = new Object[] { queueId };
				
				try {
					//Restore Selected Message to the in queue table
					HL7InArchive hl7InArchive = hL7Service.getHL7InArchive(Integer.valueOf(queueId));
					HL7InQueue hl7InQueue = new HL7InQueue(hl7InArchive);
					hL7Service.createHL7InQueue(hl7InQueue);
					
					//Delete selected Message from the archives table
					hL7Service.deleteHL7InArchive(hl7InArchive);
					
					//Display a message for the operation
					success.append(msa.getMessage("Hl7inQueue.queueForm.restored", args) + "<br/>");
				}
				catch (APIException e) {
					log.warn("Error restoring deleted queue item", e);
					error.append(msa.getMessage("Hl7inQueue.queueForm.error", args) + "<br/>");
				}
			}
		}
		
		view = getSuccessView();
		
		if (!success.toString().equals("")) {
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success.toString());
		}
		if (!error.toString().equals("")) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error.toString());
		}
		
		return new ModelAndView(new RedirectView(view));
	}
}
