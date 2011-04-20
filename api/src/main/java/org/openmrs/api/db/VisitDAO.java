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

import org.openmrs.VisitType;
import org.openmrs.api.APIException;

/**
 * Database access functions for visits.
 * 
 * @since 1.9
 */
public interface VisitDAO {
	
	/**
	 * @see org.openmrs.api.VisitService#getAllVisitTypes()
	 */
	List<VisitType> getAllVisitTypes() throws APIException;
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitType(java.lang.Integer)
	 */
	VisitType getVisitType(Integer visitTypeId);
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitTypeByUuid(java.lang.String)
	 */
	VisitType getVisitTypeByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitTypes(java.lang.String)
	 */
	List<VisitType> getVisitTypes(String fuzzySearchPhrase);
	
	/**
	 * @see org.openmrs.api.VisitService#saveVisitType(org.openmrs.VisitType)
	 */
	VisitType saveVisitType(VisitType visitType);
	
	/**
	 * @see org.openmrs.api.VisitService#purgeVisitType(org.openmrs.VisitType)
	 */
	void purgeVisitType(VisitType visitType);
}
