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
package org.openmrs.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.Cohort;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.Parameter;
import org.openmrs.util.OpenmrsConstants;

public class PatientSetPortletController extends PortletController {
	
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		String attrToUse = (String) model.get("fromAttribute");
		log.debug("model.fromAttribute = " + model.get("fromAttribute"));
		HttpSession httpSession = request.getSession();
		if (httpSession != null) {
			Cohort patientSet = null;
			EvaluationContext evalContext = new EvaluationContext();
			evalContext.addParameterValue(new Parameter("general.user", "Authenticated User", org.openmrs.User.class, null),
			    Context.getAuthenticatedUser());
			if (attrToUse != null) {
				Object o = httpSession.getAttribute(attrToUse);
				if (model.get("mutable") != null) {
					model.put("mutable", model.get("mutable").toString().toLowerCase().startsWith("t"));
				} else {
					model.put("mutable", Boolean.FALSE);
				}
				if (model.get("droppable") != null) {
					model.put("droppable", model.get("droppable").toString().toLowerCase().startsWith("t"));
				} else {
					model.put("droppable", Boolean.FALSE);
				}
				
				if (o instanceof Cohort) {
					patientSet = (Cohort) o;
				} else if (o == null) {
					log.debug("attribute " + attrToUse + " is null");
				} else {
					log.debug("Don't know how to handle " + o.getClass());
				}
			} else {
				throw new IllegalArgumentException("You must specify a fromAttribute when using the patientSet portlet");
			}
			model.put("patientSet", patientSet);
			
			if (Context.isAuthenticated() && !model.containsKey("batchEntryForms")
			        && Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS)) {
				if ("true".equals(model.get("allowBatchEntry"))) {
					Collection<Form> forms = Context.getFormService().getAllForms(false);
					List<Form> shortForms = new ArrayList<Form>();
					int maxBatchEntryFields = 25; //default number
					String maxEntryGlobal = Context.getAdministrationService().getGlobalProperty(
					    "formentry.batch.max_fields");
					if (maxEntryGlobal != null) {
						try {
							maxBatchEntryFields = Integer.parseInt(maxEntryGlobal);
						}
						catch (NumberFormatException nfe) {
							maxBatchEntryFields = 25;
						}
					}
					for (Form form : forms) {
						log.debug("Form " + form.getName() + " has " + form.getFormFields().size() + " fields.");
						if (form.getFormFields().size() < maxBatchEntryFields && !form.isRetired() && form.getPublished())
							shortForms.add(form);
					}
					model.put("batchEntryForms", shortForms);
				}
			}
		}
	}
	
}
