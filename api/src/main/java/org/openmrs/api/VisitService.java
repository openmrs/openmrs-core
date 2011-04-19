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

import java.util.List;

import org.openmrs.VisitType;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service contains methods relating to visits.
 * 
 * @since 1.9
 */
@Transactional
public interface VisitService extends OpenmrsService {
	
	/**
	 * Gets all visit types.
	 * 
	 * @return a list of visit type objects.
	 */
	@Transactional(readOnly = true)
	List<VisitType> getAllVisitTypes();
	
	/**
	 * Gets a visit type by its visit type id.
	 * 
	 * @param visitTypeId the visit type id.
	 * @return the visit type object found with the given id, else null.
	 */
	@Transactional(readOnly = true)
	VisitType getVisitType(Integer visitTypeId);
	
	/**
	 * Gets a visit type by its UUID.
	 * 
	 * @param uuid the visit type UUID.
	 * @return the visit type object found with the given uuid, else null.
	 */
	@Transactional(readOnly = true)
	VisitType getVisitTypeByUuid(String uuid);
	
	/**
	 * Gets all visit types whose names are similar to or contain the given search phrase.
	 * 
	 * @param fuzzySearchPhrase the search phrase to use.
	 * @return a list of all visit types with names similar to or containing the given phrase
	 */
	@Transactional(readOnly = true)
	List<VisitType> getVisitTypes(String fuzzySearchPhrase);
	
	/**
	 * Creates or updates the given visit type in the database.
	 * 
	 * @param visitType the visit type to create or update.
	 * @return the created or updated visit type.
	 */
	VisitType saveVisitType(VisitType visitType);
	
	/**
	 * Retires a given visit type.
	 * 
	 * @param visitType the visit type to retire.
	 * @param reason the reason why the visit type is retired.
	 * @return the visit type that has been retired.
	 */
	VisitType retireVisitType(VisitType visitType, String reason);
	
	/**
	 * Unretires a visit type.
	 * 
	 * @param visitType the visit type to unretire.
	 * @return the unretired visit type
	 */
	VisitType unretireVisitType(VisitType visitType);
	
	/**
	 * Completely removes a visit type from the database. This is not reversible.
	 * 
	 * @param visitType the visit type to delete from the database.
	 */
	void purgeVisitType(VisitType visitType);
}
