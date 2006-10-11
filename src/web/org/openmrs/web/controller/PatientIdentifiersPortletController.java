package org.openmrs.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Location;
import org.openmrs.api.context.Context;

public class PatientIdentifiersPortletController extends PortletController {

	protected void populateModel(HttpServletRequest request, Map model) {
		Map<String, Location> locationNameToId = new HashMap<String, Location>();
		
		List<Location> locations = Context.getPatientService().getLocations();
		for (Location l : locations) {
			locationNameToId.put(l.getName(), l);
		}
		
		model.put("locationsByName", locationNameToId);		
	}
	
}
