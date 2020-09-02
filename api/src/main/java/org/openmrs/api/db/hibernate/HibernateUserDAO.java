/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.LoginCredential;
import org.openmrs.api.db.UserDAO;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.Security;
import org.openmrs.util.UserByNameComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate specific database methods for the UserService
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.db.UserDAO
 * @see org.openmrs.api.UserService
 */
public class HibernateUserDAO implements UserDAO {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateUserDAO.class);
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.UserService#saveUser(org.openmrs.User, java.lang.String)
	 */
	@Override
	public User saveUser(User user, String password) {
		
		// only change the user's password when creating a new user
		boolean isNewUser = user.getUserId() == null;
		
		sessionFactory.getCurrentSession().saveOrUpdate(user);
		
		if (isNewUser && password != null) {
			/* In OpenMRS, we are using generation strategy as native which will convert to IDENTITY 
			 for MySQL and SEQUENCE for PostgreSQL. When using IDENTITY strategy, hibernate directly 
			 issues insert statements where as with  SEQUENCE strategy hibernate only increments 
			 sequences and issues insert on session flush ( batching is possible) . 
			 PostgreSQL behaves differently than MySQL because it makes use of SEQUENCE strategy. 
			*/
			sessionFactory.getCurrentSession().flush();
			
			//update the new user with the password
			String salt = Security.getRandomToken();
			String hashedPassword = Security.encodeString(password + salt);
			
			updateUserPassword(hashedPassword, salt, Context.getAuthenticatedUser().getUserId(), new Date(), user
			        .getUserId());
		}
		
		return user;
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUserByUsername(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public User getUserByUsername(String username) {
		Query query = sessionFactory.getCurrentSession().createQuery(
		    "from User u where u.retired = '0' and (u.username = ?0 or u.systemId = ?1)");
		query.setString(0, username);
		query.setString(1, username);
		List<User> users = query.list();
		
		if (users == null || users.isEmpty()) {
			log.warn("request for username '" + username + "' not found");
			return null;
		}
		
		return users.get(0);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUserByEmail(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public User getUserByEmail(String email) {
		return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Restrictions.eq("email", email).ignoreCase()).uniqueResult();	
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getLoginCredentialByActivationKey(java.lang.String)
	 */
	@Override
	public LoginCredential getLoginCredentialByActivationKey(String activationKey) {
		String key = Security.encodeString(activationKey);
		LoginCredential loginCred = (LoginCredential) sessionFactory.getCurrentSession().createCriteria(LoginCredential.class)
									.add(Restrictions.like("activationKey", key, MatchMode.START)).uniqueResult();	
		if(loginCred != null) {
			String[] credTokens = loginCred.getActivationKey().split(":");
			if(credTokens[0].equals(key)){
				return loginCred;
			}
		}	
		return null;
 	}
	
	/**
	 * @see org.openmrs.api.UserService#hasDuplicateUsername(org.openmrs.User)
	 */
	@Override
	public boolean hasDuplicateUsername(String username, String systemId, Integer userId) {
		if (username == null || username.length() == 0) {
			username = "-";
		}
		if (systemId == null || systemId.length() == 0) {
			systemId = "-";
		}
		
		if (userId == null) {
			userId = -1;
		}
		
		String usernameWithCheckDigit = username;
		try {
			//Hardcoding in Luhn since past user IDs used this validator.
			usernameWithCheckDigit = new LuhnIdentifierValidator().getValidIdentifier(username);
		}
		catch (Exception e) {}
		
		Query query = sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "select count(*) from User u where (u.username = :uname1 or u.systemId = :uname2 or u.username = :sysid1 or u.systemId = :sysid2 or u.systemId = :uname3) and u.userId <> :uid");
		query.setString("uname1", username);
		query.setString("uname2", username);
		query.setString("sysid1", systemId);
		query.setString("sysid2", systemId);
		query.setString("uname3", usernameWithCheckDigit);
		query.setInteger("uid", userId);
		
		Long count = (Long) query.uniqueResult();
		
		log.debug("# users found: " + count);
		return (count != null && count != 0);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUser(java.lang.Integer)
	 */
	@Override
	public User getUser(Integer userId) {

		return (User) sessionFactory.getCurrentSession().get(User.class, userId);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllUsers()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<User> getAllUsers() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from User where not uuid = :daemonUserUuid order by userId")
				                                                     .setString("daemonUserUuid", Daemon.getDaemonUserUuid()).list();
		
	}
	
	/**
	 * @see org.openmrs.api.UserService#deleteUser(org.openmrs.User)
	 */
	@Override
	public void deleteUser(User user) {
		sessionFactory.getCurrentSession().delete(user);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsersByRole(org.openmrs.Role)
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsersByRole(Role role) throws DAOException {

		return (List<User>) sessionFactory.getCurrentSession().createCriteria(User.class, "u").createCriteria("roles", "r")
		        .add(Restrictions.like("r.role", role.getRole())).add(Restrictions.ne("u.uuid", Daemon.getDaemonUserUuid())).addOrder(Order.asc("u.username")).list();
		
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllPrivileges()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Privilege> getAllPrivileges() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from Privilege p order by p.privilege").list();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getPrivilege(String)
	 */
	@Override
	public Privilege getPrivilege(String p) throws DAOException {
		return (Privilege) sessionFactory.getCurrentSession().get(Privilege.class, p);
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#deletePrivilege(org.openmrs.Privilege)
	 */
	@Override
	public void deletePrivilege(Privilege privilege) throws DAOException {
		sessionFactory.getCurrentSession().delete(privilege);
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#savePrivilege(org.openmrs.Privilege)
	 */
	@Override
	public Privilege savePrivilege(Privilege privilege) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(privilege);
		return privilege;
	}
	
	/**
	 * @see org.openmrs.api.UserService#purgeRole(org.openmrs.Role)
	 */
	@Override
	public void deleteRole(Role role) throws DAOException {
		sessionFactory.getCurrentSession().delete(role);
	}
	
	/**
	 * @see org.openmrs.api.UserService#saveRole(org.openmrs.Role)
	 */
	@Override
	public Role saveRole(Role role) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(role);
		return role;
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllRoles()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Role> getAllRoles() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from Role r order by r.role").list();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getRole(String)
	 */
	@Override
	public Role getRole(String r) throws DAOException {
		return (Role) sessionFactory.getCurrentSession().get(Role.class, r);
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#changePassword(org.openmrs.User, java.lang.String)
	 */
	@Override
	public void changePassword(User u, String pw) throws DAOException {
		User authUser = Context.getAuthenticatedUser();
		
		if (authUser == null) {
			authUser = u;
		}
		
		log.debug("updating password");
		//update the user with the new password
		String salt = Security.getRandomToken();
		String newHashedPassword = Security.encodeString(pw + salt);
		
		updateUserPassword(newHashedPassword, salt, authUser.getUserId(), new Date(), u.getUserId());
		
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#changeHashedPassword(User, String, String)
	 */
	@Override
	public void changeHashedPassword(User user, String hashedPassword, String salt) throws DAOException {
		User authUser = Context.getAuthenticatedUser();
		updateUserPassword(hashedPassword, salt, authUser.getUserId(), new Date(), user.getUserId());
	}
	
	/**
	 * @param newHashedPassword
	 * @param salt
	 * @param userId
	 * @param date
	 * @param userId2
	 */
	private void updateUserPassword(String newHashedPassword, String salt, Integer changedBy, Date dateChanged,
	        Integer userIdToChange) {
		User changeForUser = getUser(userIdToChange);
		if (changeForUser == null) {
			throw new DAOException("Couldn't find user to set password for userId=" + userIdToChange);
		}
		User changedByUser = getUser(changedBy);
		LoginCredential credentials = getLoginCredential(changeForUser);
		credentials.setUserId(userIdToChange);
		credentials.setHashedPassword(newHashedPassword);
		credentials.setSalt(salt);
		credentials.setChangedBy(changedByUser);
		credentials.setDateChanged(dateChanged);
		credentials.setUuid(changeForUser.getUuid());
		
		sessionFactory.getCurrentSession().merge(credentials);
		
		// reset lockout 
		changeForUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, "");
		changeForUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
		saveUser(changeForUser, null);
	}
	
	/**
	 * @see org.openmrs.api.UserService#changePassword(java.lang.String, java.lang.String)
	 */
	@Override
	public void changePassword(String pw, String pw2) throws DAOException {
		User u = Context.getAuthenticatedUser();
		LoginCredential credentials = getLoginCredential(u);
		if (!credentials.checkPassword(pw)) {
			log.error("Passwords don't match");
			throw new DAOException("Passwords don't match");
		}
		
		log.info("updating password for {}", u.getUsername());
		
		// update the user with the new password
		String salt = Security.getRandomToken();
		String newHashedPassword = Security.encodeString(pw2 + salt);
		updateUserPassword(newHashedPassword, salt, u.getUserId(), new Date(), u.getUserId());
	}
	
	/**
	 * @see org.openmrs.api.UserService#changeQuestionAnswer(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void changeQuestionAnswer(String pw, String question, String answer) throws DAOException {
		User u = Context.getAuthenticatedUser();
		
		LoginCredential credentials = getLoginCredential(u);
		if (!credentials.checkPassword(pw)) {
			log.error("Passwords don't match");
			throw new DAOException("Passwords don't match");
		}
		
		changeQuestionAnswer(u, question, answer);
	}
	
	/**
	 * @see org.openmrs.api.UserService#changeQuestionAnswer(User, String, String)
	 */
	@Override
	public void changeQuestionAnswer(User u, String question, String answer) throws DAOException {
		log.info("Updating secret question and answer for " + u.getUsername());
		
		LoginCredential credentials = getLoginCredential(u);
		credentials.setSecretQuestion(question);
		String hashedAnswer = Security.encodeString(answer.toLowerCase() + credentials.getSalt());
		credentials.setSecretAnswer(hashedAnswer);
		credentials.setDateChanged(new Date());
		credentials.setChangedBy(u);
		
		updateLoginCredential(credentials);
	}
	
	/**
	 * @see org.openmrs.api.UserService#isSecretAnswer(User, java.lang.String)
	 */
	@Override
	public boolean isSecretAnswer(User u, String answer) throws DAOException {
		
		if (StringUtils.isEmpty(answer)) {
			return false;
		}
		
		LoginCredential credentials = getLoginCredential(u);
		String answerOnRecord = credentials.getSecretAnswer();
		String hashedAnswer = Security.encodeString(answer.toLowerCase() + credentials.getSalt());
		return (hashedAnswer.equals(answerOnRecord));
	}
	
	/**
	 * @see UserDAO#getUsers(String, List, boolean, Integer, Integer)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<User> getUsers(String name, List<Role> roles, boolean includeRetired, Integer start, Integer length) {
		
		String hqlSelectStart = "select distinct user from User as user inner join user.person.names as name ";
		Query query = createUserSearchQuery(name, roles, includeRetired, hqlSelectStart);
		
		if (start != null) {
			query.setFirstResult(start);
		}
		if (length != null && length > 0) {
			query.setMaxResults(length);
		}
		
		List<User> returnList = query.list();
		
		if (!CollectionUtils.isEmpty(returnList)) {
			returnList.sort(new UserByNameComparator());
		}
		
		return returnList;
	}
	
	/**
	 * @see org.openmrs.api.UserService#generateSystemId()
	 */
	@Override
	public Integer generateSystemId() {
		
		String hql = "select max(userId) from User";
		
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		
		Object object = query.uniqueResult();
		
		Integer id;
		if (object instanceof Number) {
			id = ((Number) query.uniqueResult()).intValue() + 1;
		} else {
			log.warn("What is being returned here? Definitely nothing expected object value: '" + object + "' of class: "
			        + object.getClass());
			id = 1;
		}
		
		return id;
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsersByName(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<User> getUsersByName(String givenName, String familyName, boolean includeRetired) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(User.class);
		crit.createAlias("person", "person");
		crit.createAlias("person.names", "names");
		crit.add(Restrictions.eq("names.givenName", givenName));
		crit.add(Restrictions.eq("names.familyName", familyName));
		crit.add(Restrictions.ne("uuid", Daemon.getDaemonUserUuid()));
		crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		if (!includeRetired) {
			crit.add(Restrictions.eq("retired", false));
		}
		return new ArrayList<>((List<User>) crit.list());
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getPrivilegeByUuid(java.lang.String)
	 */
	@Override
	public Privilege getPrivilegeByUuid(String uuid) {
		return (Privilege) sessionFactory.getCurrentSession().createQuery("from Privilege p where p.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getRoleByUuid(java.lang.String)
	 */
	@Override
	public Role getRoleByUuid(String uuid) {
		return (Role) sessionFactory.getCurrentSession().createQuery("from Role r where r.uuid = :uuid").setString("uuid",
		    uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getUserByUuid(java.lang.String)
	 */
	@Override
	public User getUserByUuid(String uuid) {
		User ret = null;
		
		if (uuid != null) {
			uuid = uuid.trim();
			ret = (User) sessionFactory.getCurrentSession().createQuery("from User u where u.uuid = :uuid").setString(
			    "uuid", uuid).uniqueResult();
		}
		
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getLoginCredential(org.openmrs.User)
	 */
	@Override
	public LoginCredential getLoginCredential(User user) {
		return (LoginCredential) sessionFactory.getCurrentSession().get(LoginCredential.class, user.getUserId());
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getLoginCredential(org.openmrs.User)
	 */
	@Override
	public LoginCredential getLoginCredentialByUuid(String uuid) {
		if (uuid == null) {
			return null;
		} else {
			return (LoginCredential) sessionFactory.getCurrentSession().createQuery(
			    "from LoginCredential where uuid = :uuid").setString("uuid", uuid.trim()).uniqueResult();
		}
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#updateLoginCredential(LoginCredential)
	 */
	@Override
	public void updateLoginCredential(LoginCredential credential) {
		sessionFactory.getCurrentSession().update(credential);
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getUsersByPerson(org.openmrs.Person, boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<User> getUsersByPerson(Person person, boolean includeRetired) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(User.class);
		crit.add(Restrictions.ne("uuid", Daemon.getDaemonUserUuid()));
		if (person != null) {
			crit.add(Restrictions.eq("person", person));
		}
		if (!includeRetired) {
			crit.add(Restrictions.eq("retired", false));
		}
		return (List<User>) crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getCountOfUsers(String, List, boolean)
	 */
	@Override
	public Integer getCountOfUsers(String name, List<Role> roles, boolean includeRetired) {
		String hqlSelectStart = "select count(distinct user) from User as user inner join user.person.names as name ";
		Query query = createUserSearchQuery(name, roles, includeRetired, hqlSelectStart);
		
		return ((Long) query.uniqueResult()).intValue();
	}
	
	/**
	 * Utility methods that creates a hibernate query object from the specified arguments
	 * 
	 * @param name The name of the user to search against
	 * @param roles the roles to match against
	 * @param includeRetired Specifies if retired users should be included or not
	 * @param hqlSelectStart The starting phrase of the select statement that includes the joined
	 *            tables
	 * @return the created hibernate query object
	 */
	private Query createUserSearchQuery(String name, List<Role> roles, boolean includeRetired, String hqlSelectStart) {
		
		log.debug("name: " + name);
		
		name = HibernateUtil.escapeSqlWildcards(name, sessionFactory);
		
		// Create an HQL query like this:
		// select distinct user
		// from User as user inner join user.person.names as name inner join user.roles as role
		// where (user.username like :name1 or ...and for systemId givenName familyName familyName2...)
		//   and (user.username like :name2 or ...and for systemId givenName familyName familyName2...)
		//   ...repeat for all name fragments...
		//	 and role in :roleList 
		//   and user.retired = false
		// order by username asc
		List<String> criteria = new ArrayList<>();
		int counter = 0;
		Map<String, String> namesMap = new HashMap<>();
		if (name != null) {
			name = name.replace(", ", " ");
			String[] names = name.split(" ");
			for (String n : names) {
				if (n != null && n.length() > 0) {
					// compare each fragment of the query against username, systemId, given, middle, family, and family2
					String key = "name" + ++counter;
					String value = n + "%";
					namesMap.put(key, value);
					criteria.add("(user.username like :" + key + " or user.systemId like :" + key
					        + " or name.givenName like :" + key + " or name.middleName like :" + key
					        + " or name.familyName like :" + key + " or name.familyName2 like :" + key + ")");
				}
			}
		}
		
		if (!includeRetired) {
			criteria.add("user.retired = false");
		}
		
		// build the hql query
		StringBuilder hql = new StringBuilder(hqlSelectStart);
		boolean searchOnRoles = false;
		
		if (CollectionUtils.isNotEmpty(roles)) {
			hql.append("inner join user.roles as role ");
			searchOnRoles = true;
		}
		hql.append("where user.uuid != :DAEMON_USER_UUID ");
		
		if (!criteria.isEmpty() || searchOnRoles) {
			hql.append("and ");
		}
		for (Iterator<String> i = criteria.iterator(); i.hasNext();) {
			hql.append(i.next()).append(" ");
			if (i.hasNext()) {
				hql.append("and ");
			}
		}
		
		//Match against the specified roles
		if (searchOnRoles) {
			if (!criteria.isEmpty()) {
				hql.append(" and ");
			}
			hql.append(" role in (:roleList)");
		}
		
		Query query = sessionFactory.getCurrentSession().createQuery(hql.toString());
		query.setParameter("DAEMON_USER_UUID", Daemon.getDaemonUserUuid());
		for (Map.Entry<String, String> e : namesMap.entrySet()) {
			query.setString(e.getKey(), e.getValue());
		}
		
		if (searchOnRoles) {
			query.setParameterList("roleList", roles);
		}
		
		return query;
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#createActivationKey(org.openmrs.User)
	 */
	@Override
	public void setUserActivationKey(LoginCredential credentials) {		
			sessionFactory.getCurrentSession().merge(credentials);	
	}
}
