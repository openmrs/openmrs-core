/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.OrderSet;
import org.openmrs.OrderSetAttribute;
import org.openmrs.OrderSetAttributeType;
import org.openmrs.OrderSetMember;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderSetService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.OrderSetDAO;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.springframework.transaction.annotation.Transactional;

public class OrderSetServiceImpl extends BaseOpenmrsService implements OrderSetService {
	
	protected OrderSetDAO dao;
	
	/**
	 * @see org.openmrs.api.OrderSetService#setOrderSetDAO(org.openmrs.api.db.OrderSetDAO)
	 */
	@Override
	public void setOrderSetDAO(OrderSetDAO dao) {
		this.dao = dao;
	}
	
	@Override
	@Transactional(readOnly = false)
	public OrderSet saveOrderSet(OrderSet orderSet) throws APIException {
		return saveOrderSetInternal(orderSet);
	}
	
	/**
	 * @see org.openmrs.api.OrderSetService#retireOrderSet(OrderSet, String)
	 */
	@Override
	@Transactional(readOnly = false)
	public OrderSet retireOrderSet(OrderSet orderSet, String retireReason) throws APIException {
		if (StringUtils.isBlank(retireReason)) {
			throw new IllegalArgumentException("retire reason cannot be empty or null");
		}
		for (OrderSetMember orderSetMember : orderSet.getOrderSetMembers()) {
			orderSet.retireOrderSetMember(orderSetMember);
		}
		return saveOrderSetInternal(orderSet);
	}
	
	/**
	 * @see org.openmrs.api.OrderSetService#unretireOrderSet(OrderSet)
	 */
	@Override
	@Transactional(readOnly = false)
	public OrderSet unretireOrderSet(OrderSet orderSet) throws APIException {
		return saveOrderSetInternal(orderSet);
	}
	
	/**
	 * @see org.openmrs.api.OrderSetService#saveOrderSet(OrderSet)
	 */
	private synchronized OrderSet saveOrderSetInternal(OrderSet orderSet) throws APIException {
		if (CollectionUtils.isEmpty(orderSet.getOrderSetMembers())) {
			// Why do we have to do this?
			CustomDatatypeUtil.saveAttributesIfNecessary(orderSet);
			return dao.save(orderSet);
		}
		for (OrderSetMember orderSetMember : orderSet.getOrderSetMembers()) {
			if (null == orderSetMember.getOrderSet()) {
				orderSetMember.setOrderSet(orderSet);
			}
		}
		for (OrderSetMember orderSetMember : orderSet.getOrderSetMembers()) {
			if (orderSetMember.getRetired()) {
				orderSetMember.setRetiredBy(Context.getAuthenticatedUser());
				orderSetMember.setDateRetired(new Date());
			}
		}
		
		// Why do we have to do this?
		CustomDatatypeUtil.saveAttributesIfNecessary(orderSet);
		return dao.save(orderSet);
	}
	
	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSets(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<OrderSet> getOrderSets(boolean includeRetired) throws APIException {
		return dao.getOrderSets(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSet(Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public OrderSet getOrderSet(Integer orderSetId) throws APIException {
		return dao.getOrderSetById(orderSetId);
	}
	
	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSetByUuid(String)
	 */
	@Override
	@Transactional(readOnly = true)
	public OrderSet getOrderSetByUuid(String orderSetUuid) throws APIException {
		return dao.getOrderSetByUniqueUuid(orderSetUuid);
	}

	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSetMemberByUuid(String)
	 */
	@Override
	@Transactional(readOnly = true)
	public OrderSetMember getOrderSetMemberByUuid(String uuid) {
		return dao.getOrderSetMemberByUuid(uuid);
	}

	/**
	 * @see org.openmrs.api.OrderSetService#getAllOrderSetAttributeTypes()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<OrderSetAttributeType> getAllOrderSetAttributeTypes() {
		return dao.getAllOrderSetAttributeTypes();
	}

	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSetAttributeType(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public OrderSetAttributeType getOrderSetAttributeType(Integer id) {
		return dao.getOrderSetAttributeType(id);
	}

	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSetAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public OrderSetAttributeType getOrderSetAttributeTypeByUuid(String uuid) {
		return dao.getOrderSetAttributeTypeByUuid(uuid);
	}

	/**
	 * @see org.openmrs.api.OrderSetService#saveOrderSetAttributeType(org.openmrs.OrderSetAttributeType)
	 */
	@Override
	@Transactional(readOnly = false)
	public OrderSetAttributeType saveOrderSetAttributeType(OrderSetAttributeType orderSetAttributeType) {
		return dao.saveOrderSetAttributeType(orderSetAttributeType);
	}

	/**
	 * @see org.openmrs.api.OrderSetService#retireOrderSetAttributeType(org.openmrs.OrderSetAttributeType,
	 *      java.lang.String)
	 */
	@Override
	@Transactional(readOnly = false)
	public OrderSetAttributeType retireOrderSetAttributeType(OrderSetAttributeType orderSetAttributeType,
			String reason) {
		return dao.saveOrderSetAttributeType(orderSetAttributeType);
	}

	/**
	 * @see org.openmrs.api.OrderSetService#unretireOrderSetAttributeType(org.openmrs.OrderSetAttributeType)
	 */
	@Override
	@Transactional(readOnly = false)
	public OrderSetAttributeType unretireOrderSetAttributeType(OrderSetAttributeType orderSetAttributeType) {
		return Context.getOrderSetService().saveOrderSetAttributeType(orderSetAttributeType);
	}

	/**
	 * @see org.openmrs.api.OrderSetService#purgeOrderSetAttributeType(org.openmrs.OrderSetAttributeType)
	 */
	@Override
	@Transactional(readOnly = false)
	public void purgeOrderSetAttributeType(OrderSetAttributeType orderSetAttributeType) {
		dao.deleteOrderSetAttributeType(orderSetAttributeType);
	}

	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSetAttributeTypeByName(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public OrderSetAttributeType getOrderSetAttributeTypeByName(String name) {
		return dao.getOrderSetAttributeTypeByName(name);
	}

	/**
	 * @see org.openmrs.api.OrderSetService#getOrderSetAttributeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public OrderSetAttribute getOrderSetAttributeByUuid(String uuid) {
		return dao.getOrderSetAttributeByUuid(uuid);
	}
}
