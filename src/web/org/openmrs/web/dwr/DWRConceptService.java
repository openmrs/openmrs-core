package org.openmrs.web.dwr;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.Field;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRConceptService {

	protected final Log log = LogFactory.getLog(getClass());

	public Vector findConcepts(String phrase, boolean includeRetired,
			List<String> includeClassNames, List<String> excludeClassNames,
			boolean includeDrugConcepts) {

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

			log.info(userId + "|" + phrase + "|" + includeClassNames.toString());

			Locale locale = context.getLocale();
			if (includeClassNames == null)
				includeClassNames = new Vector<String>();
			if (excludeClassNames == null)
				excludeClassNames = new Vector<String>();
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
					// turn classnames into class objects
					List<ConceptClass> includeClasses = new Vector<ConceptClass>();
					if (includeClassNames.size() > 0) {
						for (String name : includeClassNames)
							includeClasses.add(cs.getConceptClassByName(name));
					}

					// turn classnames into class objects
					List<ConceptClass> excludeClasses = new Vector<ConceptClass>();
					if (excludeClassNames.size() > 0) {
						for (String name : excludeClassNames)
							excludeClasses.add(cs.getConceptClassByName(name));
					}

					// perform the search
					words.addAll(cs.findConcepts(phrase, locale,
							includeRetired, includeClasses, excludeClasses));
				}

				if (words.size() == 0) {
					objectList
							.add("No matches found for <b>" + phrase + "</b>");
				} else {
					objectList = new Vector<Object>(words.size());
					int maxCount = 500;
					int curCount = 0;

					// turn words into concept list items
					// if user wants drug concepts included, append those
					for (ConceptWord word : words) {
						if (++curCount > maxCount)
							break;
						objectList.add(new ConceptListItem(word));

						// add drugs for concept if desired
						if (includeDrugConcepts) {
							Integer classId = word.getConcept().getConceptClass().getConceptClassId();
							if (classId.equals(OpenmrsConstants.CONCEPT_CLASS_DRUG))
								for (Drug d : cs.getDrugs(word.getConcept()))
									objectList.add(new ConceptDrugListItem(d,
											locale));
						}
					}
				}
			} catch (Exception e) {
				log.error("Error while finding concepts + "
						+ e.getMessage(), e);
				objectList.add("Error while attempting to find concepts - "
						+ e.getMessage());
			}
		}

		if (objectList.size() == 0)
			objectList.add("No matches found for <b>" + phrase + "</b>");

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

	public List<Object> findConceptAnswers(String text,
			Integer conceptId, boolean includeVoided, boolean includeDrugConcepts) {

		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Locale locale = context.getLocale();
		ConceptService cs = context.getConceptService();

		Concept concept = cs.getConcept(conceptId);

		List<ConceptWord> words = cs.findConceptAnswers(text, locale, concept,
				includeVoided);

		List<Drug> drugAnswers = new Vector<Drug>();
		for (ConceptAnswer conceptAnswer : concept.getAnswers()) {
			if (conceptAnswer.getAnswerDrug() != null)
				drugAnswers.add(conceptAnswer.getAnswerDrug());
		}
		
		List<Object> items = new Vector<Object>();
		for (ConceptWord word : words) {
			items.add(new ConceptListItem(word));
			// add drugs for concept if desired
			if (includeDrugConcepts) {
				Integer classId = word.getConcept().getConceptClass().getConceptClassId();
				if (classId.equals(OpenmrsConstants.CONCEPT_CLASS_DRUG))
					for (Drug d : cs.getDrugs(word.getConcept())) {
						if (drugAnswers.contains(d))
							items.add(new ConceptDrugListItem(d, locale));
					}
			}
		}

		return items;
	}

	public List<Object> getConceptSet(Integer conceptId) {

		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Locale locale = context.getLocale();
		ConceptService cs = context.getConceptService();
		FormService fs = context.getFormService();

		Concept concept = cs.getConcept(conceptId);

		List<Object> returnList = new Vector<Object>();

		if (concept.isSet()) {
			for (ConceptSet set : concept.getConceptSets()) {
				Field field = null;
				for (Field f : fs.findFields(set.getConcept())) {
					ConceptName cn = set.getConcept().getName(locale);
					if (f.getName().equals(cn.getName())
							&& f.getDescription().equals(cn.getDescription())
							&& f.isSelectMultiple().equals(false))
						field = f;
				}
				if (field == null)
					returnList
							.add(new ConceptListItem(set.getConcept(), locale));
				else
					returnList.add(new FieldListItem(field, locale));
			}
		}

		return returnList;
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
		List<Drug> drugs = null;

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

		return OpenmrsUtil.isValidNumericValue(value, conceptNumeric);
	}

	public String getConceptNumericUnits(Integer conceptId) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		ConceptNumeric conceptNumeric = context.getConceptService()
				.getConceptNumeric(conceptId);

		return conceptNumeric.getUnits();
	}

}