package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;

public class DWREncounterService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findEncounters(String phrase, boolean includeVoided) {

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
			}
			else {
				encs.addAll(es.getEncountersByPatientIdentifier(phrase, includeVoided));
			}

			if (encs.size() == 0) {
				objectList.add("No matches found for <b>" + phrase + "</b>");
			}
			else {
				objectList = new Vector<Object>(encs.size());
				for (Encounter e : encs) {
					objectList.add(new EncounterListItem(e));
				}
			}
		} catch (Exception e) {
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
			EncounterService es = Context.getEncounterService();
			List<Location> locations = es.findLocations(searchValue);
			
			locationList = new Vector(locations.size());
			
			for (Location loc : locations) {
				locationList.add(new LocationListItem(loc));
			}
		} catch (Exception e) {
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
			EncounterService es = Context.getEncounterService();
			List<Location> locations = es.getLocations();
			
			locationList = new Vector(locations.size());
			
			for (Location loc : locations) {
				locationList.add(new LocationListItem(loc));
			}
			
		} catch (Exception e) {
			log.error("Error while attempting to get locations", e);
			locationList.add("Error while attempting to get locations - " + e.getMessage());
		}
		
		return locationList;
	}
	
	public LocationListItem getLocation(Integer locationId) {
		EncounterService es = Context.getEncounterService();
		Location l = es.getLocation(locationId);
		return l == null ? null : new LocationListItem(l);
	}
}
