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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.UserDAO;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.Security;
import org.springframework.orm.ObjectRetrievalFailureException;

public class HibernateUserDAO implements
		UserDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	public HibernateUserDAO() { }

	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.UserService#createUser(org.openmrs.User)
	 */
	public User createUser(User user, String password) {
		if (hasDuplicateUsername(user.getUsername(), user.getSystemId(), user.getUserId()))
			throw new DAOException("Username " + user.getUsername() + " or system id " + user.getSystemId() + " is already in use.");
		
		try {
			sessionFactory.getCurrentSession().save(user);
			//sessionFactory.getCurrentSession().refresh(user);
		}
		catch (NonUniqueObjectException e) {
			sessionFactory.getCurrentSession().merge(user);
		}
		
		//update the new user with the password
		String salt = Security.getRandomToken();
		String hashedPassword = Security.encodeString(password + salt);
		
		updateUserPassword(hashedPassword, salt, Context.getAuthenticatedUser().getUserId(), new Date(), user.getUserId());
			
		return user;
	}

	/**
	 * @see org.openmrs.api.db.UserService#getUserByUsername(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public User getUserByUsername(String username) {
		List<User> users = sessionFactory.getCurrentSession()
				.createQuery(
						"from User u where u.voided = 0 and (u.username = ? or u.systemId = ?)")
				.setString(0, username)
				.setString(1, username)
				.list();
		
		if (users == null || users.size() == 0) {
			log.warn("request for username '" + username + "' not found");
			return null;
			//throw new ObjectRetrievalFailureException(User.class, username);
		}

		return users.get(0);
	}
	
	/**
	 * @see org.openmrs.api.db.UserService#hasDuplicateUsername(org.openmrs.User)
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
			Integer cd = OpenmrsUtil.getCheckDigit(username);
			usernameWithCheckDigit = usernameWithCheckDigit + "-" + cd;
		}
		catch (Exception e) {}
		
		Long count = (Long) sessionFactory.getCurrentSession().createQuery(
				"select count(*) from User u where (u.username = :uname1 or u.systemId = :uname2 or u.username = :sysid1 or u.systemId = :sysid2 or u.systemId = :uname3) and u.userId <> :uid")
				.setString("uname1", username)
				.setString("uname2", username)
				.setString("sysid1", systemId)
				.setString("sysid2", systemId)
				.setString("uname3", usernameWithCheckDigit)
				.setInteger("uid", userId)
				.uniqueResult();

		log.debug("# users found: " + count);
		if (count == null || count == 0)
			return false;
		else
			return true;
	}
	
	/**
	 * @see org.openmrs.api.db.UserService#getUser(java.lang.Long)
	 */
	public User getUser(Integer userId) {
		User user = (User) sessionFactory.getCurrentSession().get(User.class, userId);
		
		if (user == null) {
			log.warn("request for user '" + userId + "' not found");
			throw new ObjectRetrievalFailureException(User.class, userId);
		}
		return user;
	}
	
	/**
	 * @see org.openmrs.api.db.UserService#getUsers()
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsers() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from User u order by u.userId")
								.list();
	}

	/**
	 * @see org.openmrs.api.db.UserService#updateUser(org.openmrs.User)
	 */
	public void updateUser(User user) {
		if (user.getUserId() == null)
			createUser(user, "");
		else {
			if (log.isDebugEnabled()) {
				log.debug("update user id: " + user.getUserId());
			}
			
			Object obj = sessionFactory.getCurrentSession().get(Person.class, user.getUserId());
			if (!(obj instanceof User)) {
				insertUserStub(user);
			}

			sessionFactory.getCurrentSession().merge(user);
			
		}
	}
	
	/**
	 * Inserts a row into the user table
	 * 
	 * This avoids hibernate's bunging of our person/patient/user inheritance
	 * 
	 * @param user the user to create a stub for
	 */
	private void insertUserStub(User user) {
		Connection connection = sessionFactory.getCurrentSession().connection();
		try {
			PreparedStatement ps = connection.prepareStatement("INSERT INTO users (user_id, system_id, creator, date_created) VALUES (?, ?, ?, ?)");
			
			ps.setInt(1, user.getUserId());
			ps.setString(2, user.getSystemId());
			ps.setInt(3, user.getCreator().getUserId());
			ps.setDate(4, new java.sql.Date(user.getDateCreated().getTime()));
	
			ps.executeUpdate();
			
		}
		catch (SQLException e) {
			log.warn("SQL Exception while trying to create a user stub", e);
		}
		
		sessionFactory.getCurrentSession().flush();
	}

	/**
	 * @see org.openmrs.api.db.UserService#deleteUser(org.openmrs.User)
	 */
	public void deleteUser(User user) {
		HibernatePersonDAO.deletePersonAndAttributes(sessionFactory, user);
	}

	/**
	 * @see org.openmrs.api.db.UserService#getUserByRole(org.openmrs.Role)
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsersByRole(Role role) throws DAOException {
		List<User> users = sessionFactory.getCurrentSession().createCriteria(User.class, "u")
						.createCriteria("roles", "r")
						.add(Expression.like("r.role", role.getRole()))
						.addOrder(Order.asc("u.username"))
						.list();
		
		return users;
		
	}

	/**
	 * @see org.openmrs.api.db.UserService#getPrivileges()
	 */
	@SuppressWarnings("unchecked")
	public List<Privilege> getPrivileges() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from Privilege p order by p.privilege").list();
	}

	/**
	 * @see org.openmrs.api.db.UserService#getRoles()
	 */
	@SuppressWarnings("unchecked")
	public List<Role> getRoles() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from Role r order by r.role").list();
	}
	
	/**
	 * @see org.openmrs.api.db.UserService#getInheritingRoles(org.openmrs.Role)
	 */
	@SuppressWarnings("unchecked")
	public List<Role> getInheritingRoles(Role role) throws DAOException {
		/*
		Criteria crit = session.createCriteria(Role.class, "r")
			.add(Expression.in("r.inheritedRoles", role))
			.addOrder(Order.asc("r.role"));
		return crit.list();
		*/
		
		return sessionFactory.getCurrentSession().createQuery("from Role r where :role in inheritedRoles order by r.role")
				.setParameter("role", role)
				.list();
	}

	/**
	 * @see org.openmrs.api.db.UserService#getPrivilege()
	 */
	public Privilege getPrivilege(String p) throws DAOException {
		return (Privilege)sessionFactory.getCurrentSession().get(Privilege.class, p);
	}

	/**
	 * @see org.openmrs.api.db.UserService#getRole()
	 */
	public Role getRole(String r) throws DAOException {
		return (Role)sessionFactory.getCurrentSession().get(Role.class, r);
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
		String newPassword = Security.encodeString(pw + salt);
		
		updateUserPassword(newPassword, salt, authUser.getUserId(), new Date(), u.getUserId());
		
	}

	/**
	 * We have to change the password manually because we don't store the password and salt on
	 * the user
	 * 
	 * @param newPassword
	 * @param salt
	 * @param userId
	 * @param date
	 * @param userId2
	 */
    private void updateUserPassword(String newPassword, String salt, Integer changedBy, Date dateChanged, Integer userIdToChange) {
		try {
			PreparedStatement ps = getUpdateUserPasswordStatement();
			
			if (ps != null) {
				ps.setString(1, newPassword);
				ps.setString(2, salt);
				ps.setInt(3, changedBy);
				ps.setDate(4, new java.sql.Date(dateChanged.getTime()));
				ps.setInt(5, userIdToChange);
				
				ps.executeUpdate();
			}
		}
		catch (SQLException e) {
			log.warn("SQL Exception while running user-password-update ", e);
		}
		
		sessionFactory.getCurrentSession().flush();
	}
	
	/**
	 * Return or create the prepared statement for use when updating a user's password
	 * 
	 * Will return null on error
	 * 
	 * @return PreparedStatement that can be executed
	 */
	@SuppressWarnings("deprecation")
    private PreparedStatement getUpdateUserPasswordStatement() {
		// get the straight up jdbc database connection
		// TODO address this depreciation warning
		Connection connection = sessionFactory.getCurrentSession().connection();
		
		String sql = "UPDATE users SET `password` = ?, `salt` = ?, `changed_by` = ?, `date_changed` = ? WHERE `user_id` = ?";
		
		// if we're in a junit test, we're probably using hsql...and hsql
		// does not like the backtick.  Replace the backtick with the hsql
		// escape character: the double quote (or nothing).
		Dialect dialect = HibernateUtil.getDialect(sessionFactory);
		if (HSQLDialect.class.getName().equals(dialect.getClass().getName()))
			sql = sql.replace("`", "");
		
		PreparedStatement updateUserPreparedStatement = null;
		try {
			// create the prepared statement
			updateUserPreparedStatement = connection.prepareStatement(sql);
		}
		catch (SQLException e) {
			log.warn("SQL Exception while trying to create the user-password-update statement", e);
		}
		
		return updateUserPreparedStatement;
	}

	/**
	 * @see org.openmrs.api.db.UserDAO#changePassword(java.lang.String, java.lang.String)
	 */
	public void changePassword(String pw, String pw2) throws DAOException {
		User u = Context.getAuthenticatedUser();
		
		String passwordOnRecord = (String) sessionFactory.getCurrentSession().createSQLQuery(
			"select password from users where user_id = ?")
			.addScalar("password", Hibernate.STRING)
			.setInteger(0, u.getUserId())
			.uniqueResult();
		
		String saltOnRecord = (String) sessionFactory.getCurrentSession().createSQLQuery(
			"select salt from users where user_id = ?")
			.addScalar("salt", Hibernate.STRING)
			.setInteger(0, u.getUserId())
			.uniqueResult();

		String hashedPassword = Security.encodeString(pw + saltOnRecord);
		
		if (!passwordOnRecord.equals(hashedPassword)) {
			log.error("Passwords don't match");
			throw new DAOException("Passwords don't match");
		}
		
		log.debug("updating password");
		
		//update the user with the new password
		String salt = Security.getRandomToken();
		String newPassword = Security.encodeString(pw2 + salt);
		
		// do the actual password changing
		updateUserPassword(newPassword, salt, u.getUserId(), new Date(), u.getUserId());
	}
	
	public void changeQuestionAnswer(String pw, String question, String answer) throws DAOException {
		User u = Context.getAuthenticatedUser();

		String passwordOnRecord = (String) sessionFactory.getCurrentSession().createSQLQuery(
		"select password from users where user_id = ?")
		.addScalar("password", Hibernate.STRING)
		.setInteger(0, u.getUserId())
		.uniqueResult();
		
		String saltOnRecord = (String) sessionFactory.getCurrentSession().createSQLQuery(
		"select salt from users where user_id = ?")
		.addScalar("salt", Hibernate.STRING)
		.setInteger(0, u.getUserId())
		.uniqueResult();
		
		try {
			String hashedPassword = Security.encodeString(pw + saltOnRecord);
			
			if (!passwordOnRecord.equals(hashedPassword)) {
				throw new DAOException("Passwords don't match");
			}
		}
		catch (APIException e) {
			log.error(e);
			throw new DAOException(e);
		}
		
		Connection connection = sessionFactory.getCurrentSession().connection();
		try {
			PreparedStatement ps = connection.prepareStatement("UPDATE `users` SET secret_question = ?, secret_answer = ?, date_changed = ?, changed_by = ? WHERE user_id = ?");
			
			ps.setString(1, question);
			ps.setString(2, answer);
			ps.setDate(3, new java.sql.Date(new Date().getTime()));
			ps.setInt(4, u.getUserId());
			ps.setInt(5, u.getUserId());
	
			ps.executeUpdate();
		}
		catch (SQLException e) {
			log.warn("SQL Exception while trying to update a user's password", e);
		}
		
	}
	
	public boolean isSecretAnswer(User u, String answer) throws DAOException {
		
		if (answer == null || answer.equals(""))
			return false;
		
		String answerOnRecord = "";
		
		try {
			answerOnRecord = (String) sessionFactory.getCurrentSession().createSQLQuery(
			"select secret_answer from users where user_id = ?")
			.addScalar("secret_answer", Hibernate.STRING)
			.setInteger(0, u.getUserId())
			.uniqueResult();
		}
		catch (Exception e) {
			return false;
		}
		
		return (answer.equals(answerOnRecord));
	}
	
	@SuppressWarnings("unchecked")
	public List<User> findUsers(String name, List<String> roles, boolean includeVoided) {
		name = name.replace(", ", " ");
		String[] names = name.split(" ");
		
		log.debug("name: " + name);
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(User.class);
		criteria.createAlias("names", "name");
		for (String n : names) {
			if (n != null && n.length() > 0) {
				criteria.add(Expression.or(
						Expression.like("name.givenName", n, MatchMode.START),
						Expression.or(
							Expression.like("name.familyName", n, MatchMode.START),
								Expression.or(
										Expression.like("name.middleName", n, MatchMode.START),
										Expression.like("systemId", n, MatchMode.START)
										)
							)
						)
					);
			}
		}
		
		/*
		if (roles != null && roles.size() > 0) {
			criteria.createAlias("roles", "r")
				.add(Expression.in("r.role", roles))
				.createAlias("groups", "g")
				.createAlias("g.roles", "gr")
					.add(Expression.or(
							Expression.in("r.role", roles),
							Expression.in("gr.role", roles)
							));
		}
		 */
		
		if (includeVoided == false)
			criteria.add(Expression.eq("voided", false));
		
		criteria.addOrder(Order.asc("userId"));
		
		// TODO figure out how to get Hibernate to do the sql for us
		
		List returnList = new Vector();
		if (roles != null && roles.size() > 0) {
			log.debug("looping through to find matching roles");
			for (Object o : criteria.list()) {
				User u = (User)o;
				for (String r : roles)
					if (u.hasRole(r, true)) {
						returnList.add(u);
						break;
					}
			}
		}
		else {
			log.debug("not looping because there appears to be no roles");
			returnList = criteria.list();
		}
		
		return returnList;
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getAllUsers(List<Role> roles, boolean includeVoided) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(User.class);
		
		if (includeVoided == false)
			criteria.add(Expression.eq("voided", false));
		
		List<User> returnList = new Vector<User>();
		if (roles != null && roles.size() > 0) {
			for (Object o : criteria.list()) {
				User u = (User)o;
				for (Role r : roles)
					if (r != null) {
						if (u.hasRole(r.getRole(), true)) {
							returnList.add(u);
							break;
						}
					}
			}
		}
		else
			returnList = criteria.list();
		
		return returnList;
		
		// TODO figure out how to get Hibernate to do the sql for us
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#generateSystemId()
	 */
	public Integer generateSystemId() {
		
		// TODO this algorithm will fail if someone deletes a user that is not the last one.
		
		String sql = "select count(user_id) as user_id from users";
		
		Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		
		Object object = query.uniqueResult();
		
		Integer id = null;
		if (object instanceof BigInteger) 
			id = ((BigInteger)query.uniqueResult()).intValue() + 1;
		else if (object instanceof Integer)
			id = ((Integer)query.uniqueResult()).intValue() + 1;
		else {
			log.warn("What is being returned here? Definitely nothing expected object value: '" + object + "' of class: " + object.getClass());
			id = 1;
		}
		
		return id;
	}

	/**
	 * @see org.openmrs.api.db.UserDAO#findUsers(java.lang.String, java.lang.String, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<User> findUsers(String givenName, String familyName, boolean includeVoided) {
		List<User> users = new Vector<User>();
		String query = "from User u where u.names.givenName = :givenName and u.names.familyName = :familyName";
		if (!includeVoided)
			query += " and u.voided = false";
		Query q = sessionFactory.getCurrentSession().createQuery(query)
				.setString("givenName", givenName)
				.setString("familyName", familyName);
		for (User u : (List<User>) q.list()) {
			users.add(u);
		}
		return users;
	}
}
