package org.openmrs.web.dwr;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
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
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

public class DWRConceptService {

	protected final Log log = LogFactory.getLog(getClass());

	public Vector findConcepts(String phrase, boolean includeRetired,
			List<String> includeClassNames, List<String> excludeClassNames,
			List<String> includeDatatypeNames, List<String> excludeDatatypeNames,
			boolean includeDrugConcepts) {

		// List to return
		// Object type gives ability to return error strings
		Vector<Object> objectList = new Vector<Object>();

		// TODO add localization for messages

		Integer userId = -1;
		User u = Context.getAuthenticatedUser();
		if (u != null)
			userId = u.getUserId();

		log.info(userId + "|" + phrase + "|" + includeClassNames.toString());

		Locale locale = Context.getLocale();
		if (includeClassNames == null)
			includeClassNames = new Vector<String>();
		if (excludeClassNames == null)
			excludeClassNames = new Vector<String>();
		if (includeDatatypeNames == null)
			includeDatatypeNames = new Vector<String>();
		if (excludeDatatypeNames == null)
			excludeDatatypeNames = new Vector<String>();
		
		try {
			ConceptService cs = Context.getConceptService();
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
				for (String name : includeClassNames)
					includeClasses.add(cs.getConceptClassByName(name));

				// turn classnames into class objects
				List<ConceptClass> excludeClasses = new Vector<ConceptClass>();
				for (String name : excludeClassNames)
					excludeClasses.add(cs.getConceptClassByName(name));
				
				// turn classnames into class objects
				List<ConceptDatatype> includeDatatypes = new Vector<ConceptDatatype>();
				for (String name : includeDatatypeNames)
					includeDatatypes.add(cs.getConceptDatatypeByName(name));
				
				// turn classnames into class objects
				List<ConceptDatatype> excludeDatatypes = new Vector<ConceptDatatype>();
				for (String name : excludeDatatypeNames)
					excludeDatatypes.add(cs.getConceptDatatypeByName(name));

				// perform the search
				words.addAll(cs.findConcepts(phrase, locale, includeRetired, 
						includeClasses, excludeClasses,
						includeDatatypes, excludeDatatypes));
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

		if (objectList.size() == 0)
			objectList.add("No matches found for <b>" + phrase + "</b>");

		return objectList;
	}

	public ConceptListItem getConcept(Integer conceptId) {
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		Concept c = cs.getConcept(conceptId);

		return c == null ? null : new ConceptListItem(c, locale);
	}

	public List<ConceptListItem> findProposedConcepts(String text) {
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();

		List<Concept> concepts = cs.findProposedConcepts(text);
		List<ConceptListItem> cli = new Vector<ConceptListItem>();
		for (Concept c : concepts)
			cli.add(new ConceptListItem(c, locale));

		return cli;
	}

	public List<Object> findConceptAnswers(String text,
			Integer conceptId, boolean includeVoided, boolean includeDrugConcepts) {

		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();

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
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		FormService fs = Context.getFormService();

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
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();

		Concept concept = cs.getConcept(conceptId);

		List<Concept> concepts = cs.getQuestionsForAnswer(concept);

		List<ConceptListItem> items = new Vector<ConceptListItem>();
		for (Concept c : concepts) {
			items.add(new ConceptListItem(c, locale));
		}

		return items;
	}

	public ConceptDrugListItem getDrug(Integer drugId) {
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		Drug d = cs.getDrug(drugId);

		return d == null ? null : new ConceptDrugListItem(d, locale);
	}

	public List getDrugs(Integer conceptId, boolean showConcept) {
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
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
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();

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
		ConceptNumeric conceptNumeric = Context.getConceptService()
				.getConceptNumeric(conceptId);

		return OpenmrsUtil.isValidNumericValue(value, conceptNumeric);
	}

	public String getConceptNumericUnits(Integer conceptId) {
		ConceptNumeric conceptNumeric = Context.getConceptService()
				.getConceptNumeric(conceptId);

		return conceptNumeric.getUnits();
	}
	
	public List<ConceptListItem> getAnswersForQuestion(Integer conceptId) {
		Vector<ConceptListItem> ret = new Vector<ConceptListItem>();
		Concept c = Context.getConceptService().getConcept(conceptId);
		Collection<ConceptAnswer> answers = c.getAnswers();
		// TODO: deal with concept answers (e.g. drug) whose answer concept is null. (Not sure if this actually ever happens)
		for (ConceptAnswer ca : answers)
			if (ca.getAnswerConcept() != null)
				ret.add(new ConceptListItem(ca.getAnswerConcept(), Context.getLocale()));
		return ret;
	}

}