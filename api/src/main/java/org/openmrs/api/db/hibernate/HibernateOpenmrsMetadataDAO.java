package org.openmrs.api.db.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.api.db.OpenmrsMetadataDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class implementing basic data access methods for BaseOpenmrsMetadata persistents
 *
 * @since 1.10
 *
 * @param <T>
 */
public abstract class HibernateOpenmrsMetadataDAO<T extends BaseOpenmrsMetadata> implements OpenmrsMetadataDAO<T> {
	
	@Autowired
	protected SessionFactory sessionFactory;

	private Class<T> mappedClass;
	
	public HibernateOpenmrsMetadataDAO(Class<T> mappedClass) {
		super();
		this.mappedClass = mappedClass;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsMetadataDAO#getById(java.lang.Integer)
	 */
	public T getById(Integer id) {
		return (T) sessionFactory.getCurrentSession().get(mappedClass, id);
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsMetadataDAO#getByUuid(java.lang.String)
	 */
	public T getByUuid(String uuid) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(mappedClass);
		return (T) crit.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsMetadataDAO#getAll(boolean)
	 */
	public List<T> getAll(boolean includeRetired) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(mappedClass);
		
		if (!includeRetired) {
			crit.add(Restrictions.eq("retired", false));
		}
		
		return crit.list();
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsMetadataDAO#delete(org.openmrs.BaseOpenmrsMetadata)
	 */
	public void delete(T metadata) {
		sessionFactory.getCurrentSession().delete(metadata);
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsMetadataDAO#saveOrUpdate(org.openmrs.BaseOpenmrsMetadata)
	 */
	public T saveOrUpdate(T metadata) {
		sessionFactory.getCurrentSession().saveOrUpdate(metadata);
		return metadata;
	}

}
