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

import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ObsService;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;

/**
 * Observation-related database functions
 * 
 * @see org.openmrs.api.ObsService
 */
public interface ObsDAO {
	
	/**
	 * @see org.openmrs.api.ObsService#saveObs(org.openmrs.Obs, String)
	 */
	public Obs saveObs(Obs obs) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ObsService#getObs(java.lang.Integer)
	 */
	public Obs getObs(Integer obsId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ObsService#purgeObs(Obs)
	 */
	public void deleteObs(Obs obs) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ObsService#getAllMimeTypes(boolean)
	 * @deprecated
	 */
	@Deprecated
	public List<MimeType> getAllMimeTypes(boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ObsService#getMimeType(java.lang.Integer)
	 * @deprecated
	 */
	@Deprecated
	public MimeType getMimeType(Integer mimeTypeId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ObsService#saveMimeType(MimeType)
	 * @deprecated
	 */
	@Deprecated
	public MimeType saveMimeType(MimeType mimeType) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ObsService#purgeMimeType(MimeType)
	 * @deprecated
	 */
	@Deprecated
	public void deleteMimeType(MimeType mimeType) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(java.util.List, java.util.List,
	 *      java.util.List, java.util.List, java.util.List, java.util.List, java.util.List,
	 *      java.lang.Integer, java.lang.Integer, java.util.Date, java.util.Date, boolean,
	 *      java.lang.String)
	 */
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sort,
	        Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate, boolean includeVoidedObs,
	        String accessionNumber) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ObsService#getObservationCount(java.util.List, java.util.List,
	 *      java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Integer,
	 *      java.util.Date, java.util.Date, boolean, java.lang.String)
	 * @see ObsService#getObservationCount(org.openmrs.ConceptName, boolean)
	 */
	public Long getObservationCount(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, Integer obsGroupId,
	        Date fromDate, Date toDate, List<ConceptName> valueCodedNameAnswers, boolean includeVoidedObs,
	        String accessionNumber) throws DAOException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public Obs getObsByUuid(String uuid);
	
}
