package org.openmrs.api.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptSynonym;
import org.openmrs.Drug;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.context.Context;

public class HibernateConceptService implements
		ConceptService {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateConceptService(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.ConceptService#createConcept(org.openmrs.Concept)
	 */
	public void createConcept(Concept concept) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		concept.setCreator(context.getAuthenticatedUser());
		concept.setDateCreated(new Date());
		concept.setChangedBy(context.getAuthenticatedUser());
		concept.setDateChanged(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(concept);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
		
	}

	/**
	 * @see org.openmrs.api.ConceptService#deleteConcept(org.openmrs.Concept)
	 */
	public void deleteConcept(Concept concept) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.delete(concept);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
		
	}

	/**
	 * @see org.openmrs.api.ConceptService#getConcept(java.lang.Integer)
	 */
	public Concept getConcept(Integer conceptId) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		Concept concept = new Concept();
		concept = (Concept)session.get(Concept.class, conceptId);
		
		return concept;
	}

	/**
	 * @see org.openmrs.api.ConceptService#updateConcept(org.openmrs.Concept)
	 */
	public void updateConcept(Concept concept) {
		
		if (concept.getConceptId() == null)
			createConcept(concept);
		else {
			Session session = HibernateUtil.currentSession();
			
			try {
				HibernateUtil.beginTransaction();
				modifyCollections(concept);
				concept.setChangedBy(context.getAuthenticatedUser());
				concept.setDateChanged(new Date());
				session.update(concept);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e); 
			}
		}
	}
	
	protected void modifyCollections(Concept c) {
		
		for (ConceptSynonym syn : c.getSynonyms()) {
			if (syn.getCreator() == null ) {
				syn.setCreator(context.getAuthenticatedUser());
				syn.setDateCreated(new Date());
			}
			syn.setConcept(c);
		}
	}

	/**
	 * @see org.openmrs.api.ConceptService#voidConcept(org.openmrs.Concept, java.lang.String)
	 */
	public void voidConcept(Concept concept, String reason) {
		concept.setRetired(false);
		updateConcept(concept);
	}

	/**
	 * @see org.openmrs.api.ConceptService#getConceptByName(java.lang.String)
	 */
	public List<Concept> getConceptByName(String name) {
		
		Session session = HibernateUtil.currentSession();
		
		List<Concept> concepts = session.createCriteria(Concept.class)
								.createCriteria("names", "n")
								//.add(Expression.eq("locale", context.getLocale().toString()))
								.add(Expression.like("n.name", "%" + name + "%" , MatchMode.ANYWHERE))
								.list();
		return concepts;
	}

	/**
	 * @see org.openmrs.api.ConceptService#getDrugs()
	 */
	public List<Drug> getDrugs() {

		Session session = HibernateUtil.currentSession();
		
		List<Drug> drugs = session.createQuery("from Drug").list();
		
		return drugs;
	}

	/**
	 * @see org.openmrs.api.ConceptService#getConceptClass(java.lang.Integer)
	 */
	public ConceptClass getConceptClass(Integer i) {
		Session session = HibernateUtil.currentSession();
		
		ConceptClass cc = new ConceptClass();
		cc = (ConceptClass)session.get(ConceptClass.class, i);
		
		return cc;
	}

	/**
	 * @see org.openmrs.api.ConceptService#getConceptClasses()
	 */
	public List<ConceptClass> getConceptClasses() {
		Session session = HibernateUtil.currentSession();
		
		List<ConceptClass> drugs = session.createQuery("from ConceptClass cc order by cc.name").list();
		
		return drugs;
	}

	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatype(java.lang.Integer)
	 */
	public ConceptDatatype getConceptDatatype(Integer i) {
		Session session = HibernateUtil.currentSession();
		
		ConceptDatatype cd = new ConceptDatatype();
		cd = (ConceptDatatype)session.get(ConceptDatatype.class, i);
		
		return cd;
	}

	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatypes()
	 */
	public List<ConceptDatatype> getConceptDatatypes() {
		Session session = HibernateUtil.currentSession();
		
		List<ConceptDatatype> cds = session.createQuery("from ConceptDatatype cd order by cd.name").list();
		
		return cds;
	}
	
	
	
}
