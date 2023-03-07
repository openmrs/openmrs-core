/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs2_3;

import org.apache.commons.lang.StringUtils;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
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

@Component
public class OrderSearchHandler2_3 implements SearchHandler {

	public static final String REQUEST_PARAM_PATIENT = "patient";

	public static final String REQUEST_PARAM_CARE_SETTING = "careSetting";

	public static final String REQUEST_PARAM_CONCEPTS = "concepts";

	public static final String REQUEST_PARAM_ORDER_TYPES = "orderTypes";

	public static final String REQUEST_PARAM_ORDER_NUMBER = "orderNumber";

	public static final String REQUEST_PARAM_ACCESSION_NUMBER = "accessionNumber";

	public static final String REQUEST_PARAM_ACTIVATED_ON_OR_BEFORE_DATE = "activatedOnOrBeforeDate";

	public static final String REQUEST_PARAM_ACTIVATED_ON_OR_AFTER_DATE = "activatedOnOrAfterDate";

	public static final String REQUEST_PARAM_IS_STOPPED = "isStopped";

	public static final String REQUEST_PARAM_AUTO_EXPIRE_ON_OR_BEFORE_DATE = "autoExpireOnOrBeforeDate";

	public static final String REQUEST_PARAM_CANCELED_OR_AUTO_EXPIRE_ON_OR_BEFORE_DATE = "canceledOrExpiredOnOrBeforeDate";

	public static final String REQUEST_PARAM_ACTION = "action";

	public static final String REQUEST_PARAM_FULFILLER_STATUS = "fulfillerStatus";

	public static final String REQUEST_PARAM_INCLUDE_NULL_FULFILLER_STATUS = "includeNullFulfillerStatus";

	public static final String REQUEST_PARAM_EXCLUDE_CANCELED_AND_EXPIRED = "excludeCanceledAndExpired";

