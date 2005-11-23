package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSynonym;
import org.openmrs.Drug;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.APIException;
import org.openmrs.api.db.ConceptService;

public class HibernateConceptService implements
		ConceptService {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateConceptService(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#createConcept(org.openmrs.Concept)
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
	 * @see org.openmrs.api.db.ConceptService#deleteConcept(org.openmrs.Concept)
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
	 * @see org.openmrs.api.db.ConceptService#getConcept(java.lang.Integer)
	 */
	public Concept getConcept(Integer conceptId) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		Concept concept = new Concept();
		concept = (Concept)session.get(Concept.class, conceptId);
		
		return concept;
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#updateConcept(org.openmrs.Concept)
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
				session.saveOrUpdate(concept);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e); 
			}
		}
	}
	
	protected void modifyCollections(Concept c) {
		
		User authUser = context.getAuthenticatedUser();
		Date timestamp = new Date();
		if (c.getNames() != null) {
			for (ConceptName cn : c.getNames()) {
				if (cn.getCreator() == null ) {
					cn.setCreator(authUser);
					cn.setDateCreated(timestamp);
				}
			}
		}
		for (ConceptSynonym syn : c.getSynonyms()) {
			if (syn.getCreator() == null ) {
				syn.setCreator(authUser);
				syn.setDateCreated(timestamp);
			}
			syn.setConcept(c);
		}
		if (c.getConceptSets() != null) {
			for (ConceptSet set : c.getConceptSets()) {
				if (set.getCreator() == null ) {
					set.setCreator(authUser);
					set.setDateCreated(timestamp);
				}
				set.setSet(c);
			}
		}
		if (c.getAnswers() != null) {
			for (ConceptAnswer ca : c.getAnswers()) {
				if (ca.getCreator() == null ) {
					ca.setCreator(authUser);
					ca.setDateCreated(timestamp);
				}
				ca.setConcept(c);
			}
		}
		if (c.getConceptNumeric() != null) {
			ConceptNumeric cn = c.getConceptNumeric();
			if (cn.getCreator() == null) {
				cn.setCreator(authUser);
				cn.setDateCreated(timestamp);
			}
			cn.setConcept(c);
			cn.setChangedBy(authUser);
			cn.setDateChanged(timestamp);
		}

	}

	/**
	 * @see org.openmrs.api.db.ConceptService#voidConcept(org.openmrs.Concept, java.lang.String)
	 */
	public void voidConcept(Concept concept, String reason) {
		concept.setRetired(false);
		updateConcept(concept);
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getConceptByName(java.lang.String)
	 */
	public List<Concept> getConceptByName(String name) {
		
		Session session = HibernateUtil.currentSession();
		
		Query query = session.createQuery("select concept from Concept concept where concept.names.name like '%' || ? || '%'");
		query.setString(0, name);
		List<Concept> concepts = query.list();
		
		
		/*  Use if names is an entity, not a value type of Concept
		 *    aka. if names is mapped with many-to-on instead of composite-element
		Criteria criteria = session.createCriteria(Concept.class);
		criteria.createCriteria("names", "n");
		//criteria.add(Expression.eq("locale", context.getLocale().toString()));
		criteria.add(Expression.like("n.name", "%" + name + "%" , MatchMode.ANYWHERE));
		List<Concept> concepts = criteria.list();
		*/
		return concepts;
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getDrugs()
	 */
	public List<Drug> getDrugs() {

		Session session = HibernateUtil.currentSession();
		
		List<Drug> drugs = session.createQuery("from Drug").list();
		
		return drugs;
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getConceptClass(java.lang.Integer)
	 */
	public ConceptClass getConceptClass(Integer i) {
		Session session = HibernateUtil.currentSession();
		
		ConceptClass cc = new ConceptClass();
		cc = (ConceptClass)session.get(ConceptClass.class, i);
		
		return cc;
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getConceptClasses()
	 */
	public List<ConceptClass> getConceptClasses() {
		Session session = HibernateUtil.currentSession();
		
		List<ConceptClass> drugs = session.createQuery("from ConceptClass cc order by cc.name").list();
		
		return drugs;
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getConceptDatatype(java.lang.Integer)
	 */
	public ConceptDatatype getConceptDatatype(Integer i) {
		Session session = HibernateUtil.currentSession();
		
		ConceptDatatype cd = new ConceptDatatype();
		cd = (ConceptDatatype)session.get(ConceptDatatype.class, i);
		
		return cd;
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getConceptDatatypes()
	 */
	public List<ConceptDatatype> getConceptDatatypes() {
		Session session = HibernateUtil.currentSession();
		
		List<ConceptDatatype> cds = session.createQuery("from ConceptDatatype cd order by cd.name").list();
		
		return cds;
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getConceptNumeric(java.lang.Integer)
	 */
	public ConceptNumeric getConceptNumeric(Integer i) {
		Session session = HibernateUtil.currentSession();
		
		ConceptNumeric cn = new ConceptNumeric();
		cn = (ConceptNumeric)session.get(ConceptNumeric.class, i);
		
		return cn;
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getConceptSets(java.lang.Integer)
	 */
	public List<ConceptSet> getConceptSets(Concept concept) {
		Session session = HibernateUtil.currentSession();
		
		List<ConceptSet> sets = session.createCriteria(ConceptSet.class)
						.add(Restrictions.eq("set", concept))
						.addOrder(Order.asc("sortWeight"))
						.list();
		return sets;
	}
	
	
	
}
