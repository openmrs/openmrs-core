package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDerived;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSetDerived;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptSynonym;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConceptsLockedException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * Concept-related services
 * @version 1.0
 */
public class ConceptServiceImpl implements ConceptService {

	private final Log log = LogFactory.getLog(getClass());

	private ConceptDAO dao;
	
	public ConceptServiceImpl() { }

	private ConceptDAO getConceptDAO() {
		return dao;
	}
	
	public void setConceptDAO(ConceptDAO dao) {
		this.dao = dao;
	}

	
	/**
	 * @param concept
	 *            to be created
	 */
	public void createConcept(Concept concept) {
		this.createConcept(concept, false);
	}
	
	/**
	 * @param concept
	 *            to be created
	 */
	public void createConcept(Concept concept, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		log.info(authUserId + "|" + concept.getConceptId());

		getConceptDAO().createConcept(concept, isForced);
	}

	/**
	 * @param numeric
	 *            concept to be created
	 */
	public void createConcept(ConceptNumeric concept) {
		this.createConcept(concept, false);
	}
	/**
	 * @param numeric
	 *            concept to be created
	 */
	public void createConcept(ConceptNumeric concept, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		log.info(authUserId + "|" + concept.getConceptId()+ "|numeric");

		getConceptDAO().createConcept(concept, isForced);
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
	 * Gets the conceptAnswer with the given internal identifier
	 * 
	 * @param conceptAnswerId
	 * @return ConceptAnswer
	 */
	public ConceptAnswer getConceptAnswer(Integer conceptAnswerId) {
		return getConceptDAO().getConceptAnswer(conceptAnswerId);
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
		this.updateConcept(concept, false);
	}
	
	/**
	 * Update the given concept
	 * 
	 * @param concept
	 *            to be updated
	 */
	public void updateConcept(Concept concept, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		if (log.isInfoEnabled())
			log.info(authUserId + "|" + (concept.getConceptId() == null ? "ConceptId is null" : concept.getConceptId().toString()));

		getConceptDAO().updateConcept(concept, isForced);
	}

	/**
	 * Update the given numeric concept
	 * 
	 * @param numeric
	 *            concept to be updated
	 */
	public void updateConcept(ConceptNumeric concept) {
		this.updateConcept(concept, false);
	}
	
	/**
	 * Update the given numeric concept
	 * 
	 * @param numeric
	 *            concept to be updated
	 */
	public void updateConcept(ConceptNumeric concept, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		log.info(authUserId + "|" + concept.getConceptId().toString());

		getConceptDAO().updateConcept(concept, isForced);
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
		this.deleteConcept(concept, false);
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
	public void deleteConcept(Concept concept, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
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
		this.voidConcept(concept, reason, false);
	}
		
	public void voidConcept(Concept concept, String reason, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		log.info(authUserId + "|" + concept.getConceptId().toString());

		getConceptDAO().voidConcept(concept, reason, isForced);
	}

	/**
	 * @param drug
	 *            to be created
	 */
	public void createDrug(Drug drug) {
		this.createDrug(drug, false);
	}
	
	public void createDrug(Drug drug, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
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
		this.updateDrug(drug, false);
	}
		
	public void updateDrug(Drug drug, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
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
	public List<Concept> getConceptsByName(String name) {
		return getConceptDAO().getConceptsByName(name);
	}

	/**
	 * Return a Concept that matches the name exactly
	 * 
	 * @param name
	 * @return Concept with matching name
	 */
	public Concept getConceptByName(String name) {
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
	 * Return the drug object corresponding to the given name
	 * 
	 * @return Drug
	 */
	public Drug getDrug(String drugName) {
		return getConceptDAO().getDrug(drugName);
	}

	public Drug getDrugByNameOrId(String drug) {
		Integer drugId = null;
		
		try {
			drugId = new Integer(drug);
		} catch ( NumberFormatException nfe ) {
			drugId = null;
		}
		
		if ( drugId != null ) {
			return getDrug(drugId);
		} else {
			return getDrug(drug);
		}
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
	 * @param i Integer
	 * @return ConceptClass
	 */
	public ConceptClass getConceptClass(Integer i) {
		return getConceptDAO().getConceptClass(i);
	}
	
	/**
	 * Return a Concept class matching the given name
	 * 
	 * @param name String
	 * @return ConceptClass
	 */
	public ConceptClass getConceptClassByName(String name) {
		return getConceptDAO().getConceptClassByName(name);
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
	
	public ConceptDatatype getConceptDatatypeByName(String name) {
		return getConceptDAO().getConceptDatatypeByName(name);
	}

	/**
	 * Return a list of the concept sets with concept_set matching concept
	 * For example to find all concepts for ARVs, you would do
	 *    getConceptSets(getConcept("ANTIRETROVIRAL MEDICATIONS"))
	 * and then take the conceptIds from the resulting list.
	 * 
	 * @return List
	 */
	public List<ConceptSet> getConceptSets(Concept c) {
		return getConceptDAO().getConceptSets(c);
	}
	
	public List<Concept> getConceptsInSet(Concept c) {
		Set<Integer> alreadySeen = new HashSet<Integer>();
		List<Concept> ret = new ArrayList<Concept>();
		explodeConceptSetHelper(c, ret, alreadySeen);
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByClass(org.openmrs.ConceptClass)
	 */
	public List<Concept> getConceptsByClass(ConceptClass cc) {
		return getConceptDAO().getConceptsByClass(cc);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getSetsContainingConcept(org.openmrs.Concept)
	 */
	public List<ConceptSet> getSetsContainingConcept(Concept concept) {
		return getConceptDAO().getSetsContainingConcept(concept);
	}

	private void explodeConceptSetHelper(Concept concept, Collection<Concept> ret, Collection<Integer> alreadySeen) {
		if (alreadySeen.contains(concept.getConceptId()))
			return;
		alreadySeen.add(concept.getConceptId());
		List<ConceptSet> cs = getConceptSets(concept);
		for (ConceptSet set : cs) {
			Concept c = set.getConcept();
			if (c.isSet()) {
				explodeConceptSetHelper(c, ret, alreadySeen);
			} else {
				ret.add(c);
			}
		}
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
		return findConcepts(phrase, locale, includeRetired, null, null, null, null);
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
	 * @param requireClasses
	 *            List<ConceptClass>
	 * @param excludeClasses
	 *            List<ConceptClass>
	 * @param requireDatatypes
	 *            List<ConceptDatatype>
	 * @param excludeDatatypes
	 *            List<ConceptDatatype>
	 * @return
	 * 
	 * @see ConceptService.findConcepts(String,Locale,boolean)
	 */
	public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired, 
			List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, 
			List<ConceptDatatype> requireDatatypes, List<ConceptDatatype> excludeDatatypes) {
		
		if (requireClasses == null)
			requireClasses = new Vector<ConceptClass>();
		if (excludeClasses == null)
			excludeClasses = new Vector<ConceptClass>();
		if (requireDatatypes == null)
			requireDatatypes = new Vector<ConceptDatatype>();
		if (excludeDatatypes == null)
			excludeDatatypes = new Vector<ConceptDatatype>();
		
		List<ConceptWord> conceptWords = getConceptDAO().findConcepts(phrase,
				locale, includeRetired, requireClasses, excludeClasses, requireDatatypes, excludeDatatypes);

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
	 * Get the questions that have this concept as a possible answer
	 * 
	 * @param concept
	 *            Concept to get
	 * @return list of concepts
	 */
	public List<Concept> getQuestionsForAnswer(Concept concept) {
		List<Concept> concepts = getConceptDAO().getQuestionsForAnswer(concept);

		return concepts;
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
				// check synonym in case we have multiple synonym hits
				String toSplit = initialWord.getSynonym();
				if (toSplit == null || toSplit.equals("")) {
					ConceptName cn = initialWord.getConcept().getName(locale);
					toSplit = cn.getName();
				}
				List<String> nameWords = ConceptWord.getUniqueWords(toSplit);

				// if the conceptName doesn't contain all of the search words,
				// replace the initial word with this synonym based word
				if (!containsAll(nameWords, searchedWords)) {
					tmpWord.setWeight(initialWord.getWeight());
					uniqueConcepts.put(id, tmpWord);
				} else
					tmpWord = null;

			} else {
				// normalize the weighting
				tmpWord.setWeight(0.0);
				// its not in the list, add it
				uniqueConcepts.put(id, tmpWord);
			}

			// don't increase weight with second/third/... synonym
			if (tmpWord != null) {
				// default matched string
				String matchedString = tmpWord.getSynonym();

				// if there isn't a synonym, it is matching on the name,
				if (matchedString.length() == 0) {
					// We weight name matches higher
					tmpWord.increaseWeight(2.0);
					matchedString = tmpWord.getConcept().getName(locale)
							.getName();
				}

				// increase the weight by a factor of the % of words matched
				Double percentMatched = getPercentMatched(searchedWords,
						matchedString);
				tmpWord.increaseWeight(5.0 * percentMatched);
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

		// set the state of the proposal
		if (conceptProposal.getState() == null)
			conceptProposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED);

		// set the creator and date created
		if (conceptProposal.getCreator() == null
				&& conceptProposal.getEncounter() != null)
			conceptProposal.setCreator(conceptProposal.getEncounter()
					.getCreator());
		else
			conceptProposal.setCreator(Context.getAuthenticatedUser());

		if (conceptProposal.getDateCreated() == null
				&& conceptProposal.getEncounter() != null)
			conceptProposal.setDateCreated(conceptProposal.getEncounter()
					.getDateCreated());
		else
			conceptProposal.setDateCreated(new Date());

		getConceptDAO().proposeConcept(conceptProposal);
	}

	public Integer getNextAvailableId() {
		return getConceptDAO().getNextAvailableId();
	}

	private Boolean containsAll(Collection<String> parent,
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

	private double getPercentMatched(Collection<String> searchedWords,
			String matchedString) {

		List<String> subList = ConceptWord.getUniqueWords(matchedString);
		double size = ConceptWord.splitPhrase(matchedString).length; // total
		// # of
		// words

		double matches = 0.0;
		for (String s : subList) {
			s = s.toUpperCase();
			for (String p : searchedWords) {
				p = p.toUpperCase();
				if (p.startsWith(s))
					matches += 1.0;
			}
		}

		return matches == 0 ? 0 : (matches / size);
	}

	public Concept getConceptByIdOrName(String idOrName) {
		Concept c = null;
		Integer conceptId = null;
		
		try {
			conceptId = new Integer(idOrName);
		} catch (NumberFormatException nfe) {
			conceptId = null;
		}
		
		if ( conceptId != null ) {
			c = getConcept(conceptId);
		} else {
			c = getConceptByName(idOrName);
		}

		return c;
	}
	
	/**
	 * Checks to see if the global property concepts.locked is set to true.  If so, an error is thrown.
	 */
	public void checkIfLocked() throws ConceptsLockedException {
		String locked = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_CONCEPTS_LOCKED, "false");
		if (locked.toLowerCase().equals("true"))
			throw new ConceptsLockedException();
	}
	
	public List<Concept> getConceptsWithDrugsInFormulary() {
		return getConceptDAO().getConceptsWithDrugsInFormulary();
	}

	/**
     * @see org.openmrs.api.ConceptService#getConceptByGuid(java.lang.String)
     */
    public Concept getConceptByGuid(String guid) {
		return getConceptDAO().getConceptByGuid(guid);
    }

	/**
     * @see org.openmrs.api.ConceptService#getConceptClassByGuid(java.lang.String)
     */
    public ConceptClass getConceptClassByGuid(String guid) {
		return getConceptDAO().getConceptClassByGuid(guid);
    }

    public ConceptAnswer getConceptAnswerByGuid(String guid) {
		return getConceptDAO().getConceptAnswerByGuid(guid);
    }

    public ConceptDerived getConceptDerivedByGuid(String guid) {
		return getConceptDAO().getConceptDerivedByGuid(guid);
    }

    public ConceptName getConceptNameByGuid(String guid) {
		return getConceptDAO().getConceptNameByGuid(guid);
    }

    public ConceptSet getConceptSetByGuid(String guid) {
		return getConceptDAO().getConceptSetByGuid(guid);
    }

    public ConceptSetDerived getConceptSetDerivedByGuid(String guid) {
		return getConceptDAO().getConceptSetDerivedByGuid(guid);
    }

    public ConceptSource getConceptSourceByGuid(String guid) {
		return getConceptDAO().getConceptSourceByGuid(guid);
    }

    public ConceptSynonym getConceptSynonymByGuid(String guid) {
		return getConceptDAO().getConceptSynonymByGuid(guid);
    }

    public ConceptWord getConceptWordByGuid(String guid) {
		return getConceptDAO().getConceptWordByGuid(guid);
    }

	/**
     * @see org.openmrs.api.ConceptService#getConceptDatatypeByGuid(java.lang.String)
     */
    public ConceptDatatype getConceptDatatypeByGuid(String guid) {
		return getConceptDAO().getConceptDatatypeByGuid(guid);
    }

	/**
     * @see org.openmrs.api.ConceptService#getConceptNumericByGuid(java.lang.String)
     */
    public ConceptNumeric getConceptNumericByGuid(String guid) {
		return getConceptDAO().getConceptNumericByGuid(guid);
    }

	/**
     * @see org.openmrs.api.ConceptService#getConceptProposalByGuid(java.lang.String)
     */
    public ConceptProposal getConceptProposalByGuid(String guid) {
		return getConceptDAO().getConceptProposalByGuid(guid);
    }

	/**
     * @see org.openmrs.api.ConceptService#getDrugByGuid(java.lang.String)
     */
    public Drug getDrugByGuid(String guid) {
		return getConceptDAO().getDrugByGuid(guid);
    }

    public DrugIngredient getDrugIngredientByGuid(String guid) {
		return getConceptDAO().getDrugIngredientByGuid(guid);
    }

	/**
	 * @param conceptAnswer
	 *            to be created
	 */
	public void createConceptAnswer(ConceptAnswer conceptAnswer) {
		this.createConceptAnswer(conceptAnswer, false);
	}	
		
	public void createConceptAnswer(ConceptAnswer conceptAnswer, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		log.info("creating conceptAnswer - " + authUserId + "|"
				+ conceptAnswer.getConcept().toString());

		getConceptDAO().createConceptAnswer(conceptAnswer);
	}

	/**
	 * Update the given conceptAnswer
	 * 
	 * @param conceptAnswer
	 *            to be updated
	 */
	public void updateConceptAnswer(ConceptAnswer conceptAnswer) {	
		this.updateConceptAnswer(conceptAnswer, false);
	}
	
	public void updateConceptAnswer(ConceptAnswer conceptAnswer, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		if (conceptAnswer.getConcept() != null)
			log.info("updating conceptAnswer - " + authUserId + "|"
					+ conceptAnswer.getConcept().toString());

		getConceptDAO().updateConceptAnswer(conceptAnswer);
	}

	/**
	 * @param conceptName
	 *            to be created
	 */
	public void createConceptName(ConceptName conceptName) {
		this.createConceptName(conceptName, false);
	}
	
	public void createConceptName(ConceptName conceptName, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		log.info("creating conceptName - " + authUserId + "|"
				+ conceptName.getConcept().toString());

		getConceptDAO().createConceptName(conceptName);
	}

	/**
	 * Update the given conceptName
	 * 
	 * @param conceptName
	 *            to be updated
	 */
	public void updateConceptName(ConceptName conceptName) {
		this.updateConceptName(conceptName, false);
	}
	
	public void updateConceptName(ConceptName conceptName, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		if (conceptName.getConcept() != null)
			log.info("updating conceptName - " + authUserId + "|"
					+ conceptName.getConcept().toString());

		getConceptDAO().updateConceptName(conceptName);
	}

	/**
	 * @param conceptSet
	 *            to be created
	 */
	public void createConceptSet(ConceptSet conceptSet) {
		this.createConceptSet(conceptSet, false);
	}
	
	public void createConceptSet(ConceptSet conceptSet, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		log.info("creating conceptSet - " + authUserId + "|"
				+ conceptSet.getConcept().toString());

		getConceptDAO().createConceptSet(conceptSet);
	}

	/**
	 * Update the given conceptSet
	 * 
	 * @param conceptSet
	 *            to be updated
	 */
	public void updateConceptSet(ConceptSet conceptSet) {
		this.updateConceptSet(conceptSet, false);
	}

	public void updateConceptSet(ConceptSet conceptSet, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		if (conceptSet.getConcept() != null)
			log.info("updating conceptSet - " + authUserId + "|"
					+ conceptSet.getConcept().toString());

		getConceptDAO().updateConceptSet(conceptSet);
	}

	/**
	 * @param conceptSource
	 *            to be created
	 */
	public void createConceptSource(ConceptSource conceptSource) {
		this.createConceptSource(conceptSource, false);
	}
	
	public void createConceptSource(ConceptSource conceptSource, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		log.info("creating conceptSource - " + authUserId + "|"
				+ conceptSource.toString());

		getConceptDAO().createConceptSource(conceptSource);
	}

	/**
	 * Update the given conceptSource
	 * 
	 * @param conceptSource
	 *            to be updated
	 */
	public void updateConceptSource(ConceptSource conceptSource) {
		this.updateConceptSource(conceptSource, false);
	}
	
	public void updateConceptSource(ConceptSource conceptSource, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		if (conceptSource != null)
			log.info("updating conceptSource - " + authUserId + "|"
					+ conceptSource.toString());

		getConceptDAO().updateConceptSource(conceptSource);
	}

	/**
	 * @param conceptSynonym
	 *            to be created
	 */
	public void createConceptSynonym(ConceptSynonym conceptSynonym) {
		this.createConceptSynonym(conceptSynonym, false);
	}
	
	public void createConceptSynonym(ConceptSynonym conceptSynonym,  boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		log.info("creating conceptSynonym - " + authUserId + "|"
				+ conceptSynonym.toString());

		getConceptDAO().createConceptSynonym(conceptSynonym);
	}

	/**
	 * Update the given conceptSynonym
	 * 
	 * @param conceptSynonym
	 *            to be updated
	 */
	public void updateConceptSynonym(ConceptSynonym conceptSynonym) {
		this.updateConceptSynonym(conceptSynonym, false);
	}

	public void updateConceptSynonym(ConceptSynonym conceptSynonym, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		if (conceptSynonym != null)
			log.info("updating conceptSynonym - " + authUserId + "|"
					+ conceptSynonym.toString());

		getConceptDAO().updateConceptSynonym(conceptSynonym);
	}
	
	/**
	 * @param conceptWord
	 *            to be created
	 */
	public void createConceptWord(ConceptWord conceptWord) {
		this.createConceptWord(conceptWord, false);
	}
	
	public void createConceptWord(ConceptWord conceptWord, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		log.info("creating conceptWord - " + authUserId + "|"
				+ conceptWord.toString());

		getConceptDAO().createConceptWord(conceptWord);
	}

	/**
	 * Update the given conceptWord
	 * 
	 * @param conceptWord
	 *            to be updated
	 */
	public void updateConceptWord(ConceptWord conceptWord) {
		this.updateConceptWord(conceptWord, false);
	}
	
	public void updateConceptWord(ConceptWord conceptWord, boolean isForced) {
		if ( !isForced ) checkIfLocked();
		
		String authUserId = Context.getAuthenticatedUser().getUserId()
				.toString();

		if (conceptWord != null)
			log.info("updating conceptWord - " + authUserId + "|"
					+ conceptWord.toString());

		getConceptDAO().updateConceptWord(conceptWord);
	}

    /**
     * @see org.openmrs.api.ConceptService#getConceptGuids()
     */
    public Map<Integer, String> getConceptGuids() {
        return getConceptDAO().getConceptGuids();
    }

}
