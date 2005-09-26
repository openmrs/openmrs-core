package org.openmrs.api.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.openmrs.Concept;
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
		Transaction tx = session.beginTransaction();
		
		concept.setCreator(context.getAuthenticatedUser());
		concept.setDateCreated(new Date());
		session.save(concept);
		
		tx.commit();
		HibernateUtil.disconnectSession();
	}

	/**
	 * @see org.openmrs.api.ConceptService#deleteConcept(org.openmrs.Concept)
	 */
	public void deleteConcept(Concept concept) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(concept);
		
		tx.commit();
		HibernateUtil.disconnectSession();
		
	}

	/**
	 * @see org.openmrs.api.ConceptService#getConcept(java.lang.Integer)
	 */
	public Concept getConcept(Integer conceptId) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		Concept concept = new Concept();
		concept = (Concept)session.get(Concept.class, conceptId);
		
		HibernateUtil.disconnectSession();
		
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
			
			session.saveOrUpdate(concept);
			HibernateUtil.disconnectSession();
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
								.createCriteria("names")
								//.add(Expression.eq("locale", context.getLocale().toString()))
								.add(Expression.like("name", name))
								.list();
		
		HibernateUtil.disconnectSession();
		
		return concepts;
	}

	/**
	 * @see org.openmrs.api.ConceptService#getDrugs()
	 */
	public List<Drug> getDrugs() {

		Session session = HibernateUtil.currentSession();
		
		List<Drug> drugs = session.createQuery("from Drug").list();
		
		HibernateUtil.disconnectSession();
		
		return drugs;
	}
	
	
	
}
