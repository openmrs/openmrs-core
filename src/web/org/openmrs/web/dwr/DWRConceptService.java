package org.openmrs.web.dwr;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptWord;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.web.Constants;

import uk.ltd.getahead.dwr.ExecutionContext;

public class DWRConceptService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findConcepts(String phrase, List<String> classNames, boolean includeRetired) {

		// List to return
		// Object type gives ability to return error strings
		Vector<Object> objectList = new Vector<Object>();	

		Context context = (Context) ExecutionContext.get().getSession()
				.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		HttpServletRequest request = ExecutionContext.get().getHttpServletRequest();
		
		//TODO add localization for messages
		
		if (context == null) {
			objectList.add("Your session has expired.");
			objectList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			Locale locale = context.getLocale();
			try {
				ConceptService cs = context.getConceptService();
				List<ConceptWord> words = new Vector<ConceptWord>();
				
				if (phrase.matches("\\d+")) {
					// user searched on a number.  Insert concept with corresponding conceptId
					Concept c = cs.getConcept(Integer.valueOf(phrase));
					if (c != null) {
						ConceptWord word = new ConceptWord(phrase, c, locale.getLanguage(), "");
						words.add(word);
					}
				}
							
				if (phrase == null || phrase.equals("")) {
					//TODO get all concepts for testing purposes?
				}
				else {
					//TODO change this search to concept word
					log.debug(locale.getLanguage());
					words.addAll(cs.findConcepts(phrase, locale, includeRetired));
					//concepts.addAll(cs.getConceptByName(phrase));
				}

				if (words.size() == 0) {
					objectList.add("No matches found for <b>" + phrase + "</b>");
				}
				else {
					// TODO speed up this 'search by class' option
					objectList = new Vector<Object>(words.size());
					int maxCount = 200;
					int curCount = 0;
					if (classNames.size() > 0) {
						outer: for (ConceptWord word : words) {
							inner: for (String o : classNames)
								if (o.equals(word.getConcept().getConceptClass().getName())) {
									if ( ++curCount > maxCount ) {
										break outer;
									}
									objectList.add(new ConceptListItem(word));
									//objectList.add(word);
								}
						}
					}
					else {
						for (ConceptWord word : words) {
							if ( ++curCount > maxCount ) {
								break;
							}
							objectList.add(new ConceptListItem(word));
							//objectList.add(word);
						}
					}
				}
			} catch (Exception e) {
				log.error(e);
				e.printStackTrace();
				objectList.add("Error while attempting to find concepts");
			}
		}
		return objectList;
	}
	
	public ConceptListItem getConcept(Integer conceptId) {
		Context context = (Context) ExecutionContext.get().getSession().getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		ConceptService cs = context.getConceptService();
		Concept c = cs.getConcept(conceptId);
		
		return c == null ? null : new ConceptListItem(c);
	}

}
