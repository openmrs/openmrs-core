package org.openmrs.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.api.context.Context;

public class RegimenPortletController extends PortletController {
	
	protected void populateModel(HttpServletRequest request, Context context, Map<String, Object> model) {
		String drugSetIds = (String) model.get("displayDrugSetIds");
		if ( drugSetIds != null && drugSetIds.length() > 0 ) {
			List<Concept> drugSetList = null;
			String[] drugSets = drugSetIds.split(",");
			for (String drugSet : drugSets) {
				try {
					Integer drugSetId = new Integer(drugSet);
					Concept drugSetConcept = context.getConceptService().getConcept(drugSetId);
					if ( drugSetConcept != null ) {
						if ( drugSetList == null ) drugSetList = new ArrayList<Concept>();
						drugSetList.add(drugSetConcept);
					} else {
						log.debug("DrugSet Concept with ID " + drugSetId.toString() + " was null");
					}
				} catch (NumberFormatException nfe) {
					log.debug("Could not read ID number " + drugSet + " as an integer");
				}
			}
			model.put("patientDrugOrderSets", context.getOrderService().getDrugSetsByConcepts((List<DrugOrder>) model.get("patientDrugOrders"), drugSetList));
		} // do nothing
	}

}
