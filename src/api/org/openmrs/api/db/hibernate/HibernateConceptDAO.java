package org.openmrs.api.db.hibernate;

import java.math.BigInteger;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
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
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSynonym;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.util.OpenmrsConstants;

public class HibernateConceptDAO implements
		ConceptDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateConceptDAO() { }
	
	public HibernateConceptDAO(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#createConcept(org.openmrs.Concept)
	 */
	public void createConcept(Concept concept) throws DAOException {
		
		Session session = HibernateUtil.currentSession();

		modifyCollections(concept);

		try {
			HibernateUtil.beginTransaction();
			session.save(concept);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
		context.getAdministrationService().updateConceptWord(concept);
		
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptService#createConcept(org.openmrs.ConceptNumeric)
	 */
	public void createConcept(ConceptNumeric concept) throws DAOException {
		
		Session session = HibernateUtil.currentSession();

		modifyCollections(concept);
		
		try {
			HibernateUtil.beginTransaction();
			session.save(concept);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
		context.getAdministrationService().updateConceptWord(concept);
		
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#deleteConcept(org.openmrs.Concept)
	 */
	public void deleteConcept(Concept concept) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			
			session.createQuery("delete from ConceptWord where concept_id = :c")
					.setInteger("c", concept.getConceptId())
					.executeUpdate();
			
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
	 * @see org.openmrs.api.db.ConceptDAO#getConcepts(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> getConcepts(String sort, String dir) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		String sql = "from Concept concept";
		
		try {
			Concept.class.getDeclaredField(sort);
		}
		catch (NoSuchFieldException e) {
			try {
				ConceptName.class.getDeclaredField(sort);
				sort = "names." + sort;
			}
			catch (NoSuchFieldException e2) {
				sort = "conceptId";
			}
		}
		
		sql += " order by concept." + sort;
		if (dir.equals("desc"))
			sql += " desc";
		else
			sql += " asc";
		
		Query query = session.createQuery(sql);
		 
		List<Concept> concepts = query.list();
		
		return concepts;
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
				session.merge(concept);
				//session.saveOrUpdate(concept);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e); 
			}
			context.getAdministrationService().updateConceptWord(concept);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptService#updateConcept(org.openmrs.ConceptNumeric)
	 */
	public void updateConcept(ConceptNumeric concept) {
		
		if (concept.getConceptId() == null)
			createConcept(concept);
		else {
			Session session = HibernateUtil.currentSession();
			
			try {
				HibernateUtil.beginTransaction();
				modifyCollections(concept);
				session.update(concept);
				HibernateUtil.commitTransaction();
			}
			catch (StaleObjectStateException sose) {
				HibernateUtil.beginTransaction();
				Query query = session.createQuery("insert into ConceptNumeric (conceptId) select c.conceptId from Concept c where c.conceptId = :id)");
					query.setInteger("id", concept.getConceptId());
					query.executeUpdate();
				session.update(concept);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e); 
			}
			context.getAdministrationService().updateConceptWord(concept);
		}
	}
	

	/**
	 * @see org.openmrs.api.db.DrugService#createDrug(org.openmrs.Drug)
	 */
	public void createDrug(Drug drug) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		if (drug.getCreator() == null)
			drug.setCreator(context.getAuthenticatedUser());
		if (drug.getDateCreated() == null)
			drug.setDateCreated(new Date());
		
		try {
			HibernateUtil.beginTransaction();
			session.save(drug);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
		
	}
	
	/**
	 * @see org.openmrs.api.db.DrugService#updateDrug(org.openmrs.Drug)
	 */
	public void updateDrug(Drug drug) throws DAOException {
		
		Session session = HibernateUtil.currentSession();

		if (drug.getDrugId() == null)
			createDrug(drug);
		else {
		
			if (drug.getCreator() == null)
				drug.setCreator(context.getAuthenticatedUser());
			if (drug.getDateCreated() == null)
				drug.setDateCreated(new Date());
			
			try {
				HibernateUtil.beginTransaction();
				session.update(drug);
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
		
		if (c.getCreator() == null) {
			c.setCreator(authUser);
			c.setDateCreated(timestamp);
		}
		
		c.setChangedBy(authUser);
		c.setDateChanged(timestamp);
		
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
				set.setConceptSet(c);
			}
		}
		if (c.getAnswers(true) != null) {
			for (ConceptAnswer ca : c.getAnswers(true)) {
				if (ca.getCreator() == null ) {
					ca.setCreator(authUser);
					ca.setDateCreated(timestamp);
				}
				ca.setConcept(c);
			}
		}
		/*
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
		*/

	}

	/**
	 * @see org.openmrs.api.db.ConceptService#voidConcept(org.openmrs.Concept, java.lang.String)
	 */
	public void voidConcept(Concept concept, String reason) {
		concept.setRetired(false);
		updateConcept(concept);
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getConceptsByName(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> getConceptsByName(String name) {
		
		Session session = HibernateUtil.currentSession();
		
		Query query = session.createQuery("select concept from Concept concept where concept.names.name like '%' || ? || '%'");
		query.setString(0, name);
		List<Concept> concepts = query.list();
		
		return concepts;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptService#getConceptByName(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Concept getConceptByName(String name) {
		
		Session session = HibernateUtil.currentSession();
		
		Query query = session.createQuery("select concept from Concept concept where concept.names.name = ?");
		query.setString(0, name);
		List<Concept> concepts = query.list();
		
		int size = concepts.size(); 
		if (size > 0){
			if (size > 1)
				log.warn("Multiple concepts found for '" + name + "'");
			return concepts.get(0);
		}
		
		return null;
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getDrug(java.lang.Integer)
	 */
	public Drug getDrug(Integer drugId) {

		Session session = HibernateUtil.currentSession();
		
		Drug drug = (Drug)session.get(Drug.class, drugId);
		
		return drug;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptService#getDrugs()
	 */
	@SuppressWarnings("unchecked")
	public List<Drug> getDrugs() {

		Session session = HibernateUtil.currentSession();
		
		List<Drug> drugs = session.createQuery("from Drug").list();
		
		return drugs;
	}
	
	
	/**
	 * @see org.openmrs.api.db.ConceptService#findDrugs(java.lang.String,boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Drug> findDrugs(String phrase, boolean includeRetired) {
		Session session = HibernateUtil.currentSession();
		
		List<String> words = ConceptWord.getUniqueWords(phrase); //assumes getUniqueWords() removes quote(') characters.  (otherwise we would have a security leak)
		
		List<Drug> conceptDrugs = new Vector<Drug>();
		
		if (words.size() > 0) {
		
			Criteria searchCriteria = session.createCriteria(Drug.class, "drug");
			if (includeRetired == false) {
				searchCriteria.createAlias("concept", "concept");
				searchCriteria.add(Expression.eq("concept.retired", false));
			}
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
	 * @see org.openmrs.api.db.ConceptService#getDrugs(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<Drug> getDrugs(Concept concept) {

		Session session = HibernateUtil.currentSession();
		
		Criteria crit = session.createCriteria(Drug.class)
			.add(Expression.eq("concept", concept));
		
		return crit.list();
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
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
	public List<ConceptSet> getConceptSets(Concept concept) {
		Session session = HibernateUtil.currentSession();
		
		List<ConceptSet> sets = session.createCriteria(ConceptSet.class)
						.add(Restrictions.eq("set", concept))
						.addOrder(Order.asc("sortWeight"))
						.list();
		return sets;
	}

	// TODO below are functions worthy of a second tier
	
	/**
	 * @see org.openmrs.api.db.ConceptService#findConcepts(java.lang.String,java.util.Locale,boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptWord> findConcepts(String phrase, Locale loc, boolean includeRetired) {
		Session session = HibernateUtil.currentSession();
		
		String locale = loc.getLanguage().substring(0, 2);		//only get language portion of locale
		List<String> words = ConceptWord.getUniqueWords(phrase); //assumes getUniqueWords() removes quote(') characters.  (otherwise we would have a security leak)
		
		List<ConceptWord> conceptWords = new Vector<ConceptWord>();
		
		if (words.size() > 0) {
		
			Criteria searchCriteria = session.createCriteria(ConceptWord.class, "cw1");
			searchCriteria.add(Restrictions.eq("locale", locale));
			if (includeRetired == false) {
				searchCriteria.createAlias("concept", "concept");
				searchCriteria.add(Expression.eq("concept.retired", false));
			}
			Iterator<String> word = words.iterator();
			searchCriteria.add(Expression.like("word", word.next(), MatchMode.START));
			Conjunction junction = Expression.conjunction();
			while (word.hasNext()) {
				String w = word.next();
				log.debug(w);
				DetachedCriteria crit = DetachedCriteria.forClass(ConceptWord.class)
							.setProjection(Property.forName("concept"))
							.add(Expression.eqProperty("concept", "cw1.concept"))
							.add(Restrictions.like("word", w, MatchMode.START))
							.add(Restrictions.eq("locale", locale));
				junction.add(Subqueries.exists(crit));
			}
			searchCriteria.add(junction);
			searchCriteria.addOrder(Order.asc("synonym"));
			conceptWords = searchCriteria.list();
		}
		
		log.debug("ConceptWords found: " + conceptWords.size());
		
		//TODO this is a bit too much pre/post processing to be in the persistence layer.
		// consider moving to different layer (eg: logic).
		
		return conceptWords;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptService#findConcepts(java.lang.String,java.util.Locale,org.openmrs.Concept,boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptWord> findConceptAnswers(String phrase, Locale loc, Concept concept, boolean includeRetired) {
		Session session = HibernateUtil.currentSession();
		
		String locale = loc.getLanguage().substring(0, 2);		//only get language portion of locale
		List<String> words = ConceptWord.getUniqueWords(phrase); //assumes getUniqueWords() removes quote(') characters.  (otherwise we would have a security leak)
		
		// default return list
		List<ConceptWord> conceptWords = new Vector<ConceptWord>();
		
		// these are the answers to restrict on
		List<Concept> answers = new Vector<Concept>();
		
		if (concept.getAnswers() != null)
			for (ConceptAnswer conceptAnswer : concept.getAnswers()) {
				answers.add(conceptAnswer.getAnswerConcept());
			}
		
		// by default, we will return all of the concept's answers
		// however, if there are no answers, return nothing by default
		if (words.size() > 0 || !answers.isEmpty()) {
			Criteria searchCriteria = session.createCriteria(ConceptWord.class, "cw1");
			searchCriteria.add(Restrictions.eq("locale", locale));
			if (includeRetired == false) {
				searchCriteria.createAlias("concept", "concept");
				searchCriteria.add(Expression.eq("concept.retired", false));
			}
			
			// Only modification from standard word search
			// Only restrict on answers if there are any
			if (!answers.isEmpty())
				searchCriteria.add(Expression.in("cw1.concept", answers));
		
			// if the user typed in a phrase, restrict further
			if (words.size() > 0) {
				Iterator<String> word = words.iterator();
				searchCriteria.add(Expression.like("word", word.next(), MatchMode.START));
				Conjunction junction = Expression.conjunction();
				while (word.hasNext()) {	// add 'and' expression for _each word_ in search phrase
					String w = word.next();
					log.debug(w);
					DetachedCriteria crit = DetachedCriteria.forClass(ConceptWord.class)
								.setProjection(Property.forName("concept"))
								.add(Expression.eqProperty("concept", "cw1.concept"))
								.add(Restrictions.like("word", w, MatchMode.START))
								.add(Restrictions.eq("locale", locale));
					junction.add(Subqueries.exists(crit));
				}
				searchCriteria.add(junction);
			}
		
			searchCriteria.addOrder(Order.asc("synonym"));
			conceptWords = searchCriteria.list();
		}
		
		return conceptWords;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptService#getQuestionsForAnswer(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> getQuestionsForAnswer(Concept concept) {
		
		Session session = HibernateUtil.currentSession();
		// TODO broken until Hibernate fixes component and HQL code
		String q = "select c from Concept c where c.answers.answerConcept.conceptId = :answerId";
		Query query = session.createQuery(q);
		query.setParameter("answerId", concept.getConceptId());
		
		return query.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptService#getNextConcept(org.openmrs.Concept, java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public Concept getPrevConcept(Concept c) {
		Session session = HibernateUtil.currentSession();
		
		Integer i = c.getConceptId();
		
		List<Concept> concepts = session.createCriteria(Concept.class)
				.add(Expression.lt("conceptId", i))
				.addOrder(Order.desc("conceptId"))
				.setFetchSize(1)
				.list();
		
		if (concepts.size() < 1)
			return null;
		return concepts.get(0);
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getNextConcept(org.openmrs.Concept, java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public Concept getNextConcept(Concept c) {
		Session session = HibernateUtil.currentSession();
		
		Integer i = c.getConceptId();
		
		List<Concept> concepts = session.createCriteria(Concept.class)
				.add(Expression.gt("conceptId", i))
				.addOrder(Order.asc("conceptId"))
				.setFetchSize(1)
				.list();

		if (concepts.size() < 1)
			return null;
		return concepts.get(0);
	}

	/**
	 * @see org.openmrs.api.db.ConceptService#getConceptProposals(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptProposal> getConceptProposals(boolean includeCompleted) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		Criteria crit = session.createCriteria(ConceptProposal.class);
		
		if (includeCompleted == false) {
			crit.add(Expression.eq("state", OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED));
		}
		
		crit.addOrder(Order.asc("originalText"));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptService#getConceptProposal(java.lang.Integer)
	 */
	public ConceptProposal getConceptProposal(Integer conceptProposalId) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		ConceptProposal c = new ConceptProposal();
		c = (ConceptProposal)session.get(ConceptProposal.class, conceptProposalId);
		
		return c;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptService#findMatchingConceptProposals(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptProposal> findMatchingConceptProposals(String text) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		Criteria crit = session.createCriteria(ConceptProposal.class);
		crit.add(Expression.eq("state", OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED));
		crit.add(Expression.eq("originalText", text));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptService#findProposedConcepts(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Concept> findProposedConcepts(String text) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		Criteria crit = session.createCriteria(ConceptProposal.class);
		crit.add(Expression.ne("state", OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED));
		crit.add(Expression.eq("originalText", text));
		crit.add(Expression.isNotNull("mappedConcept"));
		crit.setProjection(Projections.distinct(Projections.property("mappedConcept")));
		
		return crit.list();
	}
	
	public void proposeConcept(ConceptProposal conceptProposal) throws APIException {
		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.save(conceptProposal);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}
	
	public Integer getNextAvailableId() {
		Session session = HibernateUtil.currentSession();
		
		String sql = "select min(concept_id+1) as concept_id from concept where (concept_id+1) not in (select concept_id from concept)";
		
		Query query = session.createSQLQuery(sql);
		
		BigInteger big = (BigInteger)query.uniqueResult();
		
		return new Integer(big.intValue());
	}
	
}
