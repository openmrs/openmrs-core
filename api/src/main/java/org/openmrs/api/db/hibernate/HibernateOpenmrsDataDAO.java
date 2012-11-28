package org.openmrs.api.db.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.api.db.OpenmrsDataDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class implementing basic data access methods for BaseOpenmrsData persistents
 *
 * @since 1.10
 *
 * @param <T>
 */
public class HibernateOpenmrsDataDAO<T extends BaseOpenmrsData> implements OpenmrsDataDAO<T> {
	
	@Autowired
	protected SessionFactory sessionFactory;

	private Class<T> mappedClass;
	
	public HibernateOpenmrsDataDAO(Class<T> mappedClass) {
		super();
		this.mappedClass = mappedClass;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsDataDAO#getByUuid(java.lang.String)
	 */
	public T getByUuid(String uuid) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(mappedClass);
		return (T) crit.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsDataDAO#getAll(boolean)
	 */
	public List<T> getAll(boolean includeVoided) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(mappedClass);
		
		if (!includeVoided) {
			crit.add(Restrictions.eq("voided", false));
		}
		
		return crit.list();
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsDataDAO#delete(org.openmrs.BaseOpenmrsData)
	 */
	public void delete(T persistent) {
		sessionFactory.getCurrentSession().delete(persistent);
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsDataDAO#saveOrUpdate(org.openmrs.BaseOpenmrsData)
	 */
	public T saveOrUpdate(T persistent) {
		sessionFactory.getCurrentSession().saveOrUpdate(persistent);
		return persistent;
	}

}
