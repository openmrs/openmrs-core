package org.openmrs.api.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernateUserService extends HibernateDaoSupport
	//implements UserService
{

	protected final Log log = LogFactory.getLog(getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.api.UserService#createUser(org.openmrs.User)
	 */
	public User createUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public User getUserByUsername(String username) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.api.UserService#getUser(java.lang.Long)
	 */
	public User getUser(Integer userId) {
		User user = (User) getHibernateTemplate().get(User.class, userId);
		if (user == null) {
			log.warn("request for user '" + userId + "' not found");
			throw new ObjectRetrievalFailureException(User.class, userId);
		}
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.api.UserService#saveOrUpdate(org.openmrs.User)
	 */
	public void saveUser(User user) {
		if (log.isDebugEnabled()) {
			log.debug("user id: " + user.getUserId());
		}
		
		log.debug("### pre-save middle name = " + user.getMiddleName());
		getHibernateTemplate().saveOrUpdate(user);
		log.debug("### post-save middle name = " + user.getMiddleName());

		// flush to ensure any problems are handled immediately
		getHibernateTemplate().flush();
		log.debug("### post-flush middle name = " + user.getMiddleName());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.api.UserService#voidUser(org.openmrs.User,
	 *      java.lang.String)
	 */
	public void voidUser(User user, String reason) {
		user.setVoided(true);
		// user.setVoidReason(reason);
		saveUser(user);
	}
	
	public void deleteUser(User user) {
		
	}

	public List findPatient(String q) {

		return getHibernateTemplate().find(
				"from Patient as p, PatientName as pn "
						+ "where p.patientId = pn.patientId "
						+ "and pn.familyName like ?", new Object[] { q });

	}

}
