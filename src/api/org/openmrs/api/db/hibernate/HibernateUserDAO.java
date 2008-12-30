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
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.UserDAO;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.util.Security;

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
	 * @see org.openmrs.api.UserService#saveUser(org.openmrs.User)
	 */
	public User saveUser(User user, String password) {
		
		insertUserStubIfNeeded(user);
		
		sessionFactory.getCurrentSession().saveOrUpdate(user);
		
		if (password != null) {
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
		List<User> users = sessionFactory.getCurrentSession().createQuery(
		    "from User u where u.voided = 0 and (u.username = ? or u.systemId = ?)").setString(0, username).setString(1,
		    username).list();
		
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
		
		Long count = (Long) sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "select count(*) from User u where (u.username = :uname1 or u.systemId = :uname2 or u.username = :sysid1 or u.systemId = :sysid2 or u.systemId = :uname3) and u.userId <> :uid")
		        .setString("uname1", username).setString("uname2", username).setString("sysid1", systemId).setString(
		            "sysid2", systemId).setString("uname3", usernameWithCheckDigit).setInteger("uid", userId).uniqueResult();
		
		log.debug("# users found: " + count);
		if (count == null || count == 0)
			return false;
		else
			return true;
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUser(java.lang.Long)
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
	 * Inserts a row into the user table.<br/>
	 * <br/>
	 * This avoids hibernate's bunging of our person/patient/user inheritance
	 * 
	 * @param user the user to create a stub for
	 */
	private void insertUserStubIfNeeded(User user) {
		Connection connection = sessionFactory.getCurrentSession().connection();
		
		boolean stubInsertNeeded = false;
		
		if (user.getUserId() != null) {
			// check if there is a row with a matching users.user_id 
			try {
				PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE user_id = ?");
				ps.setInt(1, user.getUserId());
				ps.execute();
				
				if (ps.getResultSet().next())
					stubInsertNeeded = false;
				else
					stubInsertNeeded = true;
				
			}
			catch (SQLException e) {
				log.error("Error while trying to see if this person is a user already", e);
			}
		}
		
		if (stubInsertNeeded) {
			try {
				PreparedStatement ps = connection
				        .prepareStatement("INSERT INTO users (user_id, system_id, creator, date_created, voided) VALUES (?, ?, ?, ?, ?)");
				
				ps.setInt(1, user.getUserId());
				ps.setString(2, user.getSystemId());
				ps.setInt(3, user.getCreator().getUserId());
				ps.setDate(4, new java.sql.Date(user.getDateCreated().getTime()));
				ps.setBoolean(5, false);
				
				ps.executeUpdate();
				
			}
			catch (SQLException e) {
				log.warn("SQL Exception while trying to create a user stub", e);
			}
		}
		
		//sessionFactory.getCurrentSession().flush();
	}
	
	/**
	 * @see org.openmrs.api.UserService#deleteUser(org.openmrs.User)
	 */
	public void deleteUser(User user) {
		HibernatePersonDAO.deletePersonAndAttributes(sessionFactory, user);
	}
	
	/**
	 * @see org.openmrs.api.db.UserService#getUserByRole(org.openmrs.Role)
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
	 * @see org.openmrs.api.UserService#getPrivilege()
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
	 * @see org.openmrs.api.UserService#deleteRole(org.openmrs.Role)
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
	 * @see org.openmrs.api.UserService#getRole()
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
		String newPassword = Security.encodeString(pw + salt);
		
		updateUserPassword(newPassword, salt, authUser.getUserId(), new Date(), u.getUserId());
		
	}
	
	/**
	 * We have to change the password manually because we don't store the password and salt on the
	 * user
	 * 
	 * @param newPassword
	 * @param salt
	 * @param userId
	 * @param date
	 * @param userId2
	 */
	private void updateUserPassword(String newPassword, String salt, Integer changedBy, Date dateChanged,
	                                Integer userIdToChange) {
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
	 * Return or create the prepared statement for use when updating a user's password. <br/>
	 * <br/>
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
	 * @see org.openmrs.api.UserService#changePassword(java.lang.String, java.lang.String)
	 */
	public void changePassword(String pw, String pw2) throws DAOException {
		User u = Context.getAuthenticatedUser();
		
		String passwordOnRecord = (String) sessionFactory.getCurrentSession().createSQLQuery(
		    "select password from users where user_id = ?").addScalar("password", Hibernate.STRING).setInteger(0,
		    u.getUserId()).uniqueResult();
		
		String saltOnRecord = (String) sessionFactory.getCurrentSession().createSQLQuery(
		    "select salt from users where user_id = ?").addScalar("salt", Hibernate.STRING).setInteger(0, u.getUserId())
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
	
	/**
	 * @see org.openmrs.api.UserService#changeQuestionAnswer(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void changeQuestionAnswer(String pw, String question, String answer) throws DAOException {
		User u = Context.getAuthenticatedUser();
		
		String passwordOnRecord = (String) sessionFactory.getCurrentSession().createSQLQuery(
		    "select password from users where user_id = ?").addScalar("password", Hibernate.STRING).setInteger(0,
		    u.getUserId()).uniqueResult();
		
		String saltOnRecord = (String) sessionFactory.getCurrentSession().createSQLQuery(
		    "select salt from users where user_id = ?").addScalar("salt", Hibernate.STRING).setInteger(0, u.getUserId())
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
			PreparedStatement ps = connection
			        .prepareStatement("UPDATE `users` SET secret_question = ?, secret_answer = ?, date_changed = ?, changed_by = ? WHERE user_id = ?");
			
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
	
	/**
	 * @see org.openmrs.api.UserService#isSecretAnswer(User, java.lang.String)
	 */
	public boolean isSecretAnswer(User u, String answer) throws DAOException {
		
		if (answer == null || answer.equals(""))
			return false;
		
		String answerOnRecord = "";
		
		try {
			answerOnRecord = (String) sessionFactory.getCurrentSession().createSQLQuery(
			    "select secret_answer from users where user_id = ?").addScalar("secret_answer", Hibernate.STRING)
			        .setInteger(0, u.getUserId()).uniqueResult();
		}
		catch (Exception e) {
			return false;
		}
		
		return (answer.equals(answerOnRecord));
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsers(java.lang.String, java.util.List, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsers(String name, List<Role> roles, boolean includeVoided) {
		log.debug("name: " + name);
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(User.class);
		
		if (name != null) {
			criteria.createAlias("names", "name");
			
			name = name.replace(", ", " ");
			String[] names = name.split(" ");
			for (String n : names) {
				if (n != null && n.length() > 0) {
					criteria.add(Expression.or(Expression.like("name.givenName", n, MatchMode.START), Expression.or(
					    Expression.like("name.familyName", n, MatchMode.START), Expression.or(Expression.like(
					        "name.middleName", n, MatchMode.START), Expression.or(Expression.like("systemId", n,
					        MatchMode.START), Expression.like("username", n, MatchMode.START))))));
				}
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
		
		if (roles != null && roles.size() > 0) {
			List returnList = new Vector();
			
			log.debug("looping through to find matching roles");
			for (Object o : criteria.list()) {
				User u = (User) o;
				for (Role r : roles)
					if (u.hasRole(r.getRole(), true)) {
						returnList.add(u);
						break;
					}
			}
			
			return returnList;
		} else {
			log.debug("not looping because there appears to be no roles");
			return criteria.list();
		}
		
	}
	
	/**
	 * @see org.openmrs.api.api.UserService#generateSystemId()
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
	public List<User> getUsersByName(String givenName, String familyName, boolean includeVoided) {
		List<User> users = new Vector<User>();
		String query = "from User u where u.names.givenName = :givenName and u.names.familyName = :familyName";
		if (!includeVoided)
			query += " and u.voided = false";
		Query q = sessionFactory.getCurrentSession().createQuery(query).setString("givenName", givenName).setString(
		    "familyName", familyName);
		for (User u : (List<User>) q.list()) {
			users.add(u);
		}
		return users;
	}
}
