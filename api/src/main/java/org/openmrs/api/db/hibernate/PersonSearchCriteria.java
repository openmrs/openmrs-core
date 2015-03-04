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

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

public class PersonSearchCriteria {
	
	Criterion prepareCriterionForAttribute(String value, MatchMode matchMode) {
		return (prepareCriterionForAttribute(value, null, matchMode));
	}
	
	Criterion prepareCriterionForName(String value) {
		return prepareCriterionForName(value, null);
	}
	
	Criterion prepareCriterionForAttribute(String value, Boolean voided, MatchMode matchMode) {
		if (voided == null || !voided) {
			return Restrictions.conjunction().add(Restrictions.eq("attributeType.searchable", true)).add(
			    Restrictions.eq("attribute.voided", false)).add(Restrictions.ilike("attribute.value", value, matchMode));
		} else {
			return Restrictions.conjunction().add(Restrictions.eq("attributeType.searchable", true)).add(
			    Restrictions.ilike("attribute.value", value, matchMode));
		}
	}
	
	Criterion prepareCriterionForName(String value, Boolean voided) {
		if (voided == null || !voided) {
			return Restrictions.conjunction().add(Restrictions.eq("name.voided", false)).add(
			    Restrictions.disjunction().add(Restrictions.ilike("name.givenName", value, MatchMode.START)).add(
			        Restrictions.ilike("name.middleName", value, MatchMode.START)).add(
			        Restrictions.ilike("name.familyName", value, MatchMode.START)).add(
			        Restrictions.ilike("name.familyName2", value, MatchMode.START)));
		} else {
			return Restrictions.conjunction().add(
			    Restrictions.disjunction().add(Restrictions.ilike("name.givenName", value, MatchMode.START)).add(
			        Restrictions.ilike("name.middleName", value, MatchMode.START)).add(
			        Restrictions.ilike("name.familyName", value, MatchMode.START)).add(
			        Restrictions.ilike("name.familyName2", value, MatchMode.START)));
		}
	}
	
	void addAliasForName(Criteria criteria) {
		criteria.createAlias("names", "name");
	}
	
	void addAliasForAttribute(Criteria criteria) {
		criteria.createAlias("attributes", "attribute", CriteriaSpecification.LEFT_JOIN);
		criteria.createAlias("attribute.attributeType", "attributeType", CriteriaSpecification.LEFT_JOIN);
	}
	
	MatchMode getAttributeMatchMode() {
		AdministrationService adminService = Context.getAdministrationService();
		String matchModeProperty = adminService.getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE, "");
		return (matchModeProperty.equals(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE)) ? MatchMode.ANYWHERE
		        : MatchMode.EXACT;
	}
	
}
