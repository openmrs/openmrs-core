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
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Patient;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.activelist.Allergy;
import org.openmrs.activelist.AllergySeverity;
import org.openmrs.activelist.AllergyType;
import org.openmrs.activelist.Problem;
import org.openmrs.activelist.ProblemModifier;
import org.openmrs.api.context.Context;

/**
 *
 */
public class ActiveListPortletController extends PortletController {
	
	/**
	 * @see org.openmrs.web.controller.PortletController#populateModel(javax.servlet.http.HttpServletRequest,
	 *      java.util.Map)
	 */
	@Override
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		Patient patient = Context.getPatientService().getPatient((Integer) model.get("patientId"));
		model.put("today", Context.getDateFormat().format(new Date()));
		
		String type = (String) model.get("type");
		if ("allergy".equals(type)) {
			List<Allergy> allergies = Context.getPatientService().getAllergies(patient);
			List<List<Allergy>> ls = separate(allergies);
			model.put("allergies", ls.get(0));
			model.put("removedAllergies", ls.get(1));
			model.put("allergyTypes", AllergyType.values());
			model.put("allergySeverities", AllergySeverity.values());
		} else if ("problem".equals(type)) {
			List<Problem> problems = Context.getPatientService().getProblems(patient);
			List<List<Problem>> ls = separate(problems);
			model.put("problems", ls.get(0));
			model.put("removedProblems", ls.get(1));
			model.put("problemModifiers", ProblemModifier.values());
		}
	}
	
	private <T extends ActiveListItem> List<List<T>> separate(List<T> ls) {
		List<T> active = new ArrayList<T>();
		List<T> removed = new ArrayList<T>();
		
		for (T item : ls) {
			if (item.getEndDate() == null) {
				active.add(item);
			} else {
				removed.add(item);
			}
		}
		
		List<List<T>> items = new ArrayList<List<T>>(2);
		items.add(active);
		items.add(removed);
		return items;
	}
}
