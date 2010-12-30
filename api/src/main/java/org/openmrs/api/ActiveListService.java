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
package org.openmrs.api;

import java.util.Date;
import java.util.List;

import org.openmrs.Person;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.activelist.ActiveListType;
import org.openmrs.api.db.ActiveListDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains generic methods pertaining to Active Lists in the system
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.Patient
 */
@Transactional
public interface ActiveListService extends OpenmrsService {
	
	/**
	 * Sets the DAO for this service. This is done by DI and Spring. See the
	 * applicationContext-service.xml definition file.
	 * 
	 * @param dao DAO for this service
	 */
	public void setActiveListDAO(ActiveListDAO dao);
	
	/**
	 * Returns a sorted set of active list items based on the type given.
	 * 
	 * @param p the Person
	 * @param type Problem or Allergy
	 * @return sorted set based on the start date
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<ActiveListItem> getActiveListItems(Person p, ActiveListType type) throws APIException;
	
	/**
	 * Returns a sorted set of active list items from a class that extends ActiveListItem
	 * 
	 * @param clazz extends ActiveListItem
	 * @param p the Person
	 * @param type
	 * @return sorted set based on the start date
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public <T extends ActiveListItem> List<T> getActiveListItems(Class<T> clazz, Person p, ActiveListType type)
	        throws APIException;
	
	/**
	 * Returns the ActiveListItem
	 * 
	 * @param clazz extends ActiveListItem
	 * @param activeListItemId the unique ID of the Active List
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public <T extends ActiveListItem> T getActiveListItem(Class<T> clazz, Integer activeListItemId) throws APIException;
	
	/**
	 * Return the ActiveList by the UUID
	 * 
	 * @param uuid unique identifier
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public ActiveListItem getActiveListItemByUuid(String uuid) throws APIException;
	
	/**
	 * Save or update an Active List Item
	 * 
	 * @param item
	 * @return the newly saved item
	 * @throws APIException
	 */
	public ActiveListItem saveActiveListItem(ActiveListItem item) throws APIException;
	
	/**
	 * Sets the Active List Item as inactive by setting the end date to today, if null
	 * 
	 * @param item
	 * @return the newly removed item
	 * @throws APIException
	 */
	public ActiveListItem removeActiveListItem(ActiveListItem item, Date endDate) throws APIException;
	
	/**
	 * Voids the Active List Item
	 * 
	 * @param item
	 * @return the newly voided item
	 * @throws APIException
	 */
	public ActiveListItem voidActiveListItem(ActiveListItem item, String reason) throws APIException;
}
