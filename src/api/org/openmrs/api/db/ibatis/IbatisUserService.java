package org.openmrs.api.db.ibatis;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.db.APIException;
import org.openmrs.api.db.UserService;
import org.openmrs.api.context.Context;

/**
 * Ibatis-specific implementation of org.openmrs.api.db.UserService
 * 
 * @see org.openmrs.api.db.UserService
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class IbatisUserService implements UserService {

	private final Log log = LogFactory.getLog(getClass());

	private Context context;

	/**
	 * Service must be constructed within a <code>context</code>
	 * @param context
	 * @see org.openmrs.api.context.Context
	 */
	public IbatisUserService(Context context) {
		this.context = context;
	}

	/**
	 * @see org.openmrs.api.db.UserService#createUser(User)
	 */
	public User createUser(User user) throws APIException {
		
		user.setCreator(context.getAuthenticatedUser());
		
		try {
			SqlMap.instance().insert("createUser", user);
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return user;
	}

	/**
	 * @see org.openmrs.api.db.UserService#getUser(Integer)
	 */
	public User getUser(Integer userId) throws APIException {
		User user;
		try {
			user = (User) SqlMap.instance().queryForObject("getUser", userId);
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return user;
	}

	/**
	 * @see org.openmrs.api.db.UserService#getUserByUsername(String)
	 */
	public User getUserByUsername(String username) throws APIException {
		User user = null;
		try {
			List users = SqlMap.instance().queryForList("getUserByUsername",
					username);
			if (users != null) {
				if (users.size() < 1) {
					user = null;
				} else {
					if (users.size() != 1) {
						log.error("search for username '" + username + "' yielded "
								+ users.size() + " results!");
					}
					user = (User) users.get(0);
				}
			}
		} catch (SQLException e) {
			log.error("error fetching user by username '" + username + "'", e);
			throw new APIException(e);
		}
		return user;
	}

	/**
	 * @see org.openmrs.api.db.UserService#updateUser(User)
	 */
	public void updateUser(User user) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();

				User authenticatedUser = context.getAuthenticatedUser();
				List oldRoles;

				if (user.getCreator() == null) {
					user.setCreator(authenticatedUser);
					SqlMap.instance().insert("createUser", user);
					oldRoles = new LinkedList();
				} else {
					user.setChangedBy(authenticatedUser);
					SqlMap.instance().update("updateUser", user);
					oldRoles = SqlMap.instance().queryForList("getRoleByUser",
							user.getUserId());
				}

				List newRoles = user.getRoles();
				Map<String, Object> args = new Hashtable<String, Object>();
				args.put("userId", user.getUserId());

				// add any new roles
				for (Iterator i = newRoles.iterator(); i.hasNext();) {
					Role role = (Role) i.next();
					if (!oldRoles.contains(role)) {
						args.put("role", role.getRole());
						SqlMap.instance().insert("createUserRole", args);
					}
				}

				// remove old roles
				for (Iterator i = oldRoles.iterator(); i.hasNext();) {
					Role role = (Role) i.next();
					if (!newRoles.contains(role)) {
						args.put("role", role.getRole());
						SqlMap.instance().delete("deleteUserRole", args);
					}
				}

				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}

	}
	
	/**
	 * @see org.openmrs.api.db.UserService#grantUserRole(User, Role)
	 */
	public void grantUserRole(User user, Role role) throws APIException {
		try {
			Map<String, Object> args = new Hashtable<String, Object>();
			args.put("userId", user.getUserId());
			args.put("role", role.getRole());
			SqlMap.instance().insert("grantUserRole", args);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.UserService#revokeUserRole(User, Role)
	 */
	public void revokeUserRole(User user, Role role) throws APIException {
		try {
			Map<String, Object> args = new Hashtable<String, Object>();
			args.put("userId", user.getUserId());
			args.put("role", role.getRole());
			SqlMap.instance().delete("revokeUserRole", args);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.UserService#voidUser(User, String)
	 */
	public void voidUser(User user, String reason) throws APIException {
		user.setVoided(true);
		user.setVoidReason(reason);
		user.setVoidedBy(context.getAuthenticatedUser());
		// user.setVoidReason(reason);
		try {
			SqlMap.instance().update("voidUser", user);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.UserService#unvoidUser(User)
	 */
	public void unvoidUser(User user) {
		user.setVoided(false);
		user.setChangedBy(context.getAuthenticatedUser());
		try {
			SqlMap.instance().update("unvoidUser", user);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.UserService#deleteUser(User)
	 */
	public void deleteUser(User user) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				List roles = user.getRoles();
				for (Iterator i = roles.iterator(); i.hasNext(); ) {
					Role r = (Role)i.next();
					revokeUserRole(user, r);
				}
				SqlMap.instance().delete("deleteUser", user);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

}
