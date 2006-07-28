package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.UserDAO;
import org.openmrs.util.Helper;
import org.openmrs.util.Security;
import org.springframework.orm.ObjectRetrievalFailureException;

public class HibernateUserDAO implements
		UserDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateUserDAO() { }

	
	public HibernateUserDAO(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.db.UserService#createUser(org.openmrs.User)
	 */
	public void createUser(User user, String password) {
		Session session = HibernateUtil.currentSession();

		if (hasDuplicateUsername(user))
			throw new DAOException("Username currently in use by '" + user.getFirstName() + " " + user.getLastName() + "'");
		
		try {
			//add all data minus the password as a new user
			HibernateUtil.beginTransaction();
			
			String systemId = generateSystemId();
			Integer checkDigit = Helper.getCheckDigit(systemId);
			user.setSystemId(systemId + "-" + checkDigit);
			
			user = updateProperties(user);
			
			session.save(user);
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
			throw new DAOException(e);
		}	
	}

	/**
	 * @see org.openmrs.api.db.UserService#getUserByUsername(java.lang.String)
	 */
	public User getUserByUsername(String username) {
		Session session = HibernateUtil.currentSession();

		List<User> users = session
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
	public boolean hasDuplicateUsername(User user) {
		Session session = HibernateUtil.currentSession();

		String username = user.getUsername();
		if (username == null || username.length() == 0)
			username = "-";
		String systemId = user.getSystemId();
		if (systemId == null || username.length() == 0)
			systemId = "-";
		
		Integer userid = user.getUserId();
		if (userid == null)
			userid = new Integer(-1);
		
		String usernameWithCheckDigit = username;
		try {
			Integer cd = Helper.getCheckDigit(username);
			usernameWithCheckDigit = usernameWithCheckDigit + "-" + cd;
		}
		catch (Exception e) {}
		
		Integer count = (Integer) session.createQuery(
				"select count(*) from User u where (u.username = :uname1 or u.systemId = :uname2 or u.username = :sysid1 or u.systemId = :sysid2 or u.systemId = :uname3) and u.userId <> :uid")
				.setString("uname1", username)
				.setString("uname2", username)
				.setString("sysid1", systemId)
				.setString("sysid2", systemId)
				.setString("uname3", usernameWithCheckDigit)
				.setInteger("uid", userid)
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
		Session session = HibernateUtil.currentSession();
		User user = (User) session.get(User.class, userId);
		
		if (user == null) {
			log.warn("request or user '" + userId + "' not found");
			throw new ObjectRetrievalFailureException(User.class, userId);
		}
		return user;
	}
	
	/**
	 * @see org.openmrs.api.db.UserService#getUsers()
	 */
	public List<User> getUsers() throws DAOException {
		Session session = HibernateUtil.currentSession();
		List<User> users = session.createQuery("from User u order by u.userId")
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
				user = updateProperties(user);
				try {
					session.update(user);
				}
				catch (NonUniqueObjectException e) {
					session.merge(user);
				}
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
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
			throw new DAOException(e);
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
	 * @see org.openmrs.api.db.UserService#getInheritingRoles(org.openmrs.Role)
	 */
	public List<Role> getInheritingRoles(Role role) throws DAOException {

		Session session = HibernateUtil.currentSession();
		
		/*
		Criteria crit = session.createCriteria(Role.class, "r")
			.add(Expression.in("r.inheritedRoles", role))
			.addOrder(Order.asc("r.role"));
		return crit.list();
		*/
		
		List<Role> roles = session.createQuery("from Role r where :role in inheritedRoles order by r.role")
				.setParameter("role", role)
				.list();
		
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
	 * @see org.openmrs.api.db.UserDAO#changePassword(org.openmrs.User, java.lang.String)
	 */
	public void changePassword(User u, String pw) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		User authUser = context.getAuthenticatedUser();
		
		if (authUser == null)
			authUser = u;
		
		try {
			log.debug("udpating password");
			//update the user with the new password
			HibernateUtil.beginTransaction();
			String salt = Security.getRandomToken();
			String newPassword = Security.encodeString(pw + salt);
			session.createQuery("update User set password = :pw, salt = :salt, changed_by = :changed, date_changed = :date where user_id = :userid")
				.setParameter("pw", newPassword)
				.setParameter("salt", salt)
				.setParameter("userid", u.getUserId())
				.setParameter("changed", authUser.getUserId())
				.setParameter("date", new Date())
				.executeUpdate();
			HibernateUtil.commitTransaction();
		}
		catch (APIException e) {
			log.error(e);
			throw new DAOException(e);
		}
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
				log.error("Passwords don't match");
				throw new DAOException("Passwords don't match");
			}
			
			log.debug("udpating password");
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
		catch (APIException e) {
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
		catch (APIException e) {
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
	
	public boolean isSecretAnswer(User u, String answer) throws DAOException {
		
		if (answer == null || answer.equals(""))
			return false;
		
		Session session = HibernateUtil.currentSession();
		String answerOnRecord = "";
		
		try {
			answerOnRecord = (String) session.createSQLQuery(
			"select secret_answer from users where user_id = ?")
			.addScalar("secret_answer", Hibernate.STRING)
			.setInteger(0, u.getUserId())
			.uniqueResult();
		}
		catch (Exception e) {
			return false;
		}
		
		return (answerOnRecord.equals(answer));
		
		
	}
	
	public List<User> findUsers(String name, List<String> roles, boolean includeVoided) {
		
		Session session = HibernateUtil.currentSession();
		
		List<User> users = new Vector<User>();
		
		name = name.replace(", ", " ");
		String[] names = name.split(" ");
		
		log.debug("name: " + name);
		
		Criteria criteria = session.createCriteria(User.class);
		//criteria.setProjection(Projections.distinct(Projections.property("user")));
		for (String n : names) {
			if (n != null && n.length() > 0) {
				criteria.add(Expression.or(
						Expression.like("lastName", n, MatchMode.START),
						Expression.or(
							Expression.like("firstName", n, MatchMode.START),
								Expression.or(
										Expression.like("middleName", n, MatchMode.START),
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

		// TODO figure out how to get Hibernate to do the sql for us
		
		List returnList = new Vector();
		if (roles != null && roles.size() > 0) {
			for (Object o : criteria.list()) {
				User u = (User)o;
				for (String r : roles)
					if (u.hasRole(r, true)) {
						returnList.add(u);
						break;
					}
			}
		}
		else
			returnList = criteria.list();
		
		return returnList;
	}
	
	public List<User> getAllUsers(List<String> roles, boolean includeVoided) {
		
		Session session = HibernateUtil.currentSession();
		
		List<User> users = new Vector<User>();
		
		Criteria criteria = session.createCriteria(User.class);
		
		if (includeVoided == false)
			criteria.add(Expression.eq("voided", false));

		List returnList = new Vector();
		if (roles != null && roles.size() > 0) {
			for (Object o : criteria.list()) {
				User u = (User)o;
				for (String r : roles)
					if (u.hasRole(r, true)) {
						returnList.add(u);
						break;
					}
			}
		}
		else
			returnList = criteria.list();
		
		return returnList;
		
		// TODO figure out how to get Hibernate to do the sql for us
		
		/*
		String sql = "select user from User as user";
		String order = " order by u.userId asc";
		
		Query query;
			
		if (roles != null && roles.size() > 0) {
			sql += ", Role r, Group g";
			sql += " where (r.userId = user.userId and r.role in (";
			for (int i=0; i<roles.size(); i++) {
				sql += "'" + roles.get(i) + "'";
				if (i != roles.size() -1)
					sql += ",";
			}
			sql += "))";
			
			sql += " or ( g.userId = user.userId and g.role in (";
			for (int i=0; i<roles.size(); i++) {
				sql += "'" + roles.get(i) + "'";
				if (i != roles.size() -1)
					sql += ",";
			}
			sql += "))";

			if (includeVoided == false) {
				sql += " and user.voided = 0";
			}
			
		}
		else {
			if (includeVoided == false) {
				sql += " user.voided = 0";
			}
		}
		
		return session.createQuery(sql).list();
		*/
	}
	
	/**
	 * Get/generate/find the next system id to be doled out.  Assume check digit /not/ applied
	 * in this method
	 * @return new system id
	 */
	public String generateSystemId() {
		Session session = HibernateUtil.currentSession();
		
		String sql = "select max(user_id) as user_id from users";
		
		Query query = session.createSQLQuery(sql);
		
		Integer id = ((Integer)query.uniqueResult()).intValue() + 1;
		
		return id.toString();
	}

	private User updateProperties(User user) {
		
		if (user.getCreator() == null) {
			user.setDateCreated(new Date());
			user.setCreator(context.getAuthenticatedUser());
		}
		
		user.setChangedBy(context.getAuthenticatedUser());
		user.setDateChanged(new Date());
		
		return user;
		
	}
}
