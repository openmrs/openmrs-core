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
			if ( drugSetIds != null && drugSetIds.length() > 0 ) {
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
						Concept drugSet = Context.getConceptService().getConceptByIdOrName(setId);
						Collection<Concept> members = new ArrayList<Concept>();
						if (drugSet != null)
							members = Context.getConceptService().getConceptsInSet(drugSet);
						drugConceptsBySetId.put(setId, members);
					}
				}
				for (DrugOrder order : ((List<DrugOrder>) model.get("patientDrugOrders"))) {
					Concept orderConcept = order.getDrug().getConcept();
					String setIdToUse = null;
					for (Map.Entry<String, Collection<Concept>> e : drugConceptsBySetId.entrySet()) {
						if (e.getValue().contains(orderConcept)) {
							setIdToUse = e.getKey();
							break;
						}
					}
					if (setIdToUse == null && includeOther)
						setIdToUse = "*";
					if (setIdToUse != null) {
						helper(patientDrugOrderSets, setIdToUse, order);
						if (order.isCurrent())
							helper(currentDrugOrderSets, setIdToUse, order);
						else
							helper(completedDrugOrderSets, setIdToUse, order);
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
