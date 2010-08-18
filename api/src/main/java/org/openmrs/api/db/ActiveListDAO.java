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
	public <T extends ActiveListItem> T getActiveListItem(Class<T> clazz, Integer activeListItemId)
	                                                                                                         throws DAOException;
	
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
}
