package org.openmrs.web.dwr;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.util.Helper;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRConceptService {

	protected final Log log = LogFactory.getLog(getClass());

	public Vector findConcepts(String phrase, List<String> classNames,
			boolean includeRetired, List<String> ignoreClassNames) {

		// List to return
		// Object type gives ability to return error strings
		Vector<Object> objectList = new Vector<Object>();

		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		HttpServletRequest request = WebContextFactory.get()
				.getHttpServletRequest();

		// TODO add localization for messages

		if (context == null) {
			objectList.add("Your session has expired.");
			objectList.add("Please <a href='" + request.getContextPath()
					+ "/logout'>log in</a> again.");
		} else {
			Integer userId = -1;
			User u = context.getAuthenticatedUser();
			if (u != null)
				userId = u.getUserId();

			log.info(userId + "|" + phrase + "|" + classNames.toString());

			Locale locale = context.getLocale();
			if (classNames == null)
				classNames = new Vector<String>();
			if (ignoreClassNames == null)
				ignoreClassNames = new Vector<String>();
			try {
				ConceptService cs = context.getConceptService();
				List<ConceptWord> words = new Vector<ConceptWord>();

				if (phrase.matches("\\d+")) {
					// user searched on a number. Insert concept with
					// corresponding conceptId
					Concept c = cs.getConcept(Integer.valueOf(phrase));
					if (c != null) {
						ConceptWord word = new ConceptWord(phrase, c, locale
								.getLanguage(), "Concept Id #" + phrase);
						words.add(word);
					}
				}

				if (phrase == null || phrase.equals("")) {
					// TODO get all concepts for testing purposes?
				} else {
					words.addAll(cs
							.findConcepts(phrase, locale, includeRetired));
				}

				if (words.size() == 0) {
					objectList
							.add("No matches found for <b>" + phrase + "</b>");
				} else {
					// TODO speed up this 'search by class' option
					objectList = new Vector<Object>(words.size());
					int maxCount = 500;
					int curCount = 0;
					if (classNames.size() > 0) {
						outer: for (ConceptWord word : words) {
							inner: for (String o : classNames)
								if (o.equals(word.getConcept()
										.getConceptClass().getName())) {
									if (++curCount > maxCount) {
										break outer;
									}
									objectList.add(new ConceptListItem(word));
								}
						}
					} else if (ignoreClassNames.size() > 0) {
						outer: for (ConceptWord word : words) {
							inner: for (String o : ignoreClassNames)
								if (!o.equals(word.getConcept()
										.getConceptClass().getName())) {
									if (++curCount > maxCount) {
										break outer;
									}
									objectList.add(new ConceptListItem(word));
								}
						}
					} else {
						for (ConceptWord word : words) {
							if (++curCount > maxCount) {
								break;
							}
							objectList.add(new ConceptListItem(word));
						}
					}
				}
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				log.error(e + " - " + sw.toString());
				objectList.add("Error while attempting to find concepts - "
						+ e.getMessage());
			}
		}
		return objectList;
	}

	public ConceptListItem getConcept(Integer conceptId) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Locale locale = context.getLocale();
		ConceptService cs = context.getConceptService();
		Concept c = cs.getConcept(conceptId);

		return c == null ? null : new ConceptListItem(c, locale);
	}

	public List<ConceptListItem> findProposedConcepts(String text) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Locale locale = context.getLocale();
		ConceptService cs = context.getConceptService();

		List<Concept> concepts = cs.findProposedConcepts(text);
		List<ConceptListItem> cli = new Vector<ConceptListItem>();
		for (Concept c : concepts)
			cli.add(new ConceptListItem(c, locale));

		return cli;
	}

	public List<ConceptListItem> findConceptAnswers(String text,
			ConceptListItem conceptListItem, boolean includeVoided) {

		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Locale locale = context.getLocale();
		ConceptService cs = context.getConceptService();

		Concept concept = cs.getConcept(conceptListItem.getConceptId());

		List<ConceptWord> words = cs.findConceptAnswers(text, locale, concept,
				includeVoided);

		List<ConceptListItem> items = new Vector<ConceptListItem>();
		for (ConceptWord word : words) {
			items.add(new ConceptListItem(word));
		}

		return items;
	}
	
	public List<ConceptListItem> getQuestionsForAnswer(Integer conceptId) {
		Context context = (Context) WebContextFactory.get().getSession()
		.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Locale locale = context.getLocale();
		ConceptService cs = context.getConceptService();
		
		Concept concept = cs.getConcept(conceptId);
		
		List<Concept> concepts = cs.getQuestionsForAnswer(concept);
		
		List<ConceptListItem> items = new Vector<ConceptListItem>();
		for (Concept c : concepts) {
			items.add(new ConceptListItem(c, locale));
		}
		
		return items;
	}

	public ConceptDrugListItem getDrug(Integer drugId) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Locale locale = context.getLocale();
		ConceptService cs = context.getConceptService();
		Drug d = cs.getDrug(drugId);

		return d == null ? null : new ConceptDrugListItem(d, locale);
	}

	public List getDrugs(Integer conceptId, boolean showConcept) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Locale locale = context.getLocale();
		ConceptService cs = context.getConceptService();
		Concept concept = cs.getConcept(conceptId);

		List<Object> items = new Vector<Object>();

		// Add this concept as the first option in the list
		// If there are no drugs to choose from, this will be automatically
		// selected
		// by the openmrsSearch.fillTable(objs) function
		if (showConcept == true) {
			ConceptDrugListItem thisConcept = new ConceptDrugListItem(null,
					conceptId, concept.getName(locale, false).getName());
			items.add(thisConcept);
		}

		// find drugs for this concept
		List<Drug> drugs = cs.getDrugs(concept);

		// if there are drugs to choose from, add some instructions
		if (drugs.size() > 0 && showConcept == true)
			items.add("Or choose a form of "
					+ concept.getName(locale, false).getName());

		// miniaturize our drug objects
		for (Drug drug : drugs) {
			items.add(new ConceptDrugListItem(drug, locale));
		}

		return items;
	}

	public List findDrugs(String phrase, boolean includeRetired) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Locale locale = context.getLocale();
		ConceptService cs = context.getConceptService();

		List<Object> items = new Vector<Object>();

		// find drugs for this concept
		List<Drug> drugs = cs.findDrugs(phrase, includeRetired);

		// miniaturize our drug objects
		for (Drug drug : drugs) {
			items.add(new ConceptDrugListItem(drug, locale));
		}

		return items;
	}

	public boolean isValidNumericValue(Float value, Integer conceptId) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		ConceptNumeric conceptNumeric = context.getConceptService()
				.getConceptNumeric(conceptId);

		return Helper.isValidNumericValue(value, conceptNumeric);
	}

	public String getConceptNumericUnits(Integer conceptId) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		ConceptNumeric conceptNumeric = context.getConceptService()
				.getConceptNumeric(conceptId);

		return conceptNumeric.getUnits();
	}

}