/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.List;

import org.openmrs.Person;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.activelist.ActiveListType;

/**
 * Database methods for the ActiveListService
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.ActiveListService
 */
public interface ActiveListDAO {
	
	/**
	 * @see org.openmrs.api.ActiveListService#getActiveListItems(org.openmrs.Patient,
	 *      org.openmrs.activelist.ActiveListType)
	 */
	public List<ActiveListItem> getActiveListItems(Person p, ActiveListType type) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ActiveListService#getActiveListItems(Class, Person, ActiveListType)
	 */
	public <T extends ActiveListItem> List<T> getActiveListItems(Class<T> clazz, Person p, ActiveListType type)
	        throws DAOException;
	
	/**
	 * @see org.openmrs.api.ActiveListService#getActiveListItem(Class, Integer)
	 */
	public <T extends ActiveListItem> T getActiveListItem(Class<T> clazz, Integer activeListItemId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ActiveListService#getActiveListItemByUuid(String)
	 */
	public ActiveListItem getActiveListItemByUuid(String uuid) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ActiveListService#saveActiveListItem(ActiveListItem)
	 * @see org.openmrs.api.ActiveListService#removeActiveListItem(ActiveListItem, java.util.Date)
	 * @see org.openmrs.api.ActiveListService#voidActiveListItem(ActiveListItem, String)
	 */
	public ActiveListItem saveActiveListItem(ActiveListItem item) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ActiveListService#purgeActiveListItem(org.openmrs.activelist.ActiveListItem)
	 */
	public void deleteActiveListItem(ActiveListItem item) throws DAOException;
}
