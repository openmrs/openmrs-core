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
package org.openmrs.web.dwr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;

public class DWREncounterService {
	
	private static final Log log = LogFactory.getLog(DWREncounterService.class);
	
	/**
	 * Returns a list of encounters for patients with a matching name, identifier or encounterId if
	 * phrase is a number.
	 * 
	 * @param phrase patient name or identifier
	 * @param includeVoided Specifies if voided encounters should be included or not
	 * @return list of the matching encounters
	 * @throws APIException
	 */
	public Vector findEncounters(String phrase, boolean includeVoided) throws APIException {
		
		return findBatchOfEncounters(phrase, includeVoided, null, null);
	}
	
	/**
	 * Returns a list of matching encounters (depending on values of start and length parameters) if
	 * the length parameter is not specified, then all matches will be returned from the start index
	 * if specified.
	 * 
	 * @param phrase patient name or identifier
	 * @param includeVoided Specifies if voided encounters should be included or not
	 * @param start the beginning index
	 * @param length the number of matching encounters to return
	 * @return list of the matching encounters
	 * @throws APIException
	 */
	public Vector findBatchOfEncounters(String phrase, boolean includeVoided, Integer start, Integer length)
	                                                                                                        throws APIException {
		
		// List to return
		// Object type gives ability to return error strings
		Vector<Object> objectList = new Vector<Object>();
		MessageSourceService mss = Context.getMessageSourceService();
		
		try {
			EncounterService es = Context.getEncounterService();
			List<Encounter> encs = new Vector<Encounter>();
			
			if (phrase == null) {
				objectList.add(mss.getMessage("Encounter.searchPhraseCannotBeNull"));
				return objectList;
			}
			
			if (phrase.matches("\\d+")) {
				// user searched on a number.  Insert concept with corresponding encounterId
				Encounter e = es.getEncounter(Integer.valueOf(phrase));
				if (e != null) {
					if (!e.isVoided() || includeVoided == true)
						encs.add(e);
				}
			}
			
			if (phrase == null || phrase.equals("")) {
				//TODO get all concepts for testing purposes?
			} else {
				encs.addAll(es.getEncounters(phrase, start, length, includeVoided));
			}
			
			if (encs.size() == 0) {
				objectList.add(mss.getMessage("Encounter.noMatchesFound", new Object[] { phrase }, Context.getLocale()));
			} else {
				objectList = new Vector<Object>(encs.size());
				for (Encounter e : encs) {
					objectList.add(new EncounterListItem(e));
				}
			}
		}
		catch (Exception e) {
			log.error("Error while searching for encounters", e);
			objectList.add(mss.getMessage("Encounter.search.error") + " - " + e.getMessage());
		}
		return objectList;
	}
	
