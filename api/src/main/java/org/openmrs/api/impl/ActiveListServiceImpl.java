/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.activelist.ActiveListType;
import org.openmrs.api.APIException;
import org.openmrs.api.ActiveListService;
import org.openmrs.api.db.ActiveListDAO;

/**
 * Default implementation of the active list service. This class should not be used on its own. The
 * current OpenMRS implementation should be fetched from the Context via
 * <code>Context.getActiveListService()</code>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.ActiveListService
 */
public class ActiveListServiceImpl extends BaseOpenmrsService implements ActiveListService {
	
	private static final Log log = LogFactory.getLog(ActiveListServiceImpl.class);
	
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
	public List<ActiveListItem> getActiveListItems(Person p, ActiveListType type) throws APIException {
		return dao.getActiveListItems(p, type);
	}
	
	/**
	 * @see org.openmrs.api.ActiveListService#getActiveListItems(java.lang.Class,
	 *      org.openmrs.Person, org.openmrs.activelist.ActiveListType)
	 */
	@Override
	public <T extends ActiveListItem> List<T> getActiveListItems(Class<T> clazz, Person p, ActiveListType type)
	                                                                                                           throws APIException {
		return dao.getActiveListItems(clazz, p, type);
	}
	
	/**
	 * @see org.openmrs.api.ActiveListService#getActiveListItem(java.lang.Class, java.lang.Integer)
	 */
	@Override
	public <T extends ActiveListItem> T getActiveListItem(Class<T> clazz, Integer activeListItemId) throws APIException {
		return dao.getActiveListItem(clazz, activeListItemId);
	}
	
	/**
	 * @see org.openmrs.api.ActiveListService#getActiveListItemByUuid(java.lang.String)
	 */
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
}
