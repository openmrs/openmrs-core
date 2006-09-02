package org.openmrs.web.controller.encounter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class EncounterDisplayController implements Controller {

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Map model = new HashMap<String, Object>();
		
		if (context != null && context.isAuthenticated()) {
		
	    	String encounterId = request.getParameter("encounterId");
	    	if (encounterId == null || encounterId.length() == 0)
	    		throw new IllegalArgumentException("encounterId is a required parameter");
	    	
	    	model.put("encounterId", Integer.valueOf(encounterId));
	    	
	    	Encounter encounter = context.getEncounterService().getEncounter(Integer.valueOf(encounterId));
	    	model.put("encounter", encounter);
	    	
			Form form = encounter.getForm();
			List<FormField> fields = new ArrayList<FormField>();
					
			if (form != null) {
				fields = new ArrayList<FormField>(form.getFormFields());
				Collections.sort(fields, new Comparator<FormField>() {
						public int compare(FormField left, FormField right) {
							Integer l = left.getPageNumber();
							if (l == null) {
								l = Integer.MAX_VALUE;
							}
							Integer r = right.getPageNumber();
							if (r == null) {
								r = Integer.MAX_VALUE;
							}
							int temp = l.compareTo(r);
							if (temp == 0) {
								l = left.getFieldNumber();
								if (l == null) {
									l = Integer.MAX_VALUE;
								}
								r = right.getFieldNumber();
								if (r == null) {
									r = Integer.MAX_VALUE;
								}
								temp = l.compareTo(r);
							}
							if (temp == 0) {
								Float lf = left.getSortWeight();
								Float rf = right.getSortWeight();
								temp = OpenmrsUtil.comparewithNullAsGreatest(lf, rf);
							}
							return temp;
						}
					});
			}
	
			Map<FormField, List<Obs>> obsByField = new LinkedHashMap<FormField, List<Obs>>();
			List<Obs> otherObs = new ArrayList<Obs>();
			Map<Concept, FormField> fieldByConcept = new HashMap<Concept, FormField>();
	
			Collection<Object> pageNumbers = new TreeSet<Object>();
			if (form != null) {
				for (FormField f : fields) {
					obsByField.put(f, new ArrayList<Obs>());
					if (f.getField().getConcept() != null)
						fieldByConcept.put(f.getField().getConcept(), f);
					pageNumbers.add(f.getPageNumber() == null ? 0 : f.getPageNumber());
				}
			}
			
			for (Obs o : encounter.getObs()) {
				FormField f = fieldByConcept.get(o.getConcept());
				if (f == null) {
					otherObs.add(o);
				} else {
					obsByField.get(f).add(o);
				}
			}
			
			if(otherObs.size() > 0)
				pageNumbers.add(0);
			
			List<Order> orders = new ArrayList<Order>(encounter.getOrders());
					
			model.put("showBlankFields", "true".equals(request.getParameter("showBlankFields")));
			model.put("pageNumbers", pageNumbers);
			model.put("form", form);
			model.put("fields", fields);
			model.put("obsByField", obsByField);
			model.put("otherObs", otherObs);
			model.put("orders", orders);
			model.put("locale", context.getLocale());
		}
		return new ModelAndView("/encounters/encounterDisplay", "model", model);
	}

}
