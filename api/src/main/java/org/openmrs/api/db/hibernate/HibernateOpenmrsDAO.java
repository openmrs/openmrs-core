package org.openmrs.api.db.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.api.db.OpenmrsDAO;

@SuppressWarnings("unchecked")
public abstract class HibernateOpenmrsDAO implements OpenmrsDAO {
		
	/**
	 * Hibernate session factory
	 */
	protected SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsDAO#getMetadata(java.lang.Class, java.lang.Integer)
	 */
	@Override
	public <T extends BaseOpenmrsMetadata> T getMetadata(Class<T> clazz, Integer id) {
		return (T)sessionFactory.getCurrentSession().get(clazz, id);
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsDAO#getMetadataByUuid(java.lang.Class, java.lang.String)
	 */
	@Override
	public <T extends BaseOpenmrsMetadata> T getMetadataByUuid(Class<T> clazz, String uuid) {
		return (T) sessionFactory.getCurrentSession().createCriteria(clazz).add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsDAO#getAllMetadata(java.lang.Class, boolean)
	 */
	@Override
	public <T extends BaseOpenmrsMetadata> List<T> getAllMetadata(Class<T> clazz, boolean includeRetired) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(clazz);
		
		if (!includeRetired) {
			crit.add(Restrictions.eq("retired", false));
		}
		
		return (List<T>)crit.list();
	}

	
	/**
	 * @see org.openmrs.api.db.OpenmrsDAO#deleteMetadata(org.openmrs.BaseOpenmrsMetadata)
	 */
	@Override
	public <T extends BaseOpenmrsMetadata> void deleteMetadata(T metadata) {
		sessionFactory.getCurrentSession().delete(metadata);
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsDAO#saveMetadata(org.openmrs.BaseOpenmrsMetadata)
	 */
	@Override
	public <T extends BaseOpenmrsMetadata> T saveMetadata(T metadata) {
		sessionFactory.getCurrentSession().saveOrUpdate(metadata);
		return metadata;
	}

}