	/**
	 * Returns a map of results with the values as count of matches and a partial list of the
	 * matching encounters (depending on values of start and length parameters) while the keys are
	 * are 'count' and 'objectList' respectively, if the length parameter is not specified, then all
	 * matches will be returned from the start index if specified.
	 * 
	 * @param phrase patient name or identifier
	 * @param includeVoided Specifies if voided encounters should be included or not
	 * @param start the beginning index
	 * @param length the number of matching encounters to return
	 * @return a map of results
	 * @throws APIException
	 * @since 1.8
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> findCountAndEncounters(String phrase, boolean includeVoided, Integer start, Integer length,
	                                                  boolean getMatchCount) throws APIException {
		//Map to return
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		Vector<Object> objectList = new Vector<Object>();
		try {
			EncounterService es = Context.getEncounterService();
			int encounterCount = 0;
			if (getMatchCount) {
				encounterCount += es.getCountOfEncounters(phrase, includeVoided);
				if (phrase.matches("\\d+")) {
					// user searched on a number
					Encounter e = es.getEncounter(Integer.valueOf(phrase));
					if (e != null) {
						if (!e.isVoided() || includeVoided == true)
							encounterCount++;
					}
				}
			}
			
			if (encounterCount > 0 || !getMatchCount)
				objectList = findBatchOfEncounters(phrase, includeVoided, start, length);
			
			resultsMap.put("count", encounterCount);
			resultsMap.put("objectList", objectList);
		}
		catch (Exception e) {
			log.error("Error while searching for encounters", e);
			objectList.clear();
			objectList.add(Context.getMessageSourceService().getMessage("Encounter.search.error") + " - " + e.getMessage());
			resultsMap.put("count", 0);
			resultsMap.put("objectList", objectList);
			resultsMap.put("errorMsg", Context.getMessageSourceService().getMessage("Encounter.search.error"));
		}
		return resultsMap;
	}
	
	public EncounterListItem getEncounter(Integer encounterId) {
		EncounterService es = Context.getEncounterService();
		Encounter e = es.getEncounter(encounterId);
		
		return e == null ? null : new EncounterListItem(e);
	}
	
	public Vector findLocations(String searchValue) {
		
		return findBatchOfLocations(searchValue, null, null);
	}
	
	/**
	 * Returns a list of matching locations (depending on values of start and length parameters) if
	 * the length parameter is not specified, then all matches will be returned from the start index
	 * if specified.
	 * 
	 * @param searchValue is the string used to search for locations
	 * @param start the beginning index
	 * @param length the number of matching locations to return
	 * @return list of the matching locations
	 * @throws APIException
	 */
	public Vector<Object> findBatchOfLocations(String searchValue, Integer start, Integer length) throws APIException {
		
		Vector<Object> locationList = new Vector<Object>();
		MessageSourceService mss = Context.getMessageSourceService();
		
		try {
			LocationService ls = Context.getLocationService();
			List<Location> locations = ls.getLocations(searchValue, start, length);
			locationList = new Vector<Object>(locations.size());
			
			for (Location loc : locations) {
				locationList.add(new LocationListItem(loc));
			}
		}
		catch (Exception e) {
			log.error(e);
			locationList.add(mss.getMessage("Location.search.error") + " - " + e.getMessage());
		}
		
		if (locationList.size() == 0) {
			locationList.add(mss.getMessage("Location.noLocationsFound"));
		}
		
		return locationList;
	}
	
	@SuppressWarnings("unchecked")
	public Vector getLocations() {
		
		Vector locationList = new Vector();
		
		try {
			LocationService ls = Context.getLocationService();
			List<Location> locations = ls.getAllLocations();
			
			locationList = new Vector(locations.size());
			
			for (Location loc : locations) {
				locationList.add(new LocationListItem(loc));
			}
			
		}
		catch (Exception e) {
			log.error("Error while attempting to get locations", e);
			locationList.add(Context.getMessageSourceService().getMessage("Location.get.error") + " - " + e.getMessage());
		}
		
		return locationList;
	}
	
	public LocationListItem getLocation(Integer locationId) {
		LocationService ls = Context.getLocationService();
		Location l = ls.getLocation(locationId);
		return l == null ? null : new LocationListItem(l);
	}
	
	/**
	 * Returns a map of results with the values as count of matches and a partial list of the
	 * matching locations (depending on values of start and length parameters) while the keys are
	 * are 'count' and 'objectList' respectively, if the length parameter is not specified, then all
	 * matches will be returned from the start index if specified.
	 * 
	 * @param searchValue is the string used to search for locations
	 * @param start the beginning index
	 * @param length the number of matching encounters to return
	 * @param getMatchCount Specifies if the count of matches should be included in the returned map
	 * @return a map of results
	 * @throws APIException
	 * @since 1.8
	 */
	public Map<String, Object> findCountAndLocations(String phrase, Integer start, Integer length, boolean getMatchCount)
	                                                                                                                     throws APIException {
		
		//Map to return
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		Vector<Object> objectList = new Vector<Object>();
		try {
			LocationService es = Context.getLocationService();
			int locationCount = 0;
			if (getMatchCount)
				locationCount += es.getCountOfLocations(phrase, true);
			
			if (locationCount > 0 || !getMatchCount)
				objectList = findBatchOfLocations(phrase, start, length);
			
			resultsMap.put("count", locationCount);
			resultsMap.put("objectList", objectList);
		}
		catch (Exception e) {
			log.error("Error while searching for locations", e);
			objectList.clear();
			objectList.add(Context.getMessageSourceService().getMessage("Location.search.error") + " - " + e.getMessage());
			resultsMap.put("count", 0);
			resultsMap.put("objectList", objectList);
		}
		return resultsMap;
	}
}
