package org.openmrs.web.controller;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;

public class RegimenPortletController extends PortletController {
	
	@SuppressWarnings("unchecked")
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		String drugSetIds = (String) model.get("displayDrugSetIds");
		if ( drugSetIds != null && drugSetIds.length() > 0 ) {
			OrderService os = Context.getOrderService();
			Map<String, List<DrugOrder>> patientDrugOrderSets;
			Map<String, List<DrugOrder>> currentDrugOrderSets;
			Map<String, List<DrugOrder>> completedDrugOrderSets;
			
			if ( model.containsKey("patientDrugOrderSets") ) log.debug("pdo already in model");
			else {
				patientDrugOrderSets = os.getDrugSetsByDrugSetIdList((List<DrugOrder>)model.get("patientDrugOrders"), drugSetIds, ",");
				model.put("patientDrugOrderSets", patientDrugOrderSets);
			}
			
			if ( model.containsKey("currentDrugOrderSets") ) log.debug("cdo already in req model");
			else {
				currentDrugOrderSets = os.getDrugSetsByDrugSetIdList((List<DrugOrder>)model.get("currentDrugOrders"), drugSetIds, ",");
				model.put("currentDrugOrderSets", currentDrugOrderSets);
			}
			
			if ( model.containsKey("completedDrugOrderSets") ) log.debug("ldo already in req model");
			else {
				completedDrugOrderSets = os.getDrugSetsByDrugSetIdList((List<DrugOrder>)model.get("completedDrugOrders"), drugSetIds, ",");
				model.put("completedDrugOrderSets", completedDrugOrderSets);
			}

			if ( !model.containsKey("drugOrderHeaders") || !model.containsKey("drugOrderDatePattern")) {
				Map<String, Concept> drugOrderHeaders = OpenmrsUtil.delimitedStringToConceptMap(drugSetIds, ",");
				String datePattern = OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase());

				model.put("drugOrderHeaders", drugOrderHeaders);
				model.put("drugOrderDatePattern", datePattern);
			}
			
		} // else do nothing - we already have orders in the model
	}

}
