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
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptStopWord;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.search.LuceneQuery;
import org.openmrs.collection.ListPart;
import org.openmrs.util.ConceptMapTypeComparator;
import org.openmrs.util.OpenmrsConstants;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
	
	private final Function<ConceptName, Concept> transformNameToConcept = new Function<ConceptName, Concept>() {
		
		@Override
		public Concept apply(ConceptName name) {
			return name.getConcept();
		}
	};
	
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
		
		boolean isNameField = false;
		
		try {
			Concept.class.getDeclaredField(sortBy);
		}
		catch (NoSuchFieldException e) {
			try {
				ConceptName.class.getDeclaredField(sortBy);
				isNameField = true;
			}
			catch (NoSuchFieldException e2) {
				sortBy = "conceptId";
			}
		}
		
		String hql = "";
		if (isNameField) {
			hql += "select concept";
		}
		
		hql += " from Concept as concept";
		boolean hasWhereClause = false;
		if (isNameField) {
			hasWhereClause = true;
			//This assumes every concept has a unique(avoid duplicates) fully specified name
			//which should be true for a clean concept dictionary
			hql += " left join concept.names as names where names.conceptNameType = 'FULLY_SPECIFIED'";
		}
		
		if (!includeRetired) {
			if (hasWhereClause) {
				hql += " and";
			} else {
				hql += " where";
			}
			hql += " concept.retired = false";
			
		}
		
		if (isNameField) {
			hql += " order by names." + sortBy;
		} else {
			hql += " order by concept." + sortBy;
		}
		
		hql += asc ? " asc" : " desc";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
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
		if (!includeRetired) {
			searchCriteria.add(Restrictions.eq("drug.retired", false));
		}
		if (concept != null) {
			searchCriteria.add(Restrictions.eq("drug.concept", concept));
		}
		if (drugName != null) {
			searchCriteria.add(Restrictions.eq("drug.name", drugName));
		}
		return (List<Drug>) searchCriteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getDrugsByIngredient(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<Drug> getDrugsByIngredient(Concept ingredient) {
		Criteria searchDrugCriteria = sessionFactory.getCurrentSession().createCriteria(Drug.class, "drug");
		Criterion rhs = Restrictions.eq("drug.concept", ingredient);
		searchDrugCriteria.createAlias("ingredients", "ingredients");
		Criterion lhs = Restrictions.eq("ingredients.ingredient", ingredient);
		searchDrugCriteria.add(Restrictions.or(lhs, rhs));
		
		return (List<Drug>) searchDrugCriteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getDrugs(java.lang.String)
	 */
	@Override
	public List<Drug> getDrugs(final String phrase) throws DAOException {
		String searchPhrase = newRequirePartialWordsSearchPhrase(phrase);
		
		String query = "+name:(" + searchPhrase + ")";
		
		List<Drug> list = LuceneQuery.newQuery(query, sessionFactory.getCurrentSession(), Drug.class).list();
		
		return list;
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
		if (name != null) {
			crit.add(Restrictions.eq("name", name));
		}
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getAllConceptClasses(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptClass> getAllConceptClasses(boolean includeRetired) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptClass.class);
		
		// Minor bug - was assigning includeRetired instead of evaluating
		if (!includeRetired) {
			crit.add(Restrictions.eq("retired", false));
		}
		
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
	 * @see org.openmrs.api.db.ConceptDAO#purgeConceptNameTag(org.openmrs.ConceptNameTag)
	 */
	public void deleteConceptNameTag(ConceptNameTag cnt) throws DAOException {
		sessionFactory.getCurrentSession().delete(cnt);
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
		
		if (!includeRetired) {
			crit.add(Restrictions.eq("retired", false));
		}
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptDatatypes(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptDatatype> getConceptDatatypes(String name) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptDatatype.class);
		
		if (name != null) {
			crit.add(Restrictions.like("name", name, MatchMode.START));
		}
		
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
	public List<Concept> getConcepts(final String name, final Locale loc, final boolean searchOnPhrase,
	        final List<ConceptClass> classes, final List<ConceptDatatype> datatypes) throws DAOException {
		
		StringBuilder query = new StringBuilder();
		
		final Locale locale;
		if (loc == null) {
			locale = Context.getLocale();
		} else {
			locale = loc;
		}
		
		if (!StringUtils.isBlank(name)) {
			if (searchOnPhrase) {
				String search = newNamesQuery(Sets.newHashSet(locale), name, true);
				query.append(search);
			} else {
				String search = newNamesQuery(Sets.newHashSet(locale), name, false);
				query.append(search);
			}
		}
		
		query.append(" +concept.retired:false");
		
		appendIdsQuery(query, "+concept.conceptClass.conceptClassId", classes);
		
		appendIdsQuery(query, "+concept.datatype.conceptDatatypeId", datatypes);
		
		final List<ConceptName> names = LuceneQuery.newQuery(query.toString(), sessionFactory.getCurrentSession(),
		    ConceptName.class).skipSame("concept.conceptId", "conceptNameId").list();
		
		final List<Concept> concepts = Lists.transform(names, transformNameToConcept);
		
		return concepts;
	}
	
	private String newNamesQuery(final Set<Locale> locales, final String name, final boolean keywords) {
		final String phrase;
		if (keywords) {
			phrase = "(" + newRequirePartialWordsSearchPhrase(name) + " \"" + name + "\"^1000)";
		} else {
			phrase = "\"" + LuceneQuery.escapeQuery(name) + "\"";
		}
		
		StringBuilder query = new StringBuilder();
		
		query.append(" +name:").append(phrase);
		query.append(" +locale:(");
		for (Locale locale : locales) {
			query.append(locale.getLanguage()).append("* ");
			if (!StringUtils.isBlank(locale.getCountry())) {
				query.append(locale).append("^2");
			}
		}
		query.append(")");
		query.append(" +voided:false");
		
		return query.toString();
	}
	
	/**
	 * gets questions for the given answer concept
	 *
	 * @see org.openmrs.api.db.ConceptDAO#getConceptsByAnswer(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> getConceptsByAnswer(Concept concept) {
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
		    Restrictions.lt("conceptId", i)).addOrder(Order.desc("conceptId")).setFetchSize(1).list();
		
		if (concepts.size() < 1) {
			return null;
		}
		return concepts.get(0);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getNextConcept(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public Concept getNextConcept(Concept c) {
		Integer i = c.getConceptId();
		
		List<Concept> concepts = sessionFactory.getCurrentSession().createCriteria(Concept.class).add(
		    Restrictions.gt("conceptId", i)).addOrder(Order.asc("conceptId")).setMaxResults(1).list();
		
		if (concepts.size() < 1) {
			return null;
		}
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
		
		if (!includeCompleted) {
			crit.add(Restrictions.eq("state", OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED));
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
		crit.add(Restrictions.eq("state", OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED));
		crit.add(Restrictions.eq("originalText", text));
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getProposedConcepts(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> getProposedConcepts(String text) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptProposal.class);
		crit.add(Restrictions.ne("state", OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED));
		crit.add(Restrictions.eq("originalText", text));
		crit.add(Restrictions.isNotNull("mappedConcept"));
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
		    Restrictions.eq("tag", name));
		
		if (crit.list().size() < 1) {
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
	public List<ConceptSource> getAllConceptSources(boolean includeRetired) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptSource.class);
		
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		}
		
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
		if (nameTag == null) {
			return null;
		}
		
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
		
		//join to the conceptReferenceTerm table
		criteria.createAlias("conceptReferenceTerm", "term");
		
		// match the source code to the passed code
		criteria.add(Restrictions.eq("term.code", code));
		
		// join to concept reference source and match to the h17Code or source name
		criteria.createAlias("term.conceptSource", "source");
		criteria.add(Restrictions.or(Restrictions.eq("source.name", sourceName), Restrictions.eq("source.hl7Code",
		    sourceName)));
		
		criteria.createAlias("concept", "concept");
		
		if (!includeRetired) {
			// ignore retired concepts
			criteria.add(Restrictions.eq("concept.retired", false));
		} else {
			// sort retired concepts to the end of the list
			criteria.addOrder(Order.asc("concept.retired"));
		}
		
		// we only want distinct concepts
		criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
		
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
	
	public ConceptSource getConceptSourceByUuid(String uuid) {
		return (ConceptSource) sessionFactory.getCurrentSession().createQuery("from ConceptSource cc where cc.uuid = :uuid")
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
		for (Object[] o : list) {
			ret.put((Integer) o[0], (String) o[1]);
		}
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
	 * @see org.openmrs.api.db.ConceptDAO#getConceptMapsBySource(ConceptSource)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptMap> getConceptMapsBySource(ConceptSource conceptSource) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptMap.class);
		criteria.createAlias("conceptReferenceTerm", "term");
		criteria.add(Restrictions.eq("term.conceptSource", conceptSource));
		return (List<ConceptMap>) criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptSourceByName(java.lang.String)
	 */
	public ConceptSource getConceptSourceByName(String conceptSourceName) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptSource.class, "source");
		criteria.add(Restrictions.eq("source.name", conceptSourceName));
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
	 * @see org.openmrs.api.db.ConceptDAO#getConceptStopWords(java.util.Locale)
	 */
	public List<String> getConceptStopWords(Locale locale) throws DAOException {
		
		locale = (locale == null ? Context.getLocale() : locale);
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptStopWord.class);
		criteria.setProjection(Projections.property("value"));
		criteria.add(Restrictions.eq("locale", locale));
		
		return (List<String>) criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#saveConceptStopWord(org.openmrs.ConceptStopWord)
	 */
	public ConceptStopWord saveConceptStopWord(ConceptStopWord conceptStopWord) throws DAOException {
		if (conceptStopWord != null) {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptStopWord.class);
			criteria.add(Restrictions.eq("value", conceptStopWord.getValue()));
			criteria.add(Restrictions.eq("locale", conceptStopWord.getLocale()));
			List<ConceptStopWord> stopWordList = criteria.list();
			
			if (!stopWordList.isEmpty()) {
				throw new DAOException("Duplicate ConceptStopWord Entry");
			}
			sessionFactory.getCurrentSession().saveOrUpdate(conceptStopWord);
		}
		
		return conceptStopWord;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#deleteConceptStopWord(java.lang.Integer)
	 */
	public void deleteConceptStopWord(Integer conceptStopWordId) throws DAOException {
		if (conceptStopWordId == null) {
			throw new DAOException("conceptStopWordId is null");
		}
		Object csw = sessionFactory.getCurrentSession().createCriteria(ConceptStopWord.class).add(
		    Restrictions.eq("conceptStopWordId", conceptStopWordId)).uniqueResult();
		if (csw == null) {
			throw new DAOException("Concept Stop Word not found or already deleted");
		}
		sessionFactory.getCurrentSession().delete(csw);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getAllConceptStopWords()
	 */
	public List<ConceptStopWord> getAllConceptStopWords() {
		return sessionFactory.getCurrentSession().createCriteria(ConceptStopWord.class).list();
	}
	
	/**
	 * @see ConceptService#getCountOfDrugs(String, Concept, boolean, boolean)
	 */
	public Long getCountOfDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired) throws DAOException {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(Drug.class, "drug");
		if (StringUtils.isBlank(drugName) && concept == null) {
			return 0L;
		}
		
		if (!includeRetired) {
			searchCriteria.add(Restrictions.eq("drug.retired", false));
		}
		if (concept != null) {
			searchCriteria.add(Restrictions.eq("drug.concept", concept));
		}
		MatchMode matchMode = MatchMode.START;
		if (searchOnPhrase) {
			matchMode = MatchMode.ANYWHERE;
		}
		if (!StringUtils.isBlank(drugName)) {
			searchCriteria.add(Restrictions.ilike("drug.name", drugName, matchMode));
			if (searchDrugConceptNames) {
				searchCriteria.createCriteria("concept", "concept").createAlias("concept.names", "names");
				searchCriteria.add(Restrictions.ilike("names.name", drugName, matchMode));
			}
		}
		
		searchCriteria.setProjection(Projections.countDistinct("drug.drugId"));
		
		return (Long) searchCriteria.uniqueResult();
	}
	
	/**
	 * @should return a drug if either the drug name or concept name matches the phase not both
	 * @should return distinct drugs
	 * @should return a drug, if phrase match concept_name No need to match both concept_name and drug_name
	 * @should return drug when phrase match drug_name even searchDrugConceptNames is false
	 * @should return a drug if phrase match drug_name No need to match both concept_name and drug_name
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Drug> getDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired, Integer start, Integer length) throws DAOException {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(Drug.class, "drug");
		if (StringUtils.isBlank(drugName) && concept == null) {
			return Collections.emptyList();
		}
		
		if (!includeRetired) {
			searchCriteria.add(Restrictions.eq("drug.retired", false));
		}
		MatchMode matchMode = MatchMode.START;
		if (searchOnPhrase) {
			matchMode = MatchMode.ANYWHERE;
		}
		if (!StringUtils.isBlank(drugName)) {
			if (searchDrugConceptNames && concept != null) {
				searchCriteria.createCriteria("concept", "concept").createAlias("concept.names", "names");
				searchCriteria.add(Restrictions.or(Restrictions.ilike("drug.name", drugName, matchMode), Restrictions.ilike(
				    "names.name", drugName, matchMode)));
				searchCriteria.setProjection(Projections.distinct(Projections.property("drugId")));
			} else {
				searchCriteria.add(Restrictions.ilike("drug.name", drugName, matchMode));
				
			}
		}
		
		if (start != null) {
			searchCriteria.setFirstResult(start);
		}
		if (length != null && length > 0) {
			searchCriteria.setMaxResults(length);
		}
		
		return searchCriteria.list();
	}
	
	/**
	 * @see ConceptDAO#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer,
	 *      Integer)
	 */
	@Override
	public List<ConceptSearchResult> getConcepts(final String phrase, final List<Locale> locales,
	        final boolean includeRetired, final List<ConceptClass> requireClasses, final List<ConceptClass> excludeClasses,
	        final List<ConceptDatatype> requireDatatypes, final List<ConceptDatatype> excludeDatatypes,
	        final Concept answersToConcept, final Integer start, final Integer size) throws DAOException {
		
		final StringBuilder query = new StringBuilder();
		
		if (!StringUtils.isBlank(phrase)) {
			final Set<Locale> searchLocales;
			
			if (locales == null) {
				searchLocales = Sets.newHashSet(Context.getLocale());
			} else {
				searchLocales = Sets.newHashSet(locales);
			}
			
			query.append(newNamesQuery(searchLocales, phrase, true));
		}
		
		if (!includeRetired) {
			query.append(" +concept.retired:false");
		}
		
		appendIdsQuery(query, "+concept.conceptClass.conceptClassId", requireClasses);
		
		appendIdsQuery(query, "-concept.conceptClass.conceptClassId", excludeClasses);
		
		appendIdsQuery(query, "+concept.datatype.conceptDatatypeId", requireDatatypes);
		
		appendIdsQuery(query, "-concept.datatype.conceptDatatypeId", excludeDatatypes);
		
		if (answersToConcept != null) {
			Collection<ConceptAnswer> answers = answersToConcept.getAnswers(false);
			
			if (answers != null && !answers.isEmpty()) {
				StringBuilder ids = new StringBuilder();
				for (ConceptAnswer conceptAnswer : answersToConcept.getAnswers(false)) {
					ids.append(conceptAnswer.getAnswerConcept().getId()).append(" ");
				}
				query.append(" +concept.conceptId:(").append(ids).append(")");
			}
		}
		
		ListPart<ConceptName> names = LuceneQuery.newQuery(query.toString(), sessionFactory.getCurrentSession(),
		    ConceptName.class).skipSame("concept.conceptId", "conceptNameId").listPart(start, size);
		
		List<ConceptSearchResult> results = Lists.transform(names.getList(),
		    new Function<ConceptName, ConceptSearchResult>() {
			    
			    @Override
			    public ConceptSearchResult apply(ConceptName conceptName) {
				    return new ConceptSearchResult(phrase, conceptName.getConcept(), conceptName);
			    }
		    });
		
		return results;
	}
	
	private void appendIdsQuery(final StringBuilder query, final String field, final List<? extends OpenmrsObject> objects) {
		String ids = transformToIds(objects);
		if (ids != null) {
			query.append(" ").append(field).append(":(").append(ids).append(")");
		}
	}
	
	private String newRequirePartialWordsSearchPhrase(final String phrase) {
		StringBuilder searchPhrase = new StringBuilder();
		String[] words = LuceneQuery.escapeQuery(phrase).trim().split(" ");
		for (String word : words) {
			word = word.trim();
			searchPhrase.append(" +(").append(word).append("~ ").append(word).append("*^2)");
		}
		return searchPhrase.toString();
	}
	
	private String transformToIds(final List<? extends OpenmrsObject> items) {
		if (items == null || items.isEmpty()) {
			return null;
		}
		
		StringBuilder ids = new StringBuilder();
		for (OpenmrsObject item : items) {
			ids.append(item.getId()).append(" ");
		}
		return ids.toString();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptMapTypes(boolean, boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ConceptMapType> getConceptMapTypes(boolean includeRetired, boolean includeHidden) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptMapType.class);
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		}
		if (!includeHidden) {
			criteria.add(Restrictions.eq("isHidden", false));
		}
		
		List<ConceptMapType> conceptMapTypes = criteria.list();
		Collections.sort(conceptMapTypes, new ConceptMapTypeComparator());
		
		return conceptMapTypes;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptMapType(java.lang.Integer)
	 */
	@Override
	public ConceptMapType getConceptMapType(Integer conceptMapTypeId) throws DAOException {
		return (ConceptMapType) sessionFactory.getCurrentSession().get(ConceptMapType.class, conceptMapTypeId);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptMapTypeByUuid(java.lang.String)
	 */
	@Override
	public ConceptMapType getConceptMapTypeByUuid(String uuid) throws DAOException {
		return (ConceptMapType) sessionFactory.getCurrentSession().createQuery(
		    "from ConceptMapType cmt where cmt.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptMapTypeByName(java.lang.String)
	 */
	@Override
	public ConceptMapType getConceptMapTypeByName(String name) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptMapType.class);
		criteria.add(Restrictions.ilike("name", name, MatchMode.EXACT));
		return (ConceptMapType) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#saveConceptMapType(org.openmrs.ConceptMapType)
	 */
	@Override
	public ConceptMapType saveConceptMapType(ConceptMapType conceptMapType) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(conceptMapType);
		return conceptMapType;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#deleteConceptMapType(org.openmrs.ConceptMapType)
	 */
	@Override
	public void deleteConceptMapType(ConceptMapType conceptMapType) throws DAOException {
		sessionFactory.getCurrentSession().delete(conceptMapType);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptReferenceTerms(boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ConceptReferenceTerm> getConceptReferenceTerms(boolean includeRetired) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTerm.class);
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		}
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptReferenceTerm(java.lang.Integer)
	 */
	@Override
	public ConceptReferenceTerm getConceptReferenceTerm(Integer conceptReferenceTermId) throws DAOException {
		return (ConceptReferenceTerm) sessionFactory.getCurrentSession().get(ConceptReferenceTerm.class,
		    conceptReferenceTermId);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptReferenceTermByUuid(java.lang.String)
	 */
	@Override
	public ConceptReferenceTerm getConceptReferenceTermByUuid(String uuid) throws DAOException {
		return (ConceptReferenceTerm) sessionFactory.getCurrentSession().createQuery(
		    "from ConceptReferenceTerm crt where crt.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptReferenceTermsBySource(ConceptSource)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ConceptReferenceTerm> getConceptReferenceTermsBySource(ConceptSource conceptSource) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTerm.class);
		criteria.add(Restrictions.eq("conceptSource", conceptSource));
		return (List<ConceptReferenceTerm>) criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptReferenceTermByName(java.lang.String,
	 *      org.openmrs.ConceptSource)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public ConceptReferenceTerm getConceptReferenceTermByName(String name, ConceptSource conceptSource) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTerm.class);
		criteria.add(Restrictions.ilike("name", name, MatchMode.EXACT));
		criteria.add(Restrictions.eq("conceptSource", conceptSource));
		List terms = criteria.list();
		if (terms.size() == 0) {
			return null;
		} else if (terms.size() > 1) {
			throw new APIException(Context.getMessageSourceService().getMessage(
			    "ConceptReferenceTerm.foundMultipleTermsWithNameInSource", new Object[] { name, conceptSource.getName() },
			    null));
		}
		return (ConceptReferenceTerm) terms.get(0);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptReferenceTermByCode(java.lang.String,
	 *      org.openmrs.ConceptSource)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public ConceptReferenceTerm getConceptReferenceTermByCode(String code, ConceptSource conceptSource) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTerm.class);
		criteria.add(Restrictions.eq("code", code));
		criteria.add(Restrictions.eq("conceptSource", conceptSource));
		List terms = criteria.list();
		if (terms.size() == 0) {
			return null;
		} else if (terms.size() > 1) {
			throw new APIException(Context.getMessageSourceService().getMessage(
			    "ConceptReferenceTerm.foundMultipleTermsWithCodeInSource", new Object[] { code, conceptSource.getName() },
			    null));
		}
		return (ConceptReferenceTerm) terms.get(0);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#saveConceptReferenceTerm(org.openmrs.ConceptReferenceTerm)
	 */
	@Override
	public ConceptReferenceTerm saveConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(conceptReferenceTerm);
		return conceptReferenceTerm;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#deleteConceptReferenceTerm(org.openmrs.ConceptReferenceTerm)
	 */
	@Override
	public void deleteConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) throws DAOException {
		sessionFactory.getCurrentSession().delete(conceptReferenceTerm);
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getCountOfConceptReferenceTerms(java.lang.String, boolean)
	 */
	@Override
	public Long getCountOfConceptReferenceTerms(String query, ConceptSource conceptSource, boolean includeRetired)
	        throws DAOException {
		Criteria criteria = createConceptReferenceTermCriteria(query, conceptSource, includeRetired);
		
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptReferenceTerms(String, ConceptSource, Integer,
	 *      Integer, boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ConceptReferenceTerm> getConceptReferenceTerms(String query, ConceptSource conceptSource, Integer start,
	        Integer length, boolean includeRetired) throws APIException {
		Criteria criteria = createConceptReferenceTermCriteria(query, conceptSource, includeRetired);
		
		if (start != null) {
			criteria.setFirstResult(start);
		}
		if (length != null && length > 0) {
			criteria.setMaxResults(length);
		}
		
		return criteria.list();
	}
	
	/**
	 * @param query
	 * @param includeRetired
	 * @return
	 */
	private Criteria createConceptReferenceTermCriteria(String query, ConceptSource conceptSource, boolean includeRetired) {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTerm.class);
		if (conceptSource != null) {
			searchCriteria.add(Restrictions.eq("conceptSource", conceptSource));
		}
		if (!includeRetired) {
			searchCriteria.add(Restrictions.eq("retired", false));
		}
		if (query != null) {
			searchCriteria.add(Restrictions.or(Restrictions.ilike("name", query, MatchMode.ANYWHERE), Restrictions.ilike(
			    "code", query, MatchMode.ANYWHERE)));
		}
		return searchCriteria;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getReferenceTermMappingsTo(ConceptReferenceTerm)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ConceptReferenceTermMap> getReferenceTermMappingsTo(ConceptReferenceTerm term) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTermMap.class);
		criteria.add(Restrictions.eq("termB", term));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#isConceptReferenceTermInUse(org.openmrs.ConceptReferenceTerm)
	 */
	@Override
	public boolean isConceptReferenceTermInUse(ConceptReferenceTerm term) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptMap.class);
		criteria.add(Restrictions.eq("conceptReferenceTerm", term));
		criteria.setProjection(Projections.rowCount());
		if ((Long) criteria.uniqueResult() > 0) {
			return true;
		}
		
		criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTermMap.class);
		criteria.add(Restrictions.eq("termB", term));
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.uniqueResult() > 0;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#isConceptMapTypeInUse(org.openmrs.ConceptMapType)
	 */
	@Override
	public boolean isConceptMapTypeInUse(ConceptMapType mapType) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptMap.class);
		criteria.add(Restrictions.eq("conceptMapType", mapType));
		criteria.setProjection(Projections.rowCount());
		if ((Long) criteria.uniqueResult() > 0) {
			return true;
		}
		
		criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTermMap.class);
		criteria.add(Restrictions.eq("conceptMapType", mapType));
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.uniqueResult() > 0;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptsByName(java.lang.String, java.util.Locale,
	 *      java.lang.Boolean)
	 */
	@Override
	public List<Concept> getConceptsByName(final String name, final Locale locale, final Boolean exactLocale) {
		StringBuilder query = new StringBuilder();
		
		if (!StringUtils.isBlank(name)) {
			final String searchPhrase = LuceneQuery.escapeQuery(name);
			
			final Locale searchLocale;
			if (locale == null) {
				searchLocale = Context.getLocale();
			} else {
				searchLocale = locale;
			}
			
			if (exactLocale == null || exactLocale) {
				query.append(" +name:").append("\"").append(searchPhrase).append("\"");
				query.append(" +locale:").append(searchLocale);
			} else {
				query.append(newNamesQuery(Sets.newHashSet(searchLocale), searchPhrase, false));
			}
		}
		
		query.append(" +concept.retired:false");
		
		final List<ConceptName> names = LuceneQuery.newQuery(query.toString(), sessionFactory.getCurrentSession(),
		    ConceptName.class).skipSame("concept.conceptId", "conceptNameId").list();
		
		final List<Concept> concepts = Lists.transform(names, transformNameToConcept);
		
		return concepts;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getDefaultConceptMapType()
	 */
	@Override
	public ConceptMapType getDefaultConceptMapType() throws DAOException {
		FlushMode previousFlushMode = sessionFactory.getCurrentSession().getFlushMode();
		sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);
		try {
			//Defaults to same-as if the gp is not set.
			String defaultConceptMapType = Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GP_DEFAULT_CONCEPT_MAP_TYPE);
			if (defaultConceptMapType == null) {
				throw new DAOException("The default concept map type is not set. You need to set the '"
				        + OpenmrsConstants.GP_DEFAULT_CONCEPT_MAP_TYPE + "' global property.");
			}
			
			ConceptMapType conceptMapType = getConceptMapTypeByName(defaultConceptMapType);
			if (conceptMapType == null) {
				throw new DAOException("The default concept map type (name: " + defaultConceptMapType
				        + ") does not exist! You need to set the '" + OpenmrsConstants.GP_DEFAULT_CONCEPT_MAP_TYPE
				        + "' global property.");
			}
			return conceptMapType;
		}
		finally {
			sessionFactory.getCurrentSession().setFlushMode(previousFlushMode);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#isConceptNameDuplicate(org.openmrs.ConceptName)
	 */
	@Override
	public boolean isConceptNameDuplicate(ConceptName name) {
		if (!name.isFullySpecifiedName() || !name.isLocalePreferred()) {
			return false;
		}
		if (name.getConcept() != null && name.getConcept().isRetired()) {
			return false;
		}
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptName.class);
		
		criteria.add(Restrictions.eq("voided", false));
		criteria.add(Restrictions.or(Restrictions.eq("locale", name.getLocale()), Restrictions.eq("locale", new Locale(name
		        .getLocale().getLanguage()))));
		if (Context.getConceptService().isConceptNameSearchCaseSensitive()) {
			criteria.add(Restrictions.ilike("name", name.getName()));
		} else {
			criteria.add(Restrictions.eq("name", name.getName()));
		}
		
		criteria.add(Restrictions.or(Restrictions.eq("conceptNameType", ConceptNameType.FULLY_SPECIFIED), Restrictions.eq(
		    "localePreferred", true)));
		
		criteria.createAlias("concept", "concept");
		criteria.add(Restrictions.eq("concept.retired", false));
		if (name.getConcept() != null && name.getConcept().getConceptId() != null) {
			criteria.add(Restrictions.ne("concept.conceptId", name.getConcept().getConceptId()));
		}
		
		criteria.setProjection(Projections.rowCount());
		long rowCount = ((Number) criteria.uniqueResult()).longValue();
		
		return rowCount != 0L;
	}
}
