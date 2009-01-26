/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDerived;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSetDerived;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.util.OpenmrsConstants;

/**
 * The Hibernate class for Concepts, Drugs, and related classes. <br/>
 * <br/>
 * Use the {@link ConceptService} to access these methods
 * 
 * @see ConceptService
 */
public class HibernateConceptDAO implements ConceptDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * Sets the session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptComplex(java.lang.Integer)
	 */
	public ConceptComplex getConceptComplex(Integer conceptId) {
		ConceptComplex cc;
		Object obj = sessionFactory.getCurrentSession().get(ConceptComplex.class, conceptId);
		// If Concept has already been read & cached, we may get back a Concept instead of
		// ConceptComplex.  If this happens, we need to clear the object from the cache
		// and re-fetch it as a ConceptComplex
		if (obj != null && !obj.getClass().equals(ConceptComplex.class)) {
			sessionFactory.getCurrentSession().evict(obj); // remove from cache
			// session.get() did not work here, we need to perform a query to get a ConceptComplex
			Query query = sessionFactory.getCurrentSession().createQuery("from ConceptComplex where conceptId = :conceptId")
			        .setParameter("conceptId", conceptId);
			obj = query.uniqueResult();
		}
		cc = (ConceptComplex) obj;
		
		return cc;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#saveConcept(org.openmrs.Concept)
	 */
	public Concept saveConcept(Concept concept) throws DAOException {
		if ((concept.getConceptId() != null) && (concept.getConceptId() > 0)) {
			// this method checks the concept_numeric, concept_derived, etc tables
			// to see if a row exists there or not.  This is needed because hibernate
			// doesn't like to insert into concept_numeric but update concept in the 
			// same go.  It assumes that its either in both tables or no tables
			insertRowIntoSubclassIfNecessary(concept);
		}
		
		sessionFactory.getCurrentSession().saveOrUpdate(concept);
		return concept;
	}
	
	/**
	 * Convenience method that will check this concept for subtype values (ConceptNumeric,
	 * ConceptDerived, etc) and insert a line into that subtable if needed. This prevents a
	 * hibernate ConstraintViolationException
	 * 
	 * @param concept the concept that will be inserted
	 */
	private void insertRowIntoSubclassIfNecessary(Concept concept) {
		Connection connection = sessionFactory.getCurrentSession().connection();
		
		// check the concept_numeric table
		if (concept instanceof ConceptNumeric) {
			
			try {
				PreparedStatement ps = connection
				        .prepareStatement("SELECT * FROM concept WHERE concept_id = ? and not exists (select * from concept_numeric WHERE concept_id = ?)");
				ps.setInt(1, concept.getConceptId());
				ps.setInt(2, concept.getConceptId());
				ps.execute();
				
				if (ps.getResultSet().next()) {
					// we have to evict the current concept out of the session because
					// the user probably had to change the class of this object to get it 
					// to now be a numeric
					// (must be done before the "insert into...")
					sessionFactory.getCurrentSession().clear();
					
					ps = connection.prepareStatement("INSERT INTO concept_numeric (concept_id, precise) VALUES (?, false)");
					ps.setInt(1, concept.getConceptId());
					ps.executeUpdate();
				} else {
					// no stub insert is needed because either a concept row 
					// doesn't exist or a concept_numeric row does exist
				}
				
			}
			catch (SQLException e) {
				log.error("Error while trying to see if this ConceptNumeric is in the concept_numeric table already", e);
			}
		} else if (concept instanceof ConceptComplex) {
			
			try {
				PreparedStatement ps = connection
				        .prepareStatement("SELECT * FROM concept WHERE concept_id = ? and not exists (select * from concept_complex WHERE concept_id = ?)");
				ps.setInt(1, concept.getConceptId());
				ps.setInt(2, concept.getConceptId());
				ps.execute();
				
				if (ps.getResultSet().next()) {
					// we have to evict the current concept out of the session because
					// the user probably had to change the class of this object to get it 
					// to now be a numeric
					// (must be done before the "insert into...")
					sessionFactory.getCurrentSession().clear();
					
					ps = connection.prepareStatement("INSERT INTO concept_complex (concept_id, precise) VALUES (?, false)");
					ps.setInt(1, concept.getConceptId());
					ps.executeUpdate();
				} else {
					// no stub insert is needed because either a concept row 
					// doesn't exist or a concept_numeric row does exist
				}
				
			}
			catch (SQLException e) {
				log.error("Error while trying to see if this ConceptComplex is in the concept_complex table already", e);
			}
		} else if (concept instanceof ConceptDerived) {
			// check the concept_derived table
		}
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#purgeConcept(org.openmrs.Concept)
	 */
	public void purgeConcept(Concept concept) throws DAOException {
		// must delete all the stored concept words first
		sessionFactory.getCurrentSession().createQuery("delete from ConceptWord where concept_id = :c").setInteger("c",
		    concept.getConceptId()).executeUpdate();
		
		// now we can safely delete the concept
		sessionFactory.getCurrentSession().delete(concept);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConcept(java.lang.Integer)
	 */
	public Concept getConcept(Integer conceptId) throws DAOException {
		return (Concept) sessionFactory.getCurrentSession().get(Concept.class, conceptId);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptName(java.lang.Integer)
	 */
	public ConceptName getConceptName(Integer conceptNameId) throws DAOException {
		return (ConceptName) sessionFactory.getCurrentSession().get(ConceptName.class, conceptNameId);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptAnswer(java.lang.Integer)
	 */
	public ConceptAnswer getConceptAnswer(Integer conceptAnswerId) throws DAOException {
		return (ConceptAnswer) sessionFactory.getCurrentSession().get(ConceptAnswer.class, conceptAnswerId);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getAllConcepts(java.lang.String, boolean, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> getAllConcepts(String sortBy, boolean asc, boolean includeRetired) throws DAOException {
		String sql = "from Concept concept";
		
		if (!includeRetired)
			sql += " where retired = false ";
		try {
			Concept.class.getDeclaredField(sortBy);
		}
		catch (NoSuchFieldException e) {
			try {
				ConceptName.class.getDeclaredField(sortBy);
				sortBy = "names." + sortBy;
			}
			catch (NoSuchFieldException e2) {
				sortBy = "conceptId";
			}
		}
		sql += " order by concept." + sortBy;
		if (!asc)
			sql += " desc";
		else
			sql += " asc";
		Query query = sessionFactory.getCurrentSession().createQuery(sql);
		return (List<Concept>) query.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#saveDrug(org.openmrs.Drug)
	 */
	public Drug saveDrug(Drug drug) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(drug);
		return drug;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getDrug(java.lang.Integer)
	 */
	public Drug getDrug(Integer drugId) throws DAOException {
		return (Drug) sessionFactory.getCurrentSession().get(Drug.class, drugId);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getDrugs(java.lang.String, org.openmrs.Concept, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Drug> getDrugs(String drugName, Concept concept, boolean includeRetired) throws DAOException {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(Drug.class, "drug");
		if (includeRetired == false)
			searchCriteria.add(Expression.eq("drug.retired", false));
		if (concept != null)
			searchCriteria.add(Expression.eq("drug.concept", concept));
		if (drugName != null)
			searchCriteria.add(Expression.eq("drug.name", drugName));
		return (List<Drug>) searchCriteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getDrugs(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Drug> getDrugs(String phrase) throws DAOException {
		List<String> words = ConceptWord.getUniqueWords(phrase);
		List<Drug> conceptDrugs = new Vector<Drug>();
		
		if (words.size() > 0) {
			
			Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(Drug.class, "drug");
			
			searchCriteria.add(Expression.eq("drug.retired", false));
			
			Iterator<String> word = words.iterator();
			searchCriteria.add(Expression.like("name", word.next(), MatchMode.ANYWHERE));
			while (word.hasNext()) {
				String w = word.next();
				log.debug(w);
				searchCriteria.add(Expression.like("name", w, MatchMode.ANYWHERE));
			}
			searchCriteria.addOrder(Order.asc("drug.concept"));
			conceptDrugs = searchCriteria.list();
		}
		
		return conceptDrugs;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptClass(java.lang.Integer)
	 */
	public ConceptClass getConceptClass(Integer i) throws DAOException {
		return (ConceptClass) sessionFactory.getCurrentSession().get(ConceptClass.class, i);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptClasses(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptClass> getConceptClasses(String name) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptClass.class);
		if (name != null)
			crit.add(Expression.eq("name", name));
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getAllConceptClasses(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptClass> getAllConceptClasses(boolean includeRetired) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptClass.class);
		
		// Minor bug - was assigning includeRetired instead of evaluating 
		if (includeRetired == false)
			crit.add(Expression.eq("retired", false));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#saveConceptClass(org.openmrs.ConceptClass)
	 */
	public ConceptClass saveConceptClass(ConceptClass cc) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(cc);
		return cc;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#purgeConceptClass(org.openmrs.ConceptClass)
	 */
	public void purgeConceptClass(ConceptClass cc) throws DAOException {
		sessionFactory.getCurrentSession().delete(cc);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptDatatype(java.lang.Integer)
	 */
	public ConceptDatatype getConceptDatatype(Integer i) {
		return (ConceptDatatype) sessionFactory.getCurrentSession().get(ConceptDatatype.class, i);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getAllConceptDatatypes(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptDatatype> getAllConceptDatatypes(boolean includeRetired) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptDatatype.class);
		
		if (includeRetired == false)
			crit.add(Expression.eq("retired", false));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptDatatypes(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptDatatype> getConceptDatatypes(String name) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptDatatype.class);
		
		if (name != null)
			crit.add(Expression.like("name", name, MatchMode.START));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#saveConceptDatatype(org.openmrs.ConceptDatatype)
	 */
	public ConceptDatatype saveConceptDatatype(ConceptDatatype cd) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(cd);
		return cd;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#purgeConceptDatatype(org.openmrs.ConceptDatatype)
	 */
	public void purgeConceptDatatype(ConceptDatatype cd) throws DAOException {
		sessionFactory.getCurrentSession().delete(cd);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptNumeric(java.lang.Integer)
	 */
	public ConceptNumeric getConceptNumeric(Integer i) {
		ConceptNumeric cn;
		Object obj = sessionFactory.getCurrentSession().get(ConceptNumeric.class, i);
		// If Concept has already been read & cached, we may get back a Concept instead of
		// ConceptNumeric.  If this happens, we need to clear the object from the cache
		// and re-fetch it as a ConceptNumeric
		if (obj != null && !obj.getClass().equals(ConceptNumeric.class)) {
			sessionFactory.getCurrentSession().evict(obj); // remove from cache
			// session.get() did not work here, we need to perform a query to get a ConceptNumeric
			Query query = sessionFactory.getCurrentSession().createQuery("from ConceptNumeric where conceptId = :conceptId")
			        .setParameter("conceptId", i);
			obj = query.uniqueResult();
		}
		cn = (ConceptNumeric) obj;
		
		return cn;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConcepts(java.lang.String, java.util.Locale, boolean,
	 *      java.util.List, java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> getConcepts(String name, Locale loc, boolean searchOnPhrase, List<ConceptClass> classes,
	                                 List<ConceptDatatype> datatypes) throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Concept.class);
		
		criteria.add(Expression.eq("retired", false));
		
		if (name != null) {
			if (loc == null)
				throw new DAOException("Locale must be not null");
			
			criteria.createAlias("names", "names");
			MatchMode matchmode = MatchMode.EXACT;
			if (searchOnPhrase)
				matchmode = MatchMode.ANYWHERE;
			
			criteria.add(Expression.like("names.name", name, matchmode));
			criteria.add(Expression.eq("names.locale", loc.getLanguage().substring(0, 2)));
		}
		
		if (classes.size() > 0)
			criteria.add(Expression.in("conceptClass", classes));
		
		if (datatypes.size() > 0)
			criteria.add(Expression.in("datatype", datatypes));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptWords(java.lang.String, java.util.List, boolean,
	 *      java.util.List, java.util.List, java.util.List, java.util.List, org.openmrs.Concept,
	 *      java.lang.Integer, java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptWord> getConceptWords(String phrase, List<Locale> locales, boolean includeRetired,
	                                         List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
	                                         List<ConceptDatatype> requireDatatypes, List<ConceptDatatype> excludeDatatypes,
	                                         Concept answersToConcept, Integer start, Integer size) throws DAOException {
		
		//add the language-only portion of locale if its not in the list of locales already
		List<Locale> localesToAdd = new Vector<Locale>();
		for (Locale locale : locales) {
			Locale languageOnly = new Locale(locale.getLanguage());
			if (locales.contains(languageOnly) == false)
				localesToAdd.add(languageOnly);
		}
		
		locales.addAll(localesToAdd);
		
		//String locale = loc.getLanguage().substring(0, 2);		
		List<String> words = ConceptWord.getUniqueWords(phrase); //assumes getUniqueWords() removes quote(') characters.  (otherwise we would have a security leak)
		
		// these are the answers to restrict on
		List<Concept> answers = new Vector<Concept>();
		
		if (answersToConcept != null && answersToConcept.getAnswers() != null) {
			for (ConceptAnswer conceptAnswer : answersToConcept.getAnswers()) {
				answers.add(conceptAnswer.getAnswerConcept());
			}
		}
		
		List<ConceptWord> conceptWords = new Vector<ConceptWord>();
		
		if (words.size() > 0 || !answers.isEmpty()) {
			
			Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(ConceptWord.class, "cw1");
			searchCriteria.add(Expression.in("locale", locales));
			
			if (includeRetired == false) {
				searchCriteria.createAlias("concept", "concept");
				searchCriteria.add(Expression.eq("concept.retired", false));
			}
			
			// Only restrict on answers if there are any
			if (!answers.isEmpty())
				searchCriteria.add(Expression.in("cw1.concept", answers));
			
			if (words.size() > 0) {
				Iterator<String> word = words.iterator();
				searchCriteria.add(Expression.like("word", word.next(), MatchMode.START));
				Conjunction junction = Expression.conjunction();
				while (word.hasNext()) {
					String w = word.next();
					
					if (log.isDebugEnabled())
						log.debug("Current word: " + w);
					
					DetachedCriteria crit = DetachedCriteria.forClass(ConceptWord.class).setProjection(
					    Property.forName("concept")).add(Expression.eqProperty("concept", "cw1.concept")).add(
					    Restrictions.like("word", w, MatchMode.START)).add(Expression.in("locale", locales));
					junction.add(Subqueries.exists(crit));
				}
				searchCriteria.add(junction);
			}
			
			if (requireClasses.size() > 0)
				searchCriteria.add(Expression.in("concept.conceptClass", requireClasses));
			
			if (excludeClasses.size() > 0)
				searchCriteria.add(Expression.not(Expression.in("concept.conceptClass", excludeClasses)));
			
			if (requireDatatypes.size() > 0)
				searchCriteria.add(Expression.in("concept.datatype", requireDatatypes));
			
			if (excludeDatatypes.size() > 0)
				searchCriteria.add(Expression.not(Expression.in("concept.datatype", excludeDatatypes)));
			
			searchCriteria.addOrder(Order.asc("synonym"));
			conceptWords = searchCriteria.list();
			
			// trim down the list 
			// TODO: put this in the criteria object?
			if (start != null && size != null) {
				List<ConceptWord> subList = conceptWords.subList(start, start + size);
				return subList;
			}
		}
		
		if (log.isDebugEnabled())
			log.debug("ConceptWords found: " + conceptWords.size());
		
		return conceptWords;
	}
	
	/**
	 * gets questions for the given answer concept
	 * 
	 * @see org.openmrs.api.db.ConceptDAO#getConceptsByAnswer(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> getConceptsByAnswer(Concept concept) {
		// TODO broken until Hibernate fixes component and HQL code
		String q = "select c from Concept c join c.answers ca where ca.answerConcept = :answer";
		Query query = sessionFactory.getCurrentSession().createQuery(q);
		query.setParameter("answer", concept);
		
		return query.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getPrevConcept(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public Concept getPrevConcept(Concept c) {
		Integer i = c.getConceptId();
		
		List<Concept> concepts = sessionFactory.getCurrentSession().createCriteria(Concept.class).add(
		    Expression.lt("conceptId", i)).addOrder(Order.desc("conceptId")).setFetchSize(1).list();
		
		if (concepts.size() < 1)
			return null;
		return concepts.get(0);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getNextConcept(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public Concept getNextConcept(Concept c) {
		Integer i = c.getConceptId();
		
		List<Concept> concepts = sessionFactory.getCurrentSession().createCriteria(Concept.class).add(
		    Expression.gt("conceptId", i)).addOrder(Order.asc("conceptId")).setFetchSize(1).list();
		
		if (concepts.size() < 1)
			return null;
		return concepts.get(0);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptsWithDrugsInFormulary()
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> getConceptsWithDrugsInFormulary() {
		Query query = sessionFactory.getCurrentSession().createQuery(
		    "select distinct concept from Drug d where d.retired = false");
		return query.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#purgeDrug(org.openmrs.Drug)
	 */
	public void purgeDrug(Drug drug) throws DAOException {
		sessionFactory.getCurrentSession().delete(drug);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#updateConceptWord(org.openmrs.Concept)
	 */
	public void updateConceptWord(Concept concept) throws DAOException {
		log.debug("updateConceptWord(" + concept + ")");
		if (concept != null) {
			// remove all old words
			if (concept.getConceptId() != null && concept.getConceptId() > 0)
				
				deleteConceptWord(concept);
			
			// add all new words
			Collection<ConceptWord> words = ConceptWord.makeConceptWords(concept);
			log.debug("words: " + words);
			for (ConceptWord word : words) {
				try {
					sessionFactory.getCurrentSession().save(word);
				}
				catch (NonUniqueObjectException e) {
					ConceptWord tmp = (ConceptWord) sessionFactory.getCurrentSession().merge(word);
					sessionFactory.getCurrentSession().evict(tmp);
					sessionFactory.getCurrentSession().save(word);
				}
			}
		}
	}
	
	/**
	 * Deletes all concept words for a concept. Called by {@link #updateConceptWord(Concept)}
	 * 
	 * @param concept
	 * @throws DAOException
	 */
	@SuppressWarnings("unchecked")
	private void deleteConceptWord(Concept concept) throws DAOException {
		log.debug("deletConceptWord(" + concept + ")");
		if (concept != null) {
			if (log.isDebugEnabled()) {
				Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptWord.class);
				crit.add(Expression.eq("concept", concept));
				
				List<ConceptWord> words = crit.list();
				
				Integer authUserId = null;
				if (Context.isAuthenticated())
					authUserId = Context.getAuthenticatedUser().getUserId();
				
				log.debug(authUserId + "|ConceptWord|" + words);
			}
			sessionFactory.getCurrentSession().createQuery("delete from ConceptWord where concept_id = :c").setInteger("c",
			    concept.getConceptId()).executeUpdate();
		}
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#saveConceptProposal(org.openmrs.ConceptProposal)
	 */
	public ConceptProposal saveConceptProposal(ConceptProposal cp) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(cp);
		return cp;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#purgeConceptProposal(org.openmrs.ConceptProposal)
	 */
	public void purgeConceptProposal(ConceptProposal cp) throws DAOException {
		sessionFactory.getCurrentSession().delete(cp);
		return;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getAllConceptProposals(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptProposal> getAllConceptProposals(boolean includeCompleted) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptProposal.class);
		
		if (includeCompleted == false) {
			crit.add(Expression.eq("state", OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED));
		}
		crit.addOrder(Order.asc("originalText"));
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptProposal(java.lang.Integer)
	 */
	public ConceptProposal getConceptProposal(Integer conceptProposalId) throws DAOException {
		return (ConceptProposal) sessionFactory.getCurrentSession().get(ConceptProposal.class, conceptProposalId);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptProposals(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptProposal> getConceptProposals(String text) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptProposal.class);
		crit.add(Expression.eq("state", OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED));
		crit.add(Expression.eq("originalText", text));
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getProposedConcepts(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> getProposedConcepts(String text) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptProposal.class);
		crit.add(Expression.ne("state", OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED));
		crit.add(Expression.eq("originalText", text));
		crit.add(Expression.isNotNull("mappedConcept"));
		crit.setProjection(Projections.distinct(Projections.property("mappedConcept")));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptSetsByConcept(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptSet> getConceptSetsByConcept(Concept concept) {
		return sessionFactory.getCurrentSession().createCriteria(ConceptSet.class).add(
		    Restrictions.eq("conceptSet", concept)).addOrder(Order.asc("sortWeight")).list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getSetsContainingConcept(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptSet> getSetsContainingConcept(Concept concept) {
		return sessionFactory.getCurrentSession().createCriteria(ConceptSet.class).add(Restrictions.eq("concept", concept))
		        .list();
	}
	
	//TODO:  eventually, this method should probably just run updateConceptSetDerived(Concept) inside an iteration of all concepts... (or something else less transactionally-intense)
	/**
	 * @see org.openmrs.api.db.ConceptDAO#updateConceptSetDerived()
	 */
	public void updateConceptSetDerived() throws DAOException {
		sessionFactory.getCurrentSession().createQuery("delete from ConceptSetDerived").executeUpdate();
		try {
			// remake the derived table by copying over the basic concept_set table
			sessionFactory
			        .getCurrentSession()
			        .connection()
			        .prepareStatement(
			            "insert into concept_set_derived (concept_id, concept_set, sort_weight) select cs.concept_id, cs.concept_set, cs.sort_weight from concept_set cs where not exists (select concept_id from concept_set_derived csd where csd.concept_id = cs.concept_id and csd.concept_set = cs.concept_set)")
			        .execute();
			
			// burst the concept sets -- make grandchildren direct children of grandparents
			sessionFactory
			        .getCurrentSession()
			        .connection()
			        .prepareStatement(
			            "insert into concept_set_derived (concept_id, concept_set, sort_weight) select cs1.concept_id, cs2.concept_set, cs1.sort_weight from concept_set cs1 join concept_set cs2 where cs2.concept_id = cs1.concept_set and not exists (select concept_id from concept_set_derived csd where csd.concept_id = cs1.concept_id and csd.concept_set = cs2.concept_set)")
			        .execute();
			
			// burst the concept sets -- make greatgrandchildren direct child of greatgrandparents
			sessionFactory
			        .getCurrentSession()
			        .connection()
			        .prepareStatement(
			            "insert into concept_set_derived (concept_id, concept_set, sort_weight) select cs1.concept_id, cs3.concept_set, cs1.sort_weight from concept_set cs1 join concept_set cs2 join concept_set cs3 where cs1.concept_set = cs2.concept_id and cs2.concept_set = cs3.concept_id and not exists (select concept_id from concept_set_derived csd where csd.concept_id = cs1.concept_id and csd.concept_set = cs3.concept_set)")
			        .execute();
			
			// TODO This 'algorithm' only solves three layers of children.  Options for correction:
			//	1) Add a few more join statements to cover 5 layers (conceivable upper limit of layers)
			//	2) Find the deepest layer and programmatically create the sql statements
			//	3) Run the joins on 
		}
		catch (SQLException e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * utility method used in updateConceptSetDerived(...)
	 * 
	 * @param List of parent Concept objects
	 * @param Concept current
	 * @return Set of ConceptSetDerived
	 */
	private Set<ConceptSetDerived> deriveChildren(List<Concept> parents, Concept current) {
		Set<ConceptSetDerived> updates = new HashSet<ConceptSetDerived>();
		
		ConceptSetDerived derivedSet = null;
		// make each child a direct child of each parent/grandparent
		for (ConceptSet childSet : current.getConceptSets()) {
			Concept child = childSet.getConcept();
			log.debug("Deriving child: " + child.getConceptId());
			Double sort_weight = childSet.getSortWeight();
			for (Concept parent : parents) {
				log.debug("Matching child: " + child.getConceptId() + " with parent: " + parent.getConceptId());
				derivedSet = new ConceptSetDerived(parent, child, sort_weight++);
				updates.add(derivedSet);
			}
			
			//recurse if this child is a set as well
			if (child.isSet()) {
				log.debug("Concept id: " + child.getConceptId() + " is a set");
				List<Concept> new_parents = new Vector<Concept>();
				new_parents.addAll(parents);
				new_parents.add(child);
				updates.addAll(deriveChildren(new_parents, child));
			}
		}
		
		return updates;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#updateConceptSetDerived(org.openmrs.Concept)
	 */
	public void updateConceptSetDerived(Concept concept) throws DAOException {
		log.debug("Updating concept set derivisions for #" + concept.getConceptId().toString());
		
		// deletes current concept's sets and matching parent's sets
		
		//recursively get all parents
		List<Concept> parents = getParents(concept);
		
		// delete this concept's children and their bursted parents
		for (Concept parent : parents) {
			sessionFactory
			        .getCurrentSession()
			        .createQuery(
			            "delete from ConceptSetDerived csd where csd.concept in (select cs.concept from ConceptSet cs where cs.conceptSet = :c) and csd.conceptSet = :parent)")
			        .setParameter("c", concept).setParameter("parent", parent).executeUpdate();
		}
		
		//set of updates to be passed to the server (unique list)
		Set<ConceptSetDerived> updates = new HashSet<ConceptSetDerived>();
		
		//add parents as sets of parents below
		ConceptSetDerived csd;
		for (Integer a = 0; a < parents.size() - 1; a++) {
			Concept set = parents.get(a);
			for (Integer b = a + 1; b < parents.size(); b++) {
				Concept conc = parents.get(b);
				csd = new ConceptSetDerived(set, conc, Double.valueOf(b.doubleValue()));
				updates.add(csd);
			}
		}
		
		//recursively add parents to children
		updates.addAll(deriveChildren(parents, concept));
		
		for (ConceptSetDerived c : updates) {
			sessionFactory.getCurrentSession().saveOrUpdate(c);
		}
		
	}
	
	/**
	 * returns a list of n-generations of parents of a concept in a concept set
	 * 
	 * @param Concept current
	 * @return List<Concept>
	 * @throws DAOException
	 */
	@SuppressWarnings("unchecked")
	private List<Concept> getParents(Concept current) throws DAOException {
		List<Concept> parents = new Vector<Concept>();
		if (current != null) {
			Query query = sessionFactory.getCurrentSession().createQuery(
			    "from Concept c join c.conceptSets sets where sets.concept = ?").setEntity(0, current);
			List<Concept> immed_parents = query.list();
			for (Concept c : immed_parents) {
				parents.addAll(getParents(c));
			}
			parents.add(current);
			if (log.isDebugEnabled()) {
				log.debug("parents found: ");
				for (Concept c : parents) {
					log.debug("id: " + c.getConceptId());
				}
			}
		}
		return parents;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getLocalesOfConceptNames()
	 */
	public Set<Locale> getLocalesOfConceptNames() {
		Set<Locale> locales = new HashSet<Locale>();
		
		Query query = sessionFactory.getCurrentSession().createQuery("select distinct locale from ConceptName");
		
		for (Object locale : query.list()) {
			locales.add((Locale) locale);
		}
		
		return locales;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptNameTag(java.lang.Integer)
	 */
	public ConceptNameTag getConceptNameTag(Integer i) {
		return (ConceptNameTag) sessionFactory.getCurrentSession().get(ConceptNameTag.class, i);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptNameTagByName(java.lang.String)
	 */
	public ConceptNameTag getConceptNameTagByName(String name) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptNameTag.class).add(
		    Expression.eq("tag", name));
		
		if (crit.list().size() < 1) {
			log.warn("No concept name tag found with name: " + name);
			return null;
		}
		
		return (ConceptNameTag) crit.list().get(0);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getAllConceptNameTags()
	 */
	public List<ConceptNameTag> getAllConceptNameTags() {
		return sessionFactory.getCurrentSession().createQuery("from ConceptNameTag cnt order by cnt.tag").list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptSource(java.lang.Integer)
	 */
	public ConceptSource getConceptSource(Integer conceptSourceId) {
		return (ConceptSource) sessionFactory.getCurrentSession().get(ConceptSource.class, conceptSourceId);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getAllConceptSources()
	 */
	public List<ConceptSource> getAllConceptSources() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptSource.class);
		
		criteria.add(Expression.eq("voided", false));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#deleteConceptSource(org.openmrs.ConceptSource)
	 */
	public ConceptSource deleteConceptSource(ConceptSource cs) throws DAOException {
		sessionFactory.getCurrentSession().delete(cs);
		return cs;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#saveConceptSource(org.openmrs.ConceptSource)
	 */
	public ConceptSource saveConceptSource(ConceptSource conceptSource) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(conceptSource);
		return conceptSource;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#saveConceptNameTag(org.openmrs.ConceptNameTag)
	 */
	public ConceptNameTag saveConceptNameTag(ConceptNameTag nameTag) {
		if (nameTag == null)
			return null;
		ConceptNameTag returnedTag = getConceptNameTagByName(nameTag.getTag());
		if (returnedTag == null) {
			returnedTag = nameTag;
			sessionFactory.getCurrentSession().saveOrUpdate(nameTag);
		}
		return returnedTag;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getMaxConceptId()
	 */
	public Integer getMinConceptId() {
		Query query = sessionFactory.getCurrentSession().createQuery("select min(conceptId) from Concept");
		return (Integer) query.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getMaxConceptId()
	 */
	public Integer getMaxConceptId() {
		Query query = sessionFactory.getCurrentSession().createQuery("select max(conceptId) from Concept");
		return (Integer) query.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#conceptIterator()
	 */
	public Iterator<Concept> conceptIterator() {
		return new ConceptIterator();
	}
	
	/**
	 * An iterator that loops over all concepts in the dictionary one at a time
	 */
	private class ConceptIterator implements Iterator<Concept> {
		
		Concept currentConcept = null;
		
		Concept nextConcept;
		
		public ConceptIterator() {
			final int firstConceptId = getMinConceptId();
			nextConcept = getConcept(firstConceptId);
		}
		
		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return (nextConcept != null);
		}
		
		/**
		 * @see java.util.Iterator#next()
		 */
		public Concept next() {
			if (currentConcept != null) {
				sessionFactory.getCurrentSession().evict(currentConcept);
			}
			
			currentConcept = nextConcept;
			
			nextConcept = getNextConcept(currentConcept);
			
			return currentConcept;
		}
		
		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
}
