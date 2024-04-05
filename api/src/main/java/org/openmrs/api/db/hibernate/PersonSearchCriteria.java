/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.AttributeType;
import org.openmrs.util.OpenmrsConstants;

public class PersonSearchCriteria {

	Predicate preparePredicateForAttribute(CriteriaBuilder cb, Join<Patient, Attribute> attributeJoin, 
			Join<Attribute, AttributeType> attributeTypeJoin, String value, Boolean voided, MatchMode matchMode) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.isTrue(attributeTypeJoin.get("searchable")));

		predicates.add(cb.like(cb.lower(attributeJoin.get("value")), matchMode.toLowerCasePattern(value)));

		if (voided == null || !voided) {
			predicates.add(cb.isFalse(attributeJoin.get("voided")));
		}

		return cb.and(predicates.toArray(new Predicate[]{}));
	}
	
	Join<Patient, Attribute> addAliasForAttribute(Join<Encounter, Patient> patientJoin) {
		return patientJoin.join("attributes", JoinType.LEFT);
	}


	Join<Attribute, AttributeType> addAliasForAttributeType(Join<Patient, Attribute> attributeJoin) {
		return attributeJoin.join("attributeType", JoinType.LEFT);
	}

	MatchMode getAttributeMatchMode() {
		AdministrationService adminService = Context.getAdministrationService();
		String matchModeProperty = adminService.getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE, "");
		return (matchModeProperty.equals(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE)) ? MatchMode.ANYWHERE
		        : MatchMode.EXACT;
	}
	
}
