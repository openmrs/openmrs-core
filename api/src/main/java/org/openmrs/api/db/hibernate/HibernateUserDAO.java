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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.LoginCredential;
import org.openmrs.api.db.UserDAO;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.util.Security;
import org.openmrs.util.UserByNameComparator;

/**
 * Hibernate specific database methods for the UserService
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.db.UserDAO
 * @see org.openmrs.api.UserService
 */
public class HibernateUserDAO implements UserDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
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
	public User saveUser(User user, String password) {
		
		// only change the user's password when creating a new user
		boolean isNewUser = user.getUserId() == null;
		
		sessionFactory.getCurrentSession().saveOrUpdate(user);
		
		if (isNewUser && password != null) {
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
	@SuppressWarnings("unchecked")
	public User getUserByUsername(String username) {
		Query query = sessionFactory.getCurrentSession().createQuery(
		    "from User u where u.retired = 0 and (u.username = ? or u.systemId = ?)");
		query.setString(0, username);
		query.setString(1, username);
		List<User> users = query.list();
		
		if (users == null || users.size() == 0) {
			log.warn("request for username '" + username + "' not found");
			return null;
		}
		
		return users.get(0);
	}
	
	/**
	 * @see org.openmrs.api.UserService#hasDuplicateUsername(org.openmrs.User)
	 */
	public boolean hasDuplicateUsername(String username, String systemId, Integer userId) {
		if (username == null || username.length() == 0)
			username = "-";
		if (systemId == null || systemId.length() == 0)
			systemId = "-";
		
		if (userId == null)
			userId = new Integer(-1);
		
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
	public User getUser(Integer userId) {
		User user = (User) sessionFactory.getCurrentSession().get(User.class, userId);
		
		return user;
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllUsers()
	 */
	@SuppressWarnings("unchecked")
	public List<User> getAllUsers() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from User u order by u.userId").list();
	}
	
	/**
	 * @see org.openmrs.api.UserService#deleteUser(org.openmrs.User)
	 */
	public void deleteUser(User user) {
		sessionFactory.getCurrentSession().delete(user);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsersByRole(org.openmrs.Role)
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsersByRole(Role role) throws DAOException {
		List<User> users = sessionFactory.getCurrentSession().createCriteria(User.class, "u").createCriteria("roles", "r")
		        .add(Expression.like("r.role", role.getRole())).addOrder(Order.asc("u.username")).list();
		
		return users;
		
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllPrivileges()
	 */
	@SuppressWarnings("unchecked")
	public List<Privilege> getAllPrivileges() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from Privilege p order by p.privilege").list();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getPrivilege(String)
	 */
	public Privilege getPrivilege(String p) throws DAOException {
		return (Privilege) sessionFactory.getCurrentSession().get(Privilege.class, p);
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#deletePrivilege(org.openmrs.Privilege)
	 */
	public void deletePrivilege(Privilege privilege) throws DAOException {
		sessionFactory.getCurrentSession().delete(privilege);
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#savePrivilege(org.openmrs.Privilege)
	 */
	public Privilege savePrivilege(Privilege privilege) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(privilege);
		return privilege;
	}
	
	/**
	 * @see org.openmrs.api.UserService#purgeRole(org.openmrs.Role)
	 */
	public void deleteRole(Role role) throws DAOException {
		sessionFactory.getCurrentSession().delete(role);
	}
	
	/**
	 * @see org.openmrs.api.UserService#saveRole(org.openmrs.Role)
	 */
	public Role saveRole(Role role) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(role);
		return role;
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllRoles()
	 */
	@SuppressWarnings("unchecked")
	public List<Role> getAllRoles() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from Role r order by r.role").list();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getRole(String)
	 */
	public Role getRole(String r) throws DAOException {
		return (Role) sessionFactory.getCurrentSession().get(Role.class, r);
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#changePassword(org.openmrs.User, java.lang.String)
	 */
	public void changePassword(User u, String pw) throws DAOException {
		User authUser = Context.getAuthenticatedUser();
		
		if (authUser == null)
			authUser = u;
		
		log.debug("updating password");
		//update the user with the new password
		String salt = Security.getRandomToken();
		String newHashedPassword = Security.encodeString(pw + salt);
		
		updateUserPassword(newHashedPassword, salt, authUser.getUserId(), new Date(), u.getUserId());
		
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#changeHashedPassword(User, String, String)
	 */
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
		if (changeForUser == null)
			throw new DAOException("Couldn't find user to set password for userId=" + userIdToChange);
		User changedByUser = getUser(changedBy);
		LoginCredential credentials = new LoginCredential();
		credentials.setUserId(userIdToChange);
		credentials.setHashedPassword(newHashedPassword);
		credentials.setSalt(salt);
		credentials.setChangedBy(changedByUser);
		credentials.setDateChanged(dateChanged);
		credentials.setUuid(changeForUser.getUuid());
		
		sessionFactory.getCurrentSession().merge(credentials);
	}
	
	/**
	 * @see org.openmrs.api.UserService#changePassword(java.lang.String, java.lang.String)
	 */
	public void changePassword(String pw, String pw2) throws DAOException {
		User u = Context.getAuthenticatedUser();
		LoginCredential credentials = getLoginCredential(u);
		if (!credentials.checkPassword(pw)) {
			log.error("Passwords don't match");
			throw new DAOException("Passwords don't match");
		}
		
		log.info("updating password for " + u.getUsername());
		
		// update the user with the new password
		String salt = Security.getRandomToken();
		String newHashedPassword = Security.encodeString(pw2 + salt);
		updateUserPassword(newHashedPassword, salt, u.getUserId(), new Date(), u.getUserId());
	}
	
	/**
	 * @see org.openmrs.api.UserService#changeQuestionAnswer(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
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
	public void changeQuestionAnswer(User u, String question, String answer) throws DAOException {
		log.info("Updating secret question and answer for " + u.getUsername());
		
		LoginCredential credentials = getLoginCredential(u);
		credentials.setSecretQuestion(question);
		credentials.setSecretAnswer(answer);
		credentials.setDateChanged(new Date());
		credentials.setChangedBy(u);
		
		updateLoginCredential(credentials);
	}
	
	/**
	 * @see org.openmrs.api.UserService#isSecretAnswer(User, java.lang.String)
	 */
	public boolean isSecretAnswer(User u, String answer) throws DAOException {
		
		if (answer == null || answer.equals(""))
			return false;
		
		String answerOnRecord = getLoginCredential(u).getSecretAnswer();
		return (answer.equals(answerOnRecord));
	}
	
	/**
	 * @see UserDAO#getUsers(String, List, boolean, Integer, Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsers(String name, List<Role> roles, boolean includeRetired, Integer start, Integer length) {
		
		String hqlSelectStart = "select distinct user from User as user inner join user.person.names as name ";
		Query query = createUserSearchQuery(name, roles, includeRetired, hqlSelectStart);
		
		if (start != null)
			query.setFirstResult(start);
		if (length != null && length > 0)
			query.setMaxResults(length);
		
		List<User> returnList = query.list();
		
		if (!CollectionUtils.isEmpty(returnList))
			Collections.sort(returnList, new UserByNameComparator());
		
		return returnList;
	}
	
	/**
	 * @see org.openmrs.api.UserService#generateSystemId()
	 */
	public Integer generateSystemId() {
		
		// TODO this algorithm will fail if someone deletes a user that is not the last one.
		
		String sql = "select count(user_id) as user_id from users";
		
		Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		
		Object object = query.uniqueResult();
		
		Integer id = null;
		if (object instanceof BigInteger)
			id = ((BigInteger) query.uniqueResult()).intValue() + 1;
		else if (object instanceof Integer)
			id = ((Integer) query.uniqueResult()).intValue() + 1;
		else {
			log.warn("What is being returned here? Definitely nothing expected object value: '" + object + "' of class: "
			        + object.getClass());
			id = 1;
		}
		
		return id;
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsersByName(java.lang.String, java.lang.String, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsersByName(String givenName, String familyName, boolean includeRetired) {
		List<User> users = new Vector<User>();
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(User.class);
		crit.createAlias("person", "person");
		crit.createAlias("person.names", "names");
		crit.add(Expression.eq("names.givenName", givenName));
		crit.add(Expression.eq("names.familyName", familyName));
		crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		if (!includeRetired)
			crit.add(Expression.eq("retired", false));
		for (User u : (List<User>) crit.list()) {
			users.add(u);
		}
		return users;
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getPrivilegeByUuid(java.lang.String)
	 */
	public Privilege getPrivilegeByUuid(String uuid) {
		return (Privilege) sessionFactory.getCurrentSession().createQuery("from Privilege p where p.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getRoleByUuid(java.lang.String)
	 */
	public Role getRoleByUuid(String uuid) {
		return (Role) sessionFactory.getCurrentSession().createQuery("from Role r where r.uuid = :uuid").setString("uuid",
		    uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getUserByUuid(java.lang.String)
	 */
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
	public LoginCredential getLoginCredential(User user) {
		return (LoginCredential) sessionFactory.getCurrentSession().get(LoginCredential.class, user.getUserId());
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getLoginCredential(org.openmrs.User)
	 */
	public LoginCredential getLoginCredentialByUuid(String uuid) {
		if (uuid == null)
			return null;
		else
			return (LoginCredential) sessionFactory.getCurrentSession().createQuery(
			    "from LoginCredential where uuid = :uuid").setString("uuid", uuid.trim()).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#updateLoginCredential(org.openmrs.LoginCredential)
	 */
	public void updateLoginCredential(LoginCredential credential) {
		sessionFactory.getCurrentSession().update(credential);
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#getUsersByPerson(org.openmrs.Person, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsersByPerson(Person person, boolean includeRetired) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(User.class);
		if (person != null)
			crit.add(Restrictions.eq("person", person));
		if (!includeRetired)
			crit.add(Restrictions.eq("retired", false));
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
		List<String> criteria = new ArrayList<String>();
		int counter = 0;
		Map<String, String> namesMap = new HashMap<String, String>();
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
		
		if (includeRetired == false)
			criteria.add("user.retired = false");
		
		// build the hql query
		String hql = hqlSelectStart;
		boolean searchOnRoles = false;
		
		if (CollectionUtils.isNotEmpty(roles)) {
			hql += "inner join user.roles as role ";
			searchOnRoles = true;
		}
		
		if (criteria.size() > 0 || searchOnRoles)
			hql += "where ";
		for (Iterator<String> i = criteria.iterator(); i.hasNext();) {
			hql += i.next() + " ";
			if (i.hasNext())
				hql += "and ";
		}
		
		//Match against the specified roles
		if (searchOnRoles) {
			if (criteria.size() > 0){
				hql += " and ";
			}
			hql += " role in (:roleList)";
		}
		
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		
		for (Map.Entry<String, String> e : namesMap.entrySet())
			query.setString(e.getKey(), e.getValue());
		
		if (searchOnRoles) {
			query.setParameterList("roleList", roles);
		}
		
		return query;
	}
	
}
