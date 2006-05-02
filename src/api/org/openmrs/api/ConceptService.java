package org.openmrs.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.api.db.DAOContext;
import org.openmrs.util.OpenmrsConstants;

/**
 * Concept-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class ConceptService {

	private final Log log = LogFactory.getLog(getClass());

	private Context context;

	private DAOContext daoContext;

	public ConceptService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}

	private ConceptDAO getConceptDAO() {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_VIEW_CONCEPTS);

		return daoContext.getConceptDAO();
	}

	/**
	 * @param concept
	 *            to be created
	 */
	public void createConcept(Concept concept) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_ADD_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_ADD_CONCEPTS);

		String authUserId = context.getAuthenticatedUser().getUserId()
				.toString();

		log.info(authUserId + "|" + concept.getConceptId().toString());

		getConceptDAO().createConcept(concept);
	}

	/**
	 * @param numeric
	 *            concept to be created
	 */
	public void createConcept(ConceptNumeric concept) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_ADD_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_ADD_CONCEPTS);

		String authUserId = context.getAuthenticatedUser().getUserId()
				.toString();

		log.info(authUserId + "|" + concept.getConceptId().toString()
				+ "|numeric");

		getConceptDAO().createConcept(concept);
	}

	/**
	 * Gets the concept with the given internal identifier
	 * 
	 * @param conceptId
	 * @return Concept
	 */
	public Concept getConcept(Integer conceptId) {
		return getConceptDAO().getConcept(conceptId);
	}

	/**
	 * Return a list of concepts sorted on sortBy in dir direction (asc/desc)
	 * 
	 * @param sortBy
	 * @param dir
	 * @return List of concepts
	 */
	public List<Concept> getConcepts(String sortBy, String dir) {
		return getConceptDAO().getConcepts(sortBy, dir);
	}

	/**
	 * Update the given concept
	 * 
	 * @param concept
	 *            to be updated
	 */
	public void updateConcept(Concept concept) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		String authUserId = context.getAuthenticatedUser().getUserId()
				.toString();

		log.info(authUserId + "|" + concept.getConceptId().toString());

		getConceptDAO().updateConcept(concept);
	}

	/**
	 * Update the given numeric concept
	 * 
	 * @param numeric
	 *            concept to be updated
	 */
	public void updateConcept(ConceptNumeric concept) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		String authUserId = context.getAuthenticatedUser().getUserId()
				.toString();

		log.info(authUserId + "|" + concept.getConceptId().toString());

		getConceptDAO().updateConcept(concept);
	}

	/**
	 * Delete the given concept
	 * 
	 * For super users only. If dereferencing concepts, use
	 * <code>voidConcept(org.openmrs.Concept)</code>
	 * 
	 * @param Concept
	 *            to be deleted
	 */
	public void deleteConcept(Concept concept) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_DELETE_CONCEPTS);

		String authUserId = context.getAuthenticatedUser().getUserId()
				.toString();

		log.info(authUserId + "|" + concept.getConceptId().toString());

		getConceptDAO().deleteConcept(concept);
	}

	/**
	 * Voiding a concept essentially removes it from circulation
	 * 
	 * @param Concept
	 *            concept
	 * @param String
	 *            reason
	 */
	public void voidConcept(Concept concept, String reason) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		String authUserId = context.getAuthenticatedUser().getUserId()
				.toString();

		log.info(authUserId + "|" + concept.getConceptId().toString());

		getConceptDAO().voidConcept(concept, reason);
	}

	/**
	 * @param drug
	 *            to be created
	 */
	public void createDrug(Drug drug) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_ADD_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_ADD_CONCEPTS);

		String authUserId = context.getAuthenticatedUser().getUserId()
				.toString();

		log.info("creating drug - " + authUserId + "|"
				+ drug.getConcept().toString());

		getConceptDAO().createDrug(drug);
	}

	/**
	 * Update the given drug
	 * 
	 * @param drug
	 *            to be updated
	 */
	public void updateDrug(Drug drug) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_CONCEPTS);

		String authUserId = context.getAuthenticatedUser().getUserId()
				.toString();

		if (drug.getConcept() != null)
			log.info("updating drug - " + authUserId + "|"
					+ drug.getConcept().toString());

		getConceptDAO().updateDrug(drug);
	}

	/**
	 * Return a list of concepts matching "name" anywhere in the name
	 * 
	 * @param name
	 * @return List of concepts
	 */
	public List<Concept> getConceptByName(String name) {
		return getConceptDAO().getConceptByName(name);
	}

	/**
	 * Return the drug object corresponding to the given id
	 * 
	 * @return Drug
	 */
	public Drug getDrug(Integer drugId) {
		return getConceptDAO().getDrug(drugId);
	}

	/**
	 * Return a list of drugs currently in the database
	 * 
	 * @return List of Drugs
	 */
	public List<Drug> getDrugs() {
		return getConceptDAO().getDrugs();
	}

	/**
	 * Find drugs in the system. The string search can match either drug.name or
	 * drug.concept.name
	 * 
	 * @param phrase
	 * @param includeRetired
	 * @return List of Drugs
	 */
	public List<Drug> findDrugs(String phrase, boolean includeRetired) {
		return getConceptDAO().findDrugs(phrase, includeRetired);
	}

	/**
	 * Return a list of drugs associated with the given concept
	 * 
	 * @param Concept
	 * @return List of Drugs
	 */
	public List<Drug> getDrugs(Concept concept) {
		return getConceptDAO().getDrugs(concept);
	}

	/**
	 * Return a list of concept classes currently in the database
	 * 
	 * @return List of Concept class objects
	 */
	public List<ConceptClass> getConceptClasses() {
		return getConceptDAO().getConceptClasses();
	}

	/**
	 * Return a Concept class matching the given identifier
	 * 
	 * @return ConceptClass
	 */
	public ConceptClass getConceptClass(Integer i) {
		return getConceptDAO().getConceptClass(i);
	}

	/**
	 * Return a list of concept datatypes currently in the database
	 * 
	 * @return List of ConceptDatatypes
	 */
	public List<ConceptDatatype> getConceptDatatypes() {
		return getConceptDAO().getConceptDatatypes();
	}

	/**
	 * Return a ConceptDatatype matching the given identifier
	 * 
	 * @return ConceptDatatype
	 */
	public ConceptDatatype getConceptDatatype(Integer i) {
		return getConceptDAO().getConceptDatatype(i);
	}

	/**
	 * Return a list of the concept sets with concept_set matching concept
	 * 
	 * @return List
	 */
	public List<ConceptSet> getConceptSets(Concept c) {
		return getConceptDAO().getConceptSets(c);
	}

	/**
	 * Return a concept numeric object given the concept id
	 * 
	 * @return ConceptNumeric
	 */
	public ConceptNumeric getConceptNumeric(Integer conceptId) {
		return getConceptDAO().getConceptNumeric(conceptId);
	}

	/**
	 * Searches on given phrase via the concept word table
	 * 
	 * @param phrase/search/words
	 *            String
	 * @param locale
	 *            Locale
	 * @param includeRetired
	 *            boolean
	 * @return
	 */
	public List<ConceptWord> findConcepts(String phrase, Locale locale,
			boolean includeRetired) {
		List<ConceptWord> conceptWords = getConceptDAO().findConcepts(phrase,
				locale, includeRetired);

		return weightWords(phrase, locale, conceptWords);
	}

	/**
	 * 
	 * Finds concepts but only returns the given range
	 * 
	 * @param phrase
	 * @param locale
	 * @param includeRetired
	 * @param start
	 * @param size
	 * @return ConceptWord list
	 */
	public List<ConceptWord> findConcepts(String phrase, Locale locale,
			boolean includeRetired, int start, int size) {

		List<ConceptWord> conceptWords = findConcepts(phrase, locale,
				includeRetired);

		List<ConceptWord> subList = conceptWords.subList(start, start + size);

		return subList;
	}

	public List<ConceptWord> findConceptAnswers(String phrase, Locale locale,
			Concept concept, boolean includeRetired) {
		List<ConceptWord> conceptWords = getConceptDAO().findConceptAnswers(
				phrase, locale, concept, includeRetired);

		return weightWords(phrase, locale, conceptWords);

	}

	/**
	 * This will weight and sort the concepts we are assuming the hits are
	 * sorted with synonym matches at the bottom
	 * 
	 * @param phrase
	 *            that was used to get this search
	 * @param locale
	 *            that was used to get this search
	 * @param conceptWords
	 * @return
	 */
	protected List<ConceptWord> weightWords(String phrase, Locale locale,
			List<ConceptWord> conceptWords) {

		// Map<ConceptId, ConceptWord>
		Map<Integer, ConceptWord> uniqueConcepts = new HashMap<Integer, ConceptWord>();

		// phrase words
		if (phrase == null)
			phrase = "";
		List<String> searchedWords = ConceptWord.getUniqueWords(phrase);

		Integer id = null;
		Concept concept = null;
		for (ConceptWord tmpWord : conceptWords) {
			concept = tmpWord.getConcept();
			id = concept.getConceptId();

			if (uniqueConcepts.containsKey(id)) {
				ConceptWord initialWord = uniqueConcepts.get(id);

				// this concept is already in the list
				// because we're sort synonyms at the bottom, the initial
				// concept must be a match on the conceptName
				String toSplit = initialWord.getSynonym();
				if (toSplit == null || toSplit.equals("")) {
					ConceptName cn = initialWord.getConcept().getName(locale);
					toSplit = cn.getName();
				}
				List<String> nameWords = ConceptWord.getUniqueWords(toSplit);

				initialWord.increaseWeight(1.0);
				// if the conceptName doesn't contain all of the search words,
				// replace the initial word with this synonym based word
				if (!containsAll(nameWords, searchedWords)) {
					tmpWord.setWeight(initialWord.getWeight());
					uniqueConcepts.put(id, tmpWord);
					log.debug("Using new conceptWord: " + tmpWord);
				}

			} else {
				// normalize the weighting
				tmpWord.setWeight(0.0);
				// its not in the list, add it
				uniqueConcepts.put(id, tmpWord);
			}

			if (tmpWord.getSynonym().length() == 0) {
				// if there isn't a synonym, it is matching on the name,
				// increase the weight
				uniqueConcepts.get(id).increaseWeight(2.0);
			} else {
				// increase the weight by a factor of the % of words matched
				uniqueConcepts.get(id).increaseWeight(
						5.0 * (1 / tmpWord.getSynonym().split(" ").length));
			}
		}

		conceptWords = new Vector<ConceptWord>();
		conceptWords.addAll(uniqueConcepts.values());
		Collections.sort(conceptWords);

		return conceptWords;
	}

	/**
	 * Finds the previous available concept via concept id
	 * 
	 * @param c
	 * @param offset
	 * @return
	 */
	public Concept getPrevConcept(Concept c) {
		return getConceptDAO().getPrevConcept(c);
	}

	/**
	 * Finds the next available concept via concept id
	 * 
	 * @param c
	 * @param offset
	 * @return
	 */
	public Concept getNextConcept(Concept c) {
		return getConceptDAO().getNextConcept(c);
	}

	public List<ConceptProposal> getConceptProposals(boolean includeCompleted) {
		return getConceptDAO().getConceptProposals(includeCompleted);
	}

	public ConceptProposal getConceptProposal(Integer conceptProposalId) {
		return getConceptDAO().getConceptProposal(conceptProposalId);
	}

	public List<ConceptProposal> findMatchingConceptProposals(String text) {
		return getConceptDAO().findMatchingConceptProposals(text);
	}

	public List<Concept> findProposedConcepts(String text) {
		return getConceptDAO().findProposedConcepts(text);
	}

	public void proposeConcept(ConceptProposal conceptProposal) {
		getConceptDAO().proposeConcept(conceptProposal);
	}

	public Integer getNextAvailableId() {
		return getConceptDAO().getNextAvailableId();
	}

	public Boolean containsAll(Collection<String> parent,
			Collection<String> subList) {

		for (String s : subList) {
			s = s.toUpperCase();
			boolean found = false;
			for (String p : parent) {
				p = p.toUpperCase();
				if (p.startsWith(s))
					found = true;
			}
			if (!found)
				return false;
		}
		return true;
	}
}