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

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;

public class DWREncounterService {
	
	private static final Log log = LogFactory.getLog(DWREncounterService.class);
	
	public Vector findEncounters(String phrase, boolean includeVoided) throws APIException {
		
		// List to return
		// Object type gives ability to return error strings
		Vector<Object> objectList = new Vector<Object>();
		
		try {
			EncounterService es = Context.getEncounterService();
			List<Encounter> encs = new Vector<Encounter>();
			
			if (phrase == null) {
				objectList.add("Search phrase cannot be null");
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
				encs.addAll(es.getEncountersByPatient(phrase));
			}
			
			if (encs.size() == 0) {
				objectList.add("No matches found for <b>" + phrase + "</b>");
			} else {
				objectList = new Vector<Object>(encs.size());
				for (Encounter e : encs) {
					objectList.add(new EncounterListItem(e));
				}
			}
		}
		catch (Exception e) {
			log.error("Error while searching for encounters", e);
			objectList.add("Error while attempting to find encounter - " + e.getMessage());
		}
		return objectList;
	}
	
	public EncounterListItem getEncounter(Integer encounterId) {
		EncounterService es = Context.getEncounterService();
		Encounter e = es.getEncounter(encounterId);
		
		return e == null ? null : new EncounterListItem(e);
	}
	
	@SuppressWarnings("unchecked")
	public Vector findLocations(String searchValue) {
		
		Vector locationList = new Vector();
		
		try {
			LocationService ls = Context.getLocationService();
			List<Location> locations = ls.getLocations(searchValue);
			
			locationList = new Vector(locations.size());
			
			for (Location loc : locations) {
				locationList.add(new LocationListItem(loc));
			}
		}
		catch (Exception e) {
			log.error(e);
			locationList.add("Error while attempting to find locations - " + e.getMessage());
		}
		
		if (locationList.size() == 0) {
			locationList.add("No locations found. Please search again.");
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
			locationList.add("Error while attempting to get locations - " + e.getMessage());
		}
		
		return locationList;
	}
	
	public LocationListItem getLocation(Integer locationId) {
		LocationService ls = Context.getLocationService();
		Location l = ls.getLocation(locationId);
		return l == null ? null : new LocationListItem(l);
	}
}
