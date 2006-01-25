package org.openmrs.web.dwr;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.web.Util;
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
			Locale locale = Util.getLocale(request);
			try {
				ObsService os = context.getObsService();
				Set<Obs> encs = new HashSet<Obs>();
				
				if (phrase.matches("\\d+")) {
					// user searched on a number.  Insert concept with corresponding obsId
					Obs e = os.getObs(Integer.valueOf(phrase));
					if (e != null) {
						encs.add(e);
					}
				}
							
				if (phrase == null || phrase.equals("")) {
					//TODO get all concepts for testing purposes?
				}
				else {
					encs.addAll(os.findObservations(phrase, includeVoided));
				}

				if (encs.size() == 0) {
					objectList.add("No matches found for <b>" + phrase + "</b>");
				}
				else {
					objectList = new Vector<Object>(encs.size());
					for (Obs e : encs) {
						objectList.add(new ObsListItem(e, locale));
					}
				}
			} catch (Exception e) {
				log.error(e);
				e.printStackTrace();
				objectList.add("Error while attempting to find obs");
			}
		}
		return objectList;
	}

}
