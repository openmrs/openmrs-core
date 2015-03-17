/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.validator.RelationshipValidator;
import org.springframework.validation.MapBindingResult;

public class DWRRelationshipService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public String[] createRelationship(Integer personAId, Integer personBId, Integer relationshipTypeId, String startDateStr)
	        throws Exception {
		PersonService ps = Context.getPersonService();
		Person personA = ps.getPerson(personAId);
		Person personB = ps.getPerson(personBId);
		RelationshipType relType = Context.getPersonService().getRelationshipType(relationshipTypeId);
		Relationship rel = new Relationship();
		rel.setPersonA(personA);
		rel.setPersonB(personB);
		rel.setRelationshipType(relType);
		if (StringUtils.isNotBlank(startDateStr)) {
			rel.setStartDate(Context.getDateFormat().parse(startDateStr));
		}
		Map<String, String> map = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		new RelationshipValidator().validate(rel, errors);
		String errmsgs[];
		if (!errors.hasErrors()) {
			ps.saveRelationship(rel);
			errmsgs = null;
			return errmsgs;
		}
		errmsgs = errors.getGlobalError().getCodes();
		return errmsgs;
	}
	
	public void voidRelationship(Integer relationshipId, String voidReason) {
		Context.getPersonService().voidRelationship(Context.getPersonService().getRelationship(relationshipId), voidReason);
	}
	
	public boolean changeRelationshipDates(Integer relationshipId, String startDateStr, String endDateStr) throws Exception {
		Relationship r = Context.getPersonService().getRelationship(relationshipId);
		Date startDate = null;
		if (StringUtils.isNotBlank(startDateStr)) {
			startDate = Context.getDateFormat().parse(startDateStr);
		}
		Date endDate = null;
		if (StringUtils.isNotBlank(endDateStr)) {
			endDate = Context.getDateFormat().parse(endDateStr);
		}
		r.setStartDate(startDate);
		r.setEndDate(endDate);
		Map<String, String> map = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		new RelationshipValidator().validate(r, errors);
		if (errors.hasErrors()) {
			return false;
		} else {
			Context.getPersonService().saveRelationship(r);
			return true;
		}
	}
	
	public Vector<RelationshipListItem> getRelationships(Integer personId, Integer relationshipTypeId) {
		// hack to make sure other relationships aren't still hanging around
		Context.clearSession();
		
		Vector<RelationshipListItem> ret = new Vector<RelationshipListItem>();
		List<Relationship> rels = Context.getPersonService().getRelationshipsByPerson(
		    Context.getPersonService().getPerson(personId));
		for (Relationship rel : rels) {
			if (!rel.isVoided()
			        && (relationshipTypeId == null || rel.getRelationshipType().getRelationshipTypeId().equals(
			            relationshipTypeId))) {
				ret.add(new RelationshipListItem(rel));
			}
		}
		return ret;
	}
	
}
