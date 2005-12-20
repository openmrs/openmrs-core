package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.openmrs.Group;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.UserDAO;
import org.openmrs.util.Security;
import org.springframework.orm.ObjectRetrievalFailureException;

public class HibernateUserDAO implements
		UserDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateUserDAO(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.db.UserService#createUser(org.openmrs.User)
	 */
	public void createUser(User user, String password) {
		Session session = HibernateUtil.currentSession();

		User u = (User)session.createQuery("from User u where u.username = ?")
						.setString(0, user.getUsername())
						.uniqueResult();
		
		//TODO check for illegal characters in username
		
		if (u != null)
			throw new DAOException("Username currently in use by '" + user.getFirstName() + " " + user.getLastName() + "'");
		
		try {
			//add all data minus the password as a new user
			HibernateUtil.beginTransaction();
			user.setDateCreated(new Date());
			user.setCreator(context.getAuthenticatedUser());
			session.saveOrUpdate(user);
			HibernateUtil.commitTransaction();

			
			//update the new user with the password
			HibernateUtil.beginTransaction();
			String salt = Security.getRandomToken();
			String hashedPassword = Security.encodeString(password + salt);
			session.createQuery("update User set password = :pw, salt = :salt where user_id = :username")
				.setParameter("pw", hashedPassword)
				.setParameter("salt", salt)
				.setParameter("username", user.getUserId())
				.executeUpdate();
			HibernateUtil.commitTransaction();
			
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e.getMessage());
		}	
	}

	/**
	 * @see org.openmrs.api.db.UserService#getUserByUsername(java.lang.String)
	 */
	public User getUserByUsername(String username) {
		Session session = HibernateUtil.currentSession();

		List<User> users = session
				.createQuery(
						"from User u where (u.voided is null or u.voided = 0) and u.username = ?")
				.setString(0, username).list();
		
		if (users == null || users.size() == 0) {
			log.warn("request for username '" + username + "' not found");
			return null;
			//throw new ObjectRetrievalFailureException(User.class, username);
		}

		return users.get(0);
	}

	/**
	 * @see org.openmrs.api.db.UserService#isDuplicateUsername(java.lang.String)
	 */
	public boolean isDuplicateUsername(User user) {
		Session session = HibernateUtil.currentSession();

		String username = user.getUsername();
		if (username == null)
			username = "";
		Integer userid = user.getUserId();
		if (userid == null)
			userid = new Integer(-1);
		
		Integer count = (Integer) session.createQuery(
				"select count(*) from User u where u.username = ? and u.userId <> ?")
				.setString(0, username)
				.setInteger(1, userid)
				.uniqueResult();
		/*
		List<User> users = session
				.createQuery(
						"from User u where u.username = ? and u.userId <> ?")
				.setString(0, username)
				.setInteger(1, userid)
				.list();
		*/
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
		Session session = HibernateUtil.currentSession();
		User user = (User) session.get(User.class, userId);
		
		if (user == null) {
			log.warn("request for user '" + userId + "' not found");
			throw new ObjectRetrievalFailureException(User.class, userId);
		}
		return user;
	}
	
	/**
	 * @see org.openmrs.api.db.UserService#getUsers()
	 */
	public List<User> getUsers() throws DAOException {
		Session session = HibernateUtil.currentSession();
		List<User> users = session.createQuery("from User u order by u.username")
								.list();
		
		return users;
	}

	/**
	 * @see org.openmrs.api.db.UserService#updateUser(org.openmrs.User)
	 */
	public void updateUser(User user) {
		if (user.getCreator() == null)
			createUser(user, "");
		else {
			if (log.isDebugEnabled()) {
				log.debug("update user id: " + user.getUserId());
			}
			Session session = HibernateUtil.currentSession();
			try {
				HibernateUtil.beginTransaction();
				try {
					session.update(user);
				}
				catch (NonUniqueObjectException e) {
					User u = (User)session.merge(user);
					session.evict(u);
					session.update(user);
				}
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e.getMessage());
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.UserService#voidUser(org.openmrs.User,
	 *      java.lang.String)
	 */
	public void voidUser(User user, String reason) {
		user.setVoided(true);
		user.setVoidReason(reason);
		user.setVoidedBy(context.getAuthenticatedUser());
		user.setDateVoided(new Date());
		updateUser(user);
	}

	/**
	 * @see org.openmrs.api.db.UserService#deleteUser(org.openmrs.User)
	 */
	public void deleteUser(User user) {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(user);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e.getMessage());
		}
	}

	/**
	 * @see org.openmrs.api.db.UserService#grantUserRole(org.openmrs.User,
	 *      org.openmrs.Role)
	 */
	public void grantUserRole(User user, Role role) throws DAOException {
		user.addRole(role);
		updateUser(user);
	}

	/**
	 * @see org.openmrs.api.db.UserService#revokeUserRole(org.openmrs.User,
	 *      org.openmrs.Role)
	 */
	public void revokeUserRole(User user, Role role) throws DAOException {
		user.removeRole(role);
		updateUser(user);
	}

	/**
	 * @see org.openmrs.api.db.UserService#getUserByRole(org.openmrs.Role)
	 */
	public List<User> getUsersByRole(Role role) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		List<User> users = session.createCriteria(User.class, "u")
						.createCriteria("roles", "r")
						.add(Expression.like("r.role", role.getRole()))
						.addOrder(Order.asc("u.username"))
						.list();
		
		return users;
		
	}

	/**
	 * @see org.openmrs.api.db.UserService#unvoidUser(org.openmrs.User)
	 */
	public void unvoidUser(User user) throws DAOException {
		user.setVoided(false);
		user.setVoidReason(null);
		user.setVoidedBy(null);
		user.setDateVoided(null);
		updateUser(user);
	}

	/**
	 * @see org.openmrs.api.db.UserService#getPrivileges()
	 */
	public List<Privilege> getPrivileges() throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		List<Privilege> privileges = session.createQuery("from Privilege p order by p.privilege").list();
		
		return privileges;
	}

	/**
	 * @see org.openmrs.api.db.UserService#getRoles()
	 */
	public List<Role> getRoles() throws DAOException {

		Session session = HibernateUtil.currentSession();
		
		List<Role> roles = session.createQuery("from Role r order by r.role").list();
		
		return roles;
	}

	/**
	 * @see org.openmrs.api.db.UserService#getPrivilege()
	 */
	public Privilege getPrivilege(String p) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		Privilege privilege = (Privilege)session.get(Privilege.class, p);
		
		return privilege;
	}

	/**
	 * @see org.openmrs.api.db.UserService#getRole()
	 */
	public Role getRole(String r) throws DAOException {

		Session session = HibernateUtil.currentSession();
		Role role = (Role)session.get(Role.class, r);
		
		return role;
	}
	
	/**
	 * @see org.openmrs.api.db.UserService#getGroups()
	 */
	public List<Group> getGroups() throws DAOException {

		Session session = HibernateUtil.currentSession();
		
		List<Group> groups = session.createQuery("from Group r order by r.group").list();
		
		return groups;
	}

	/**
	 * @see org.openmrs.api.db.UserService#getGroup()
	 */
	public Group getGroup(String r) throws DAOException {

		Session session = HibernateUtil.currentSession();
		Group group = (Group)session.get(Group.class, r);
		
		return group;
	}
	
	/**
	 * @see org.openmrs.api.db.UserDAO#changePassword(java.lang.String, java.lang.String)
	 */
	public void changePassword(String pw, String pw2) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		User u = context.getAuthenticatedUser();
		
		String passwordOnRecord = (String) session.createSQLQuery(
			"select password from users where user_id = ?")
			.addScalar("password", Hibernate.STRING)
			.setInteger(0, u.getUserId())
			.uniqueResult();
		
		String saltOnRecord = (String) session.createSQLQuery(
			"select salt from users where user_id = ?")
			.addScalar("salt", Hibernate.STRING)
			.setInteger(0, u.getUserId())
			.uniqueResult();

		try {
			String hashedPassword = Security.encodeString(pw + saltOnRecord);
			
			if (!passwordOnRecord.equals(hashedPassword)) {
				throw new DAOException("Passwords don't match");
			}
			
			//update the user with the new password
			HibernateUtil.beginTransaction();
			String salt = Security.getRandomToken();
			String newPassword = Security.encodeString(pw2 + salt);
			session.createQuery("update User set password = :pw, salt = :salt where user_id = :userid")
				.setParameter("pw", newPassword)
				.setParameter("salt", salt)
				.setParameter("userid", u.getUserId())
				.executeUpdate();
			HibernateUtil.commitTransaction();
		}
		catch (ContextAuthenticationException e) {
			log.error(e);
			throw new DAOException(e);
		}
	}
	
	public void changeQuestionAnswer(String pw, String question, String answer) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		User u = context.getAuthenticatedUser();
		
		String passwordOnRecord = (String) session.createSQLQuery(
		"select password from users where user_id = ?")
		.addScalar("password", Hibernate.STRING)
		.setInteger(0, u.getUserId())
		.uniqueResult();
		
		String saltOnRecord = (String) session.createSQLQuery(
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
		catch (ContextAuthenticationException e) {
			log.error(e);
			throw new DAOException(e);
		}
		
		HibernateUtil.beginTransaction();
		session.createQuery("update User set secret_question = :q, secret_answer = :a where user_id = :userid")
			.setParameter("q", question)
			.setParameter("a", answer)
			.setParameter("userid", u.getUserId())
			.executeUpdate();
		HibernateUtil.commitTransaction();
	}
}
