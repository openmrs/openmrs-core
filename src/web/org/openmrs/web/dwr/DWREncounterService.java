package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWREncounterService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findEncounters(String phrase, boolean includeVoided) {

		// List to return
		// Object type gives ability to return error strings
		Vector<Object> objectList = new Vector<Object>();	

		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		if (context == null) {
			objectList.add("Your session has expired.");
			objectList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				EncounterService es = context.getEncounterService();
				List<Encounter> encs = new Vector<Encounter>();
				
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
				log.error(e);
				objectList.add("Error while attempting to find encounter - " + e.getMessage());
			}
		}
		return objectList;
	}
	
	public EncounterListItem getEncounter(Integer conceptId) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		EncounterService es = context.getEncounterService();
		Encounter e = es.getEncounter(conceptId);
		
		return e == null ? null : new EncounterListItem(e);
	}

	@SuppressWarnings("unchecked")
	public Vector findLocations(String searchValue) {
		
		Vector locationList = new Vector();

		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		Context context = (Context) request.getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		if (context == null) {
			locationList.add("Your session has expired.");
			locationList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				FormEntryService fs = context.getFormEntryService();
				List<Location> locations = fs.findLocations(searchValue);
				
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
		}
		return locationList;
	}

	@SuppressWarnings("unchecked")
	public Vector getLocations() {
		
		Vector locationList = new Vector();

		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		Context context = (Context) request.getSession(false)
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		if (context == null) {
			locationList.add("Your session has expired.");
			locationList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				FormEntryService fs = context.getFormEntryService();
				List<Location> locations = fs.getLocations();
				
				locationList = new Vector(locations.size());
				
				for (Location loc : locations) {
					locationList.add(new LocationListItem(loc));
				}
				
			} catch (Exception e) {
				log.error("Error while attempting to get locations", e);
				locationList.add("Error while attempting to get locations - " + e.getMessage());
			}
		}
		return locationList;
	}
	
}
