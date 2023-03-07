/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs2_2;

import org.apache.commons.lang.StringUtils;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.parameter.OrderSearchCriteria;
import org.openmrs.parameter.OrderSearchCriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Returns orders based on search by patient, care setting, concepts, order types, activated date
 * range, and voided orders
 */
@Component
public class OrderSearchHandler2_2 implements SearchHandler {
	
	public static final String REQUEST_PARAM_PATIENT = "patient";
	
	public static final String REQUEST_PARAM_CARE_SETTING = "careSetting";
	
	public static final String REQUEST_PARAM_CONCEPTS = "concepts";
	
	public static final String REQUEST_PARAM_ORDER_TYPES = "orderTypes";
	
	public static final String REQUEST_PARAM_ACTIVATED_ON_OR_BEFORE_DATE = "activatedOnOrBeforeDate";
	
	public static final String REQUEST_PARAM_ACTIVATED_ON_OR_AFTER_DATE = "activatedOnOrAfterDate";
	
	public static final String REQUEST_PARAM_INCLUDE_VOIDED = "includeVoided";
	
	@Autowired
	@Qualifier("patientService")
	PatientService patientService;
	
	@Autowired
	@Qualifier("orderService")
	OrderService orderService;
	
	@Autowired
	@Qualifier("conceptService")
	ConceptService conceptService;
	
	SearchQuery searchQuery = new SearchQuery.Builder("Allows you to search for orders, it matches on "
	        + "patient, care setting, concepts (comma delimited), order types (comma delimited), "
	        + "date activated (before or after), voided flag")
	        .withOptionalParameters(REQUEST_PARAM_PATIENT,
	            REQUEST_PARAM_CARE_SETTING,
	            REQUEST_PARAM_CONCEPTS,
	            REQUEST_PARAM_ORDER_TYPES,
	            REQUEST_PARAM_ACTIVATED_ON_OR_BEFORE_DATE,
	            REQUEST_PARAM_ACTIVATED_ON_OR_AFTER_DATE,
	            REQUEST_PARAM_INCLUDE_VOIDED).build();
	
	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1
	        + "/order", Collections.singletonList("2.2.*"), searchQuery);
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.SearchHandler#getSearchConfig()
	 */
	@Override
	public SearchConfig getSearchConfig() {
		return searchConfig;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.SearchHandler#search(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public PageableResult search(RequestContext context) throws ResponseException {
		// get input parameters
		String patientUuid = context.getParameter("patient");
		String careSettingUuid = context.getParameter("careSetting");
		String conceptUuids = context.getParameter("concepts");
		String orderTypeUuids = context.getParameter("orderTypes");
		String activatedOnOrBeforeDateStr = context.getParameter("activatedOnOrBeforeDate");
		String activatedOnOrAfterDateStr = context.getParameter("activatedOnOrAfterDate");
		String includeVoidedStr = context.getParameter("includeVoided");
		
		// build search criteria for order service
		boolean includeVoided = StringUtils.isNotBlank(includeVoidedStr) ? Boolean.parseBoolean(includeVoidedStr) : false;
		Date activatedOnOrBeforeDate = StringUtils.isNotBlank(activatedOnOrBeforeDateStr) ?
		        (Date) ConversionUtil.convert(activatedOnOrBeforeDateStr, Date.class) : null;
		Date activatedOnOrAfterDate = StringUtils.isNotBlank(activatedOnOrAfterDateStr) ?
		        (Date) ConversionUtil.convert(activatedOnOrAfterDateStr, Date.class) : null;
		List<Concept> concepts = null;
		List<OrderType> orderTypes = null;
		
		Patient patient = null;
		if (StringUtils.isNotBlank(patientUuid)) {
			patient = patientService.getPatientByUuid(patientUuid);
			if (patient == null) {
				throw new ObjectNotFoundException();
			}
		}
		
		CareSetting careSetting = null;
		if (StringUtils.isNotBlank(careSettingUuid)) {
			careSetting = orderService.getCareSettingByUuid(careSettingUuid);
			if (careSetting == null) {
				throw new ObjectNotFoundException();
			}
		}
		
		// if none of the uuids are found, throw an exception
		if (StringUtils.isNotBlank(conceptUuids)) {
			Concept concept = null;
			concepts = new ArrayList<Concept>();
			for (String conceptUuid : conceptUuids.split(",")) {
				if (!conceptUuid.trim().equalsIgnoreCase("")) {
					concept = conceptService.getConceptByUuid(conceptUuid);
					if (concept != null) {
						concepts.add(concept);
					}
				}
			}
			if (concepts.isEmpty()) {
				throw new ObjectNotFoundException();
			}
		}
		
		// if none of the uuids are found, throw an exception
		if (StringUtils.isNotBlank(orderTypeUuids)) {
			OrderType orderType = null;
			orderTypes = new ArrayList<OrderType>();
			for (String orderTypeUuid : orderTypeUuids.split(",")) {
				if (!orderTypeUuid.trim().equalsIgnoreCase("")) {
					orderType = orderService.getOrderTypeByUuid(orderTypeUuid);
					if (orderType != null) {
						orderTypes.add(orderType);
					}
				}
			}
			if (orderTypes.size() == 0) {
				throw new ObjectNotFoundException();
			}
		}
		
		OrderSearchCriteriaBuilder builder = new OrderSearchCriteriaBuilder();
		OrderSearchCriteria orderSearchCriteria = builder
		        .setPatient(patient)
		        .setCareSetting(careSetting)
		        .setConcepts(concepts)
		        .setOrderTypes(orderTypes)
		        .setActivatedOnOrBeforeDate(activatedOnOrBeforeDate)
		        .setActivatedOnOrAfterDate(activatedOnOrAfterDate)
		        .setIncludeVoided(includeVoided)
		        .build();
		
		// invoke order service and return results
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		
		return new NeedsPaging<Order>(orders, context);
	}
	
}
