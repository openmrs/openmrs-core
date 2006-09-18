package org.openmrs.web.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

public class RegimenPortletController extends PortletController {
	
	protected void populateModel(HttpServletRequest request, Context context, Map<String, Object> model) {
		String drugSetIds = (String) model.get("displayDrugSetIds");
		if ( drugSetIds != null && drugSetIds.length() > 0 ) {
			Map<String, List<DrugOrder>> patientDrugOrderSets = context.getOrderService().getDrugSetsByDrugSetIdList((List<DrugOrder>)model.get("patientDrugOrders"), drugSetIds, ",");
			Map<String, List<DrugOrder>> currentDrugOrderSets = context.getOrderService().getDrugSetsByDrugSetIdList((List<DrugOrder>)model.get("currentDrugOrders"), drugSetIds, ",");
			Map<String, List<DrugOrder>> completedDrugOrderSets = context.getOrderService().getDrugSetsByDrugSetIdList((List<DrugOrder>)model.get("completedDrugOrders"), drugSetIds, ",");
			
			Map<String, Concept> drugOrderHeaders = OpenmrsUtil.delimitedStringToConceptMap(drugSetIds, ",", context);
			
			model.put("patientDrugOrderSets", patientDrugOrderSets);
			model.put("currentDrugOrderSets", currentDrugOrderSets);
			model.put("completedDrugOrderSets", completedDrugOrderSets);
			model.put("drugOrderHeaders", drugOrderHeaders);
		} // else do nothing - we already have orders in the model
	}

}
