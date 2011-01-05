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
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSetDerived;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.api.ConceptNameType;
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
		
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		
		// check the concept_numeric table
		if (concept instanceof ConceptNumeric) {
			
			try {
				ps = connection
				        .prepareStatement("SELECT * FROM concept WHERE concept_id = ? and not exists (select * from concept_numeric WHERE concept_id = ?)");
				ps.setInt(1, concept.getConceptId());
				ps.setInt(2, concept.getConceptId());
				ps.execute();
				
				// Converting to concept numeric:  A single concept row exists, but concept numeric has not been populated yet.
				if (ps.getResultSet().next()) {
					// we have to evict the current concept out of the session because
					// the user probably had to change the class of this object to get it
					// to now be a numeric
					// (must be done before the "insert into...")
					sessionFactory.getCurrentSession().clear();
					
					ps2 = connection.prepareStatement("INSERT INTO concept_numeric (concept_id, precise) VALUES (?, false)");
					ps2.setInt(1, concept.getConceptId());
					ps2.executeUpdate();
				}
				// Converting from concept numeric:  The concept and concept numeric rows both exist, so we need to delete concept_numeric.
				else {
					//concept is changed from numeric to something else
					// hence row should be deleted from the concept_numeric
					if (!concept.isNumeric()) {
						ps2 = connection.prepareStatement("DELETE FROM concept_numeric WHERE concept_id = ?");
						ps2.setInt(1, concept.getConceptId());
						ps2.executeUpdate();
					} else {
						// it is indeed numeric now... don't delete
					}
				}
			}
			catch (SQLException e) {
				log.error("Error while trying to see if this ConceptNumeric is in the concept_numeric table already", e);
			}
			finally {
				if (ps != null) {
					try {
						ps.close();
					}
					catch (SQLException e) {
						log.error("Error generated while closing statement", e);
					}
				}
				if (ps2 != null) {
					try {
						ps2.close();
					}
					catch (SQLException e) {
						log.error("Error generated while closing statement", e);
					}
				}
			}
		}
		// check the concept complex table
		else if (concept instanceof ConceptComplex) {
			
			try {
				ps = connection
				        .prepareStatement("SELECT * FROM concept WHERE concept_id = ? and not exists (select * from concept_complex WHERE concept_id = ?)");
				ps.setInt(1, concept.getConceptId());
				ps.setInt(2, concept.getConceptId());
				ps.execute();
				
				// Converting to concept complex:  A single concept row exists, but concept complex has not been populated yet.
				if (ps.getResultSet().next()) {
					// we have to evict the current concept out of the session because
					// the user probably had to change the class of this object to get it
					// to now be a ConceptComplex
					// (must be done before the "insert into...")
					sessionFactory.getCurrentSession().clear();
					
					// Add an empty row into the concept_complex table
					ps2 = connection.prepareStatement("INSERT INTO concept_complex (concept_id) VALUES (?)");
					ps2.setInt(1, concept.getConceptId());
					ps2.executeUpdate();
				}
				// Converting from concept complex:  The concept and concept complex rows both exist, so we need to delete the concept_complex row.
				// no stub insert is needed because either a concept row doesn't exist OR a concept_complex row does exist
				else {
					// concept is changed from complex to something else
					// hence row should be deleted from the concept_complex
					if (!concept.isComplex()) {
						ps2 = connection.prepareStatement("DELETE FROM concept_complex WHERE concept_id = ?");
						ps2.setInt(1, concept.getConceptId());
						ps2.executeUpdate();
					} else {
						// it is indeed numeric now... don't delete
					}
					
				}
			}
			catch (SQLException e) {
				log.error("Error while trying to see if this ConceptComplex is in the concept_complex table already", e);
			}
			finally {
				if (ps != null) {
					try {
						ps.close();
					}
					catch (SQLException e) {
						log.error("Error generated while closing statement", e);
					}
				}
				if (ps2 != null) {
					try {
						ps2.close();
					}
					catch (SQLException e) {
						log.error("Error generated while closing statement", e);
					}
				}
			}
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
	 * @see org.openmrs.api.db.ConceptDAO#getConceptDatatypeByName(String)
	 */
	public ConceptDatatype getConceptDatatypeByName(String name) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptDatatype.class);
		if (name != null) {
			criteria.add(Restrictions.eq("name", name));
		}
		return (ConceptDatatype) criteria.uniqueResult();
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
			
			criteria.add(Expression.ilike("names.name", name, matchmode));
			
			String language = loc.getLanguage();
			if (language.length() > 2) {
				// if searching in specific locale like en_US
				criteria.add(Expression.or(Expression.eq("names.locale", loc.getLanguage()), Expression.eq("names.locale",
				    loc.getLanguage().substring(0, 2))));
			} else {
				// if searching in general locale like just "en"
				criteria.add(Expression.like("names.locale", loc.getLanguage(), MatchMode.START));
			}
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
		
		Criteria searchCriteria = createConceptWordSearchCriteria(phrase, locales, includeRetired, requireClasses,
		    excludeClasses, requireDatatypes, excludeDatatypes, answersToConcept);
		searchCriteria.addOrder(Order.desc("cw1.weight"));
		List<ConceptWord> conceptWords = new Vector<ConceptWord>();
		if (searchCriteria != null) {
			if (start != null)
				searchCriteria.setFirstResult(start);
			if (size != null && size > 0)
				searchCriteria.setMaxResults(size);
			
			return searchCriteria.list();
		}
		
		if (log.isDebugEnabled())
			log.debug("No matching ConceptWords found");
		
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
				word.setWeight(weighConceptWord(word));
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
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
	public List<ConceptSource> getAllConceptSources() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptSource.class);
		
		criteria.add(Expression.eq("retired", false));
		
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
		
		sessionFactory.getCurrentSession().saveOrUpdate(nameTag);
		return nameTag;
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
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptsByMapping(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> getConceptsByMapping(String code, String sourceName, boolean includeRetired) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptMap.class);
		
		// make this criteria return a list of concepts
		criteria.setProjection(Projections.property("concept"));
		
		// match the source code to the passed code
		criteria.add(Expression.eq("sourceCode", code));
		
		// join to conceptSource and match to the h17Code or source name
		criteria.createAlias("source", "conceptSource");
		criteria.add(Expression.or(Expression.eq("conceptSource.name", sourceName), Expression.eq("conceptSource.hl7Code",
		    sourceName)));
		
		if (!includeRetired) {
			// ignore retired concepts
			criteria.createAlias("concept", "concept");
			criteria.add(Expression.eq("concept.retired", false));
		} else {
			// sort retired concepts to the end of the list
			criteria.createAlias("concept", "concept");
			criteria.addOrder(Order.asc("concept.retired"));
		}
		
		// we only want distinct concepts
		criteria.setResultTransformer(new DistinctRootEntityResultTransformer());
		
		return (List<Concept>) criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptByUuid(java.lang.String)
	 */
	public Concept getConceptByUuid(String uuid) {
		return (Concept) sessionFactory.getCurrentSession().createQuery("from Concept c where c.uuid = :uuid").setString(
		    "uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptClassByUuid(java.lang.String)
	 */
	public ConceptClass getConceptClassByUuid(String uuid) {
		return (ConceptClass) sessionFactory.getCurrentSession().createQuery("from ConceptClass cc where cc.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	public ConceptAnswer getConceptAnswerByUuid(String uuid) {
		return (ConceptAnswer) sessionFactory.getCurrentSession().createQuery("from ConceptAnswer cc where cc.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	public ConceptName getConceptNameByUuid(String uuid) {
		return (ConceptName) sessionFactory.getCurrentSession().createQuery("from ConceptName cc where cc.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	public ConceptSet getConceptSetByUuid(String uuid) {
		return (ConceptSet) sessionFactory.getCurrentSession().createQuery("from ConceptSet cc where cc.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	public ConceptSetDerived getConceptSetDerivedByUuid(String uuid) {
		return (ConceptSetDerived) sessionFactory.getCurrentSession().createQuery(
		    "from ConceptSetDerived cc where cc.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	public ConceptSource getConceptSourceByUuid(String uuid) {
		return (ConceptSource) sessionFactory.getCurrentSession().createQuery("from ConceptSource cc where cc.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	public ConceptWord getConceptWordByUuid(String uuid) {
		return (ConceptWord) sessionFactory.getCurrentSession().createQuery("from ConceptWord cc where cc.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptDatatypeByUuid(java.lang.String)
	 */
	public ConceptDatatype getConceptDatatypeByUuid(String uuid) {
		return (ConceptDatatype) sessionFactory.getCurrentSession().createQuery(
		    "from ConceptDatatype cd where cd.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptNumericByUuid(java.lang.String)
	 */
	public ConceptNumeric getConceptNumericByUuid(String uuid) {
		return (ConceptNumeric) sessionFactory.getCurrentSession().createQuery(
		    "from ConceptNumeric cn where cn.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptProposalByUuid(java.lang.String)
	 */
	public ConceptProposal getConceptProposalByUuid(String uuid) {
		return (ConceptProposal) sessionFactory.getCurrentSession().createQuery(
		    "from ConceptProposal cp where cp.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getDrugByUuid(java.lang.String)
	 */
	public Drug getDrugByUuid(String uuid) {
		return (Drug) sessionFactory.getCurrentSession().createQuery("from Drug d where d.uuid = :uuid").setString("uuid",
		    uuid).uniqueResult();
	}
	
	public DrugIngredient getDrugIngredientByUuid(String uuid) {
		return (DrugIngredient) sessionFactory.getCurrentSession().createQuery("from DrugIngredient d where d.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptUuids()
	 */
	public Map<Integer, String> getConceptUuids() {
		Map<Integer, String> ret = new HashMap<Integer, String>();
		Query q = sessionFactory.getCurrentSession().createQuery("select conceptId, uuid from Concept");
		List<Object[]> list = q.list();
		for (Object[] o : list)
			ret.put((Integer) o[0], (String) o[1]);
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptDescriptionByUuid(java.lang.String)
	 */
	public ConceptDescription getConceptDescriptionByUuid(String uuid) {
		return (ConceptDescription) sessionFactory.getCurrentSession().createQuery(
		    "from ConceptDescription cd where cd.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptNameTagByUuid(java.lang.String)
	 */
	public ConceptNameTag getConceptNameTagByUuid(String uuid) {
		return (ConceptNameTag) sessionFactory.getCurrentSession().createQuery(
		    "from ConceptNameTag cnt where cnt.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptsByConceptSourceName(java.lang.String,
	 *      java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptMap> getConceptsByConceptSource(ConceptSource conceptSource) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptMap.class, "map");
		criteria.add(Expression.eq("map.source", conceptSource));
		return (List<ConceptMap>) criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptSourceByName(java.lang.String)
	 */
	public ConceptSource getConceptSourceByName(String conceptSourceName) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptSource.class, "source");
		criteria.add(Expression.eq("source.name", conceptSourceName));
		return (ConceptSource) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getSavedConceptDatatype(org.openmrs.Concept)
	 */
	public ConceptDatatype getSavedConceptDatatype(Concept concept) {
		SQLQuery sql = sessionFactory.getCurrentSession().createSQLQuery(
		    "select datatype.* from " + "concept_datatype datatype, " + "concept concept " + "where "
		            + "datatype.concept_datatype_id = concept.datatype_id " + "and concept.concept_id=:conceptId")
		        .addEntity(ConceptDatatype.class);
		sql.setInteger("conceptId", concept.getConceptId());
		return (ConceptDatatype) sql.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getSavedConceptName(org.openmrs.ConceptName)
	 */
	@Override
	public ConceptName getSavedConceptName(ConceptName conceptName) {
		sessionFactory.getCurrentSession().refresh(conceptName);
		return conceptName;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getCountOfConceptWords(String, List, boolean, List, List,
	 *      List, List, Concept)
	 */
	@Override
	public Integer getCountOfConceptWords(String phrase, List<Locale> locales, boolean includeRetired,
	                                      List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
	                                      List<ConceptDatatype> requireDatatypes, List<ConceptDatatype> excludeDatatypes,
	                                      Concept answersToConcept, boolean forUniqueConcepts) {
		Criteria searchCriteria = createConceptWordSearchCriteria(phrase, locales, includeRetired, requireClasses,
		    excludeClasses, requireDatatypes, excludeDatatypes, answersToConcept);
		if (searchCriteria != null) {
			if (forUniqueConcepts)
				searchCriteria.setProjection(Projections.countDistinct("concept"));
			else
				searchCriteria.setProjection(Projections.rowCount());
			
			return (Integer) searchCriteria.uniqueResult();
		}
		
		return 0;
	}
	
	/**
	 * Utility method that returns a criteria for searching for conceptWords that match the
	 * specified search phrase and arguments
	 * 
	 * @param phrase matched to the start of any word in any of the names of a concept
	 * @param locales List<Locale> to restrict to
	 * @param includeRetired boolean if false, will exclude retired concepts
	 * @param requireClasses List<ConceptClass> to restrict to
	 * @param excludeClasses List<ConceptClass> to leave out of results
	 * @param requireDatatypes List<ConceptDatatype> to restrict to
	 * @param excludeDatatypes List<ConceptDatatype> to leave out of results
	 * @param answersToConcept all results will be a possible answer to this concept
	 * @param start all results less than this number will be removed
	 * @param size if non zero, all results after <code>start</code> + <code>size</code> will be
	 *            removed
	 * @return the generated criteria object
	 */
	private Criteria createConceptWordSearchCriteria(String phrase, List<Locale> locales, boolean includeRetired,
	                                                 List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
	                                                 List<ConceptDatatype> requireDatatypes,
	                                                 List<ConceptDatatype> excludeDatatypes, Concept answersToConcept)
	                                                                                                                  throws DAOException {
		
		//add the language-only portion of locale if its not in the list of locales already
		List<Locale> localesToAdd = new Vector<Locale>();
		for (Locale locale : locales) {
			Locale languageOnly = new Locale(locale.getLanguage());
			if (locales.contains(languageOnly) == false)
				localesToAdd.add(languageOnly);
		}
		
		locales.addAll(localesToAdd);
		
		//assumes getUniqueWords() removes quote(') characters.  (otherwise we would have a security leak)
		List<String> words = ConceptWord.getUniqueWords(phrase);
		
		// these are the answers to restrict on
		List<Concept> answers = new Vector<Concept>();
		
		if (answersToConcept != null && answersToConcept.getAnswers() != null) {
			for (ConceptAnswer conceptAnswer : answersToConcept.getAnswers()) {
				answers.add(conceptAnswer.getAnswerConcept());
			}
		}
		
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
			
			return searchCriteria;
		}
		
		return null;
	}
	
	/**
	 * @see ConceptService#getCountOfDrugs(String, Concept, boolean, boolean)
	 */
	public Integer getCountOfDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	                               boolean includeRetired) throws DAOException {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(Drug.class, "drug");
		if (StringUtils.isBlank(drugName) && concept == null)
			return 0;
		
		if (includeRetired == false)
			searchCriteria.add(Expression.eq("drug.retired", false));
		if (concept != null)
			searchCriteria.add(Expression.eq("drug.concept", concept));
		MatchMode matchMode = MatchMode.START;
		if (searchOnPhrase)
			matchMode = MatchMode.ANYWHERE;
		if (!StringUtils.isBlank(drugName)) {
			searchCriteria.add(Expression.ilike("drug.name", drugName, matchMode));
			if (searchDrugConceptNames) {
				searchCriteria.createCriteria("concept", "concept").createAlias("concept.names", "names");
				searchCriteria.add(Expression.ilike("names.name", drugName, matchMode));
			}
		}
		
		searchCriteria.setProjection(Projections.rowCount());
		
		return (Integer) searchCriteria.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Drug> getDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	                           boolean includeRetired, Integer start, Integer length) throws DAOException {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(Drug.class, "drug");
		if (StringUtils.isBlank(drugName) && concept == null)
			return Collections.emptyList();
		
		if (includeRetired == false)
			searchCriteria.add(Expression.eq("drug.retired", false));
		if (concept != null)
			searchCriteria.add(Expression.eq("drug.concept", concept));
		MatchMode matchMode = MatchMode.START;
		if (searchOnPhrase)
			matchMode = MatchMode.ANYWHERE;
		if (!StringUtils.isBlank(drugName)) {
			searchCriteria.add(Expression.ilike("drug.name", drugName, matchMode));
			if (searchDrugConceptNames) {
				searchCriteria.createCriteria("concept", "concept").createAlias("concept.names", "names");
				searchCriteria.add(Expression.ilike("names.name", drugName, matchMode));
			}
		}
		
		if (start != null)
			searchCriteria.setFirstResult(start);
		if (length != null && length > 0)
			searchCriteria.setMaxResults(length);
		
		return searchCriteria.list();
	}
	
	/**
	 * @see ConceptDAO#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer,
	 *      Integer)
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" })
	@Override
	public List<ConceptSearchResult> getConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	                                             List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
	                                             List<ConceptDatatype> requireDatatypes,
	                                             List<ConceptDatatype> excludeDatatypes, Concept answersToConcept,
	                                             Integer start, Integer size) throws DAOException {
		
		Criteria searchCriteria = createConceptWordSearchCriteria(phrase, locales, includeRetired, requireClasses,
		    excludeClasses, requireDatatypes, excludeDatatypes, answersToConcept);
		
		List<ConceptSearchResult> results = new Vector<ConceptSearchResult>();
		
		if (searchCriteria != null) {
			ProjectionList pl = Projections.projectionList();
			pl.add(Projections.distinct(Projections.property("concept")), "cw1.concept");
			pl.add(Projections.property("word"), "cw1.word");
			pl.add(Projections.property("weight"), "cw1.weight");
			pl.add(Projections.property("locale"), "cw1.locale");
			pl.add(Projections.property("conceptName"), "cw1.conceptName");
			searchCriteria.setProjection(pl);
			
			searchCriteria.addOrder(Order.desc("cw1.weight"));
			
			if (start != null)
				searchCriteria.setFirstResult(start);
			if (size != null && size > 0)
				searchCriteria.setMaxResults(size);
			
			searchCriteria.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
			List resultObjects = searchCriteria.list();
			
			for (Object obj : resultObjects) {
				Map aliasEntityMap = (Map) obj;
				results.add(new ConceptSearchResult((String) aliasEntityMap.get("cw1.word"), (Concept) aliasEntityMap
				        .get("cw1.concept"), (ConceptName) aliasEntityMap.get("cw1.conceptName"), (Double) aliasEntityMap
				        .get("cw1.weight")));
			}
		}
		
		return results;
	}
	
	/**
	 * Utility methods that computes and sets the weight of a conceptWord. The weights are computed
	 * independent of locale and any phrase
	 * 
	 * @param word the word for which to compute the weight
	 * @return the weight of the word
	 */
	public Double weighConceptWord(ConceptWord word) {
		//TODO Add unit tests
		Double weight = 0.0;
		String conceptName = word.getConceptName().getName();
		String wordString = word.getWord();
		//why is this the case, this seems like invalid data
		if (conceptName.indexOf(wordString) < 0)
			return weight;
		
		//by default every word must at least weigh 1+
		weight = 1.0;
		//TODO make the numbers 5.0, 3.0, 1.0 etc constants
		//Index terms rank highly since they were added for searching
		
		//This is the actual match
		if (conceptName.equals(wordString)) {
			double weightCoefficient = 5.0;
			weight += weightCoefficient;
			
			//compute bonus based on the concept name type
			weight += computeBonusWeight(weightCoefficient, word);
		} else if (conceptName.startsWith(wordString)) {
			double weightCoefficient = 3.0;
			
			//the shorter the word, the higher the increment since it a closer match to the name
			// e.g MY in 'MY DEPOT' should weigh more than HOME in 'HOME DEPOT'
			weight += (weightCoefficient / wordString.length());
			weight += computeBonusWeight(weightCoefficient, word);
		} else {
			double weightCoefficient = 1.0;
			
			//still a shorter word should weigh more depending on its index in the full concept name
			//e.g MY in 'IN MY HOME' should weigh more than 'MY' in 'FOR MY HOME', we add 1 so that
			// if 'conceptName.indexOf(wordString)' returns 1, we still divide 5 by something greater than 1
			//e.g 'MARRIAGE' in 'PRE MARRIAGE' should weigh more than 'MARRIAGE' in 'NOT PRE MARRIAGE'
			//and still weigh more then 'MARRIAGE' in 'PRE MARRIAGE RELATIONSHIP'
			weight += ((weightCoefficient / (conceptName.indexOf(wordString) + 1)) * ((conceptName.length() - wordString
			        .length()) / new Double(conceptName.length())));
			weight += computeBonusWeight(weightCoefficient, word);
		}
		
		//round off to 2 decimal places
		return Double.parseDouble(new DecimalFormat("0.00").format(weight));
	}
	
	/**
	 * Utility method that computes the bonus weight for a concept word based on the
	 * {@link ConceptNameType}, the length of the full concept name and the weightCoefficient
	 * 
	 * @param weightCoefficient
	 * @param word
	 * @return
	 */
	private double computeBonusWeight(Double weightCoefficient, ConceptWord word) {
		double bonusWeight = 0.0;
		ConceptName conceptName = word.getConceptName();
		if (conceptName.isIndexTerm()
		        || (word.getConceptName().isPreferred() && word.getConceptName().isFullySpecifiedName()))
			bonusWeight += weightCoefficient * 0.9;
		else if (conceptName.isPreferred())
			bonusWeight += weightCoefficient * 0.7;
		else if (conceptName.isFullySpecifiedName())
			bonusWeight += weightCoefficient * 0.5;
		else if (conceptName.isSynonym())
			bonusWeight += weightCoefficient * 0.3;
		else if (conceptName.isShort())
			bonusWeight += weightCoefficient * 0.1;
		
		//the shorter the full concept name, the higher the weigth, the word 'MEASELS' in 
		//'MEASELS ON EARTH' should weigh more than another 'MEASELS' in 'MEASELS ON JUPITER'
		bonusWeight += weightCoefficient / new Double(word.getConceptName().getName().length());
		
		return bonusWeight;
	}
}
