package org.openmrs.web.dwr;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRObsService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findObs(String phrase, boolean includeVoided) {
		
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
				Set<Encounter> encs = new HashSet<Encounter>();
				
				/*
				if (phrase.matches("\\d+")) {
					// user searched on a number.  Insert obs with corresponding obsId
					Obs e = os.getObs(Integer.valueOf(phrase));
					if (e != null) {
						encs.add(e);
					}
				}
				*/
				
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
				objectList.add("Error while attempting to find obs - " + e.getMessage());
			}
		}
		return objectList;
	}

}