	public static final String REQUEST_PARAM_EXCLUDE_DISCONTINUE_ORDERS = "excludeDiscontinueOrders";

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
	        + "date activated (before or after), fulfiller status, action, canceled or expired, stopped, voided flag")
	        .withOptionalParameters(REQUEST_PARAM_PATIENT,
	            REQUEST_PARAM_CARE_SETTING,
	            REQUEST_PARAM_CONCEPTS,
	            REQUEST_PARAM_ORDER_TYPES,
	            REQUEST_PARAM_ORDER_NUMBER,
	            REQUEST_PARAM_ACCESSION_NUMBER,
	            REQUEST_PARAM_ACTIVATED_ON_OR_BEFORE_DATE,
	            REQUEST_PARAM_ACTIVATED_ON_OR_AFTER_DATE,
	            REQUEST_PARAM_IS_STOPPED,
	            REQUEST_PARAM_AUTO_EXPIRE_ON_OR_BEFORE_DATE,
	            REQUEST_PARAM_CANCELED_OR_AUTO_EXPIRE_ON_OR_BEFORE_DATE,
	            REQUEST_PARAM_ACTION,
	            REQUEST_PARAM_FULFILLER_STATUS,
	            REQUEST_PARAM_INCLUDE_NULL_FULFILLER_STATUS,
	            REQUEST_PARAM_EXCLUDE_CANCELED_AND_EXPIRED,
	            REQUEST_PARAM_EXCLUDE_DISCONTINUE_ORDERS,
	            REQUEST_PARAM_INCLUDE_VOIDED).build();

	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1
	        + "/order", Collections.singletonList("2.3.* - 9.*"), searchQuery);

	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.SearchHandler#getSearchConfig()
	 */
	@Override
	public SearchConfig getSearchConfig() {
		return searchConfig;
	}

	/**
	 * @see SearchHandler#search(RequestContext)
	 */
	@Override
	public PageableResult search(RequestContext context) throws ResponseException {
		// get input parameters
		String patientUuid = context.getParameter(REQUEST_PARAM_PATIENT);
		String careSettingUuid = context.getParameter(REQUEST_PARAM_CARE_SETTING);
		String conceptUuids = context.getParameter(REQUEST_PARAM_CONCEPTS);
		String orderTypeUuids = context.getParameter(REQUEST_PARAM_ORDER_TYPES);
		String orderNumber = context.getParameter(REQUEST_PARAM_ORDER_NUMBER);
		String accessionNumber = context.getParameter(REQUEST_PARAM_ACCESSION_NUMBER);
		String activatedOnOrBeforeDateStr = context.getParameter(REQUEST_PARAM_ACTIVATED_ON_OR_BEFORE_DATE);
		String activatedOnOrAfterDateStr = context.getParameter(REQUEST_PARAM_ACTIVATED_ON_OR_AFTER_DATE);
		String isStoppedStr = context.getParameter(REQUEST_PARAM_IS_STOPPED);
		String autoExpireOnOrBeforeDateStr = context.getParameter(REQUEST_PARAM_AUTO_EXPIRE_ON_OR_BEFORE_DATE);
		String canceledOrExpiredOnOrBeforeDateStr = context
		        .getParameter(REQUEST_PARAM_CANCELED_OR_AUTO_EXPIRE_ON_OR_BEFORE_DATE);
		String actionStr = context.getParameter(REQUEST_PARAM_ACTION);
		String fulfillerStatusStr = context.getParameter(REQUEST_PARAM_FULFILLER_STATUS);
		String includeNullFulfillerStatusStr = context.getParameter(REQUEST_PARAM_INCLUDE_NULL_FULFILLER_STATUS);
		String excludeDiscontinueOrdersStr = context.getParameter(REQUEST_PARAM_EXCLUDE_DISCONTINUE_ORDERS);
		String excludeCanceledAndExpiredStr = context.getParameter(REQUEST_PARAM_EXCLUDE_CANCELED_AND_EXPIRED);
		String includeVoidedStr = context.getParameter(REQUEST_PARAM_INCLUDE_VOIDED);

		// build search criteria for order service
		boolean includeVoided = StringUtils.isNotBlank(includeVoidedStr) ? Boolean.parseBoolean(includeVoidedStr) : false;
		// by default the Canceled(dateStopped != null) and Expired(autoExpire < today) orders are excluded
		boolean excludeDiscontinueOrders = StringUtils.isNotBlank(excludeDiscontinueOrdersStr) ? Boolean
		        .parseBoolean(excludeDiscontinueOrdersStr) : false;
		boolean excludeCanceledAndExpired = StringUtils.isNotBlank(excludeCanceledAndExpiredStr) ? Boolean
		        .parseBoolean(excludeCanceledAndExpiredStr) : false;
		boolean isStopped = StringUtils.isNotBlank(isStoppedStr) ? Boolean.parseBoolean(isStoppedStr) : false;

		Date activatedOnOrBeforeDate = StringUtils.isNotBlank(activatedOnOrBeforeDateStr) ?
		        (Date) ConversionUtil.convert(activatedOnOrBeforeDateStr, Date.class) : null;
		Date activatedOnOrAfterDate = StringUtils.isNotBlank(activatedOnOrAfterDateStr) ?
		        (Date) ConversionUtil.convert(activatedOnOrAfterDateStr, Date.class) : null;
		Date autoExpireOnOrBeforeDate = StringUtils.isNotBlank(autoExpireOnOrBeforeDateStr) ?
		        (Date) ConversionUtil.convert(autoExpireOnOrBeforeDateStr, Date.class) : null;
		Date canceledOrExpiredOnOrBeforeDate = StringUtils.isNotBlank(canceledOrExpiredOnOrBeforeDateStr) ?
		        (Date) ConversionUtil.convert(canceledOrExpiredOnOrBeforeDateStr, Date.class) : null;
		Order.Action action = StringUtils.isNotBlank(actionStr) ? Order.Action
		        .valueOf(actionStr) : null;
		Order.FulfillerStatus fulfillerStatus = StringUtils.isNotBlank(fulfillerStatusStr) ? Order.FulfillerStatus
		        .valueOf(fulfillerStatusStr) : null;
		Boolean includeNullFulfillerStatus = StringUtils.isNotBlank(includeNullFulfillerStatusStr) ? new Boolean(
		        includeNullFulfillerStatusStr) : null;
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
			if (concepts.size() == 0) {
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
			if (orderTypes.isEmpty()) {
				throw new ObjectNotFoundException();
			}
		}

		OrderSearchCriteriaBuilder builder = new OrderSearchCriteriaBuilder();
		OrderSearchCriteria orderSearchCriteria = builder
		        .setPatient(patient)
		        .setCareSetting(careSetting)
		        .setConcepts(concepts)
		        .setOrderTypes(orderTypes)
                .setOrderNumber(StringUtils.isNotEmpty(orderNumber) ? orderNumber : null)
                .setAccessionNumber(StringUtils.isNotEmpty(accessionNumber) ? accessionNumber : null)
		        .setActivatedOnOrBeforeDate(activatedOnOrBeforeDate)
		        .setActivatedOnOrAfterDate(activatedOnOrAfterDate)
		        .setIsStopped(isStopped)
		        .setAutoExpireOnOrBeforeDate(autoExpireOnOrBeforeDate)
		        .setCanceledOrExpiredOnOrBeforeDate(canceledOrExpiredOnOrBeforeDate)
		        .setAction(action)
		        .setFulfillerStatus(fulfillerStatus)
		        .setIncludeNullFulfillerStatus(includeNullFulfillerStatus)
		        .setExcludeDiscontinueOrders(excludeDiscontinueOrders)
		        .setExcludeCanceledAndExpired(excludeCanceledAndExpired)
		        .setIncludeVoided(includeVoided)
		        .build();

		// invoke order service and return results
		List<Order> orders = orderService.getOrders(orderSearchCriteria);

		return new NeedsPaging<Order>(orders, context);
	}

}
