/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.Date;
import java.util.List;

import org.openmrs.Person;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.activelist.ActiveListType;
import org.openmrs.api.db.ActiveListDAO;

/**
 * Contains generic methods pertaining to Active Lists in the system
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.Patient
 */
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
	public <T extends ActiveListItem> T getActiveListItem(Class<T> clazz, Integer activeListItemId) throws APIException;
	
	/**
	 * Return the ActiveList by the UUID
	 * 
	 * @param uuid unique identifier
	 * @return
	 * @throws APIException
	 */
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
	
	/**
	 * Completely removes an ActiveListItem from the database (not reversible)
	 * 
	 * @param item the ActiveListItem to completely remove from the database
	 * @throws APIException
	 * @should purge active list item from database
	 * 
	 * @since 1.8.5, 1.9.2
	 */
	public void purgeActiveListItem(ActiveListItem item) throws APIException;
}
