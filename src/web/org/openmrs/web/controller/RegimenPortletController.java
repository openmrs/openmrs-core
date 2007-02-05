package org.openmrs.web.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

public class RegimenPortletController extends PortletController {
	
	@SuppressWarnings("unchecked")
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		String drugSetIds = (String) model.get("displayDrugSetIds");
		if ( drugSetIds != null && drugSetIds.length() > 0 ) {
			OrderService os = Context.getOrderService();
			Map<String, List<DrugOrder>> patientDrugOrderSets = os.getDrugSetsByDrugSetIdList((List<DrugOrder>)model.get("patientDrugOrders"), drugSetIds, ",");
			Map<String, List<DrugOrder>> currentDrugOrderSets = os.getDrugSetsByDrugSetIdList((List<DrugOrder>)model.get("currentDrugOrders"), drugSetIds, ",");
			Map<String, List<DrugOrder>> completedDrugOrderSets = os.getDrugSetsByDrugSetIdList((List<DrugOrder>)model.get("completedDrugOrders"), drugSetIds, ",");
			
			Map<String, Concept> drugOrderHeaders = OpenmrsUtil.delimitedStringToConceptMap(drugSetIds, ",");
			
			String datePattern = OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase());

			model.put("patientDrugOrderSets", patientDrugOrderSets);
			model.put("currentDrugOrderSets", currentDrugOrderSets);
			model.put("completedDrugOrderSets", completedDrugOrderSets);
			model.put("drugOrderHeaders", drugOrderHeaders);
			model.put("drugOrderDatePattern", datePattern);
		} // else do nothing - we already have orders in the model
	}

}
