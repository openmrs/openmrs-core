/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
