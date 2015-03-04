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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.api.context.Context;

public class RegimenPortletController extends PortletController {
	
	@SuppressWarnings("unchecked")
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		String drugSetIds = (String) model.get("displayDrugSetIds");
		String cachedDrugSetIds = (String) model.get("cachedDrugSetIds");
		if (cachedDrugSetIds == null || !cachedDrugSetIds.equals(drugSetIds)) {
			if (drugSetIds != null && drugSetIds.length() > 0) {
				Map<String, List<DrugOrder>> patientDrugOrderSets = new HashMap<String, List<DrugOrder>>();
				Map<String, List<DrugOrder>> currentDrugOrderSets = new HashMap<String, List<DrugOrder>>();
				Map<String, List<DrugOrder>> completedDrugOrderSets = new HashMap<String, List<DrugOrder>>();
				
				Map<String, Collection<Concept>> drugConceptsBySetId = new LinkedHashMap<String, Collection<Concept>>();
				boolean includeOther = false;
				{
					for (String setId : drugSetIds.split(",")) {
						if ("*".equals(setId)) {
							includeOther = true;
							continue;
						}
						Concept drugSet = Context.getConceptService().getConcept(setId);
						Collection<Concept> members = new ArrayList<Concept>();
						if (drugSet != null)
							members = Context.getConceptService().getConceptsByConceptSet(drugSet);
						drugConceptsBySetId.put(setId, members);
					}
				}
				List<DrugOrder> patientDrugOrders = (List<DrugOrder>) model.get("patientDrugOrders");
				if (patientDrugOrders != null) {
					for (DrugOrder order : patientDrugOrders) {
						String setIdToUse = null;
						if (order.getDrug() != null) {
							Concept orderConcept = order.getDrug().getConcept();
							for (Map.Entry<String, Collection<Concept>> e : drugConceptsBySetId.entrySet()) {
								if (e.getValue().contains(orderConcept)) {
									setIdToUse = e.getKey();
									break;
								}
							}
						}
						if (setIdToUse == null && includeOther)
							setIdToUse = "*";
						if (setIdToUse != null) {
							helper(patientDrugOrderSets, setIdToUse, order);
							if (order.isCurrent() || order.isFuture())
								helper(currentDrugOrderSets, setIdToUse, order);
							else
								helper(completedDrugOrderSets, setIdToUse, order);
						}
					}
				}
				
				model.put("patientDrugOrderSets", patientDrugOrderSets);
				model.put("currentDrugOrderSets", currentDrugOrderSets);
				model.put("completedDrugOrderSets", completedDrugOrderSets);
				
				model.put("cachedDrugSetIds", drugSetIds);
			} // else do nothing - we already have orders in the model
		}
	}
	
	/**
	 * Null-safe version of "drugOrderSets.get(setIdToUse).add(order)"
	 */
	private void helper(Map<String, List<DrugOrder>> drugOrderSets, String setIdToUse, DrugOrder order) {
		List<DrugOrder> list = drugOrderSets.get(setIdToUse);
		if (list == null) {
			list = new ArrayList<DrugOrder>();
			drugOrderSets.put(setIdToUse, list);
		}
		list.add(order);
	}
	
}
