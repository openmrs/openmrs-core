package org.openmrs.web.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;

public class PersonRelationshipsPortletController extends PortletController {

	protected void populateModel(HttpServletRequest request, Map model) {
		List<RelationshipType> relationshipTypes = Context.getPersonService().getRelationshipTypes();
		model.put("relationshipTypes", relationshipTypes);
	}

	
}
