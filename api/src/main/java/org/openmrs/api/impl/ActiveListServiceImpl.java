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

import org.openmrs.Person;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.activelist.ActiveListType;
import org.openmrs.api.APIException;
import org.openmrs.api.ActiveListService;
import org.openmrs.api.db.ActiveListDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the active list service. This class should not be used on its own. The
 * current OpenMRS implementation should be fetched from the Context via
 * <code>Context.getActiveListService()</code>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.ActiveListService
 */
@Transactional
public class ActiveListServiceImpl extends BaseOpenmrsService implements ActiveListService {
	
	private ActiveListDAO dao;
	
	/**
	 * @see org.openmrs.api.ActiveListService#setActiveListDAO(org.openmrs.api.db.ActiveListDAO)
	 */
	public void setActiveListDAO(ActiveListDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.ActiveListService#getActiveListItems(org.openmrs.Person,
	 *      org.openmrs.activelist.ActiveListType)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ActiveListItem> getActiveListItems(Person p, ActiveListType type) throws APIException {
		return dao.getActiveListItems(p, type);
	}
	
	/**
	 * @see org.openmrs.api.ActiveListService#getActiveListItems(java.lang.Class,
	 *      org.openmrs.Person, org.openmrs.activelist.ActiveListType)
	 */
	@Override
	@Transactional(readOnly = true)
	public <T extends ActiveListItem> List<T> getActiveListItems(Class<T> clazz, Person p, ActiveListType type)
	        throws APIException {
		return dao.getActiveListItems(clazz, p, type);
	}
	
	/**
	 * @see org.openmrs.api.ActiveListService#getActiveListItem(java.lang.Class, java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public <T extends ActiveListItem> T getActiveListItem(Class<T> clazz, Integer activeListItemId) throws APIException {
		return dao.getActiveListItem(clazz, activeListItemId);
	}
	
	/**
	 * @see org.openmrs.api.ActiveListService#getActiveListItemByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public ActiveListItem getActiveListItemByUuid(String uuid) throws APIException {
		return dao.getActiveListItemByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ActiveListService#saveActiveListItem(org.openmrs.activelist.ActiveListItem)
	 */
	@Override
	public ActiveListItem saveActiveListItem(ActiveListItem item) throws APIException {
		if (item.getStartDate() == null) {
			item.setStartDate(new Date());
		}
		return dao.saveActiveListItem(item);
	}
	
	/**
	 * @see org.openmrs.api.ActiveListService#removeActiveListItem(org.openmrs.activelist.ActiveListItem,
	 *      java.util.Date)
	 */
	@Override
	public ActiveListItem removeActiveListItem(ActiveListItem item, Date endDate) throws APIException {
		if (item.getEndDate() == null) {
			if (endDate == null) {
				endDate = new Date();
			}
			item.setEndDate(endDate);
		}
		return dao.saveActiveListItem(item);
	}
	
	/**
	 * @see org.openmrs.api.ActiveListService#voidActiveListItem(org.openmrs.activelist.ActiveListItem,
	 *      java.lang.String)
	 */
	@Override
	public ActiveListItem voidActiveListItem(ActiveListItem item, String reason) throws APIException {
		return dao.saveActiveListItem(item);
	}
	
	/**
	 * @see org.openmrs.api.ActiveListService#purgeActiveListItem(org.openmrs.activelist.ActiveListItem)
	 */
	@Override
	public void purgeActiveListItem(ActiveListItem item) throws APIException {
		dao.deleteActiveListItem(item);
	}
}
