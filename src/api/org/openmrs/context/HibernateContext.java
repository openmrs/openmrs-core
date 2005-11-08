package org.openmrs.context;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.hibernate.HibernateAdministrationService;
import org.openmrs.api.hibernate.HibernateConceptService;
import org.openmrs.api.hibernate.HibernateEncounterService;
import org.openmrs.api.hibernate.HibernateFormService;
import org.openmrs.api.hibernate.HibernateObsService;
import org.openmrs.api.hibernate.HibernateOrderService;
import org.openmrs.api.hibernate.HibernatePatientService;
import org.openmrs.api.hibernate.HibernateUserService;
import org.openmrs.api.hibernate.HibernateUtil;

public class HibernateContext implements Context {

	private final Log log = LogFactory.getLog(getClass());

	private User user = null;
	private ConceptService conceptService;
	private EncounterService encounterService;
	private ObsService obsService;
	private PatientService patientService;
	private UserService userService;
	private AdministrationService administrationService;
	private FormService formService;
	private OrderService orderService;
	private Locale locale;

	protected HibernateContext() {
	}

	/**
	 * Authenticate the user for this context.
	 * 
	 * @param username
	 * @param password
	 * 
	 * @see org.openmrs.context.Context#authenticate(String, String)
	 * @throws ContextAuthenticationException
	 */
	public void authenticate(String username, String password)
			throws ContextAuthenticationException {

		user = null;
		String errorMsg = "Invalid username and/or password";

		//Session session = getSession();
		Session session = HibernateUtil.currentSession();
		
		User candidateUser = (User) session.createQuery(
				"from User u where u.username = ? and u.voided is null or u.voided = 0").setString(0, username)
				.uniqueResult();
		
		if (candidateUser == null) {
			throw new ContextAuthenticationException("user not found: "
					+ username);
		}

		String passwordOnRecord = (String) session.createSQLQuery(
				"select password from users where user_id = ?").addScalar(
				"password", Hibernate.STRING).setInteger(0,
				candidateUser.getUserId()).uniqueResult();

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] input = password.getBytes();
			String hashedPassword = hexString(md.digest(input));

			if (hashedPassword != null
					&& hashedPassword.equals(passwordOnRecord))
				user = candidateUser;

		} catch (NoSuchAlgorithmException e) {
			// Yikes! Can't encode password...what to do?
			errorMsg = "system cannot find password encryption algorithm";
		}
		
		if (user == null) {
			log.info("Failed login (username=\"" + username + ") - " + errorMsg);
			throw new ContextAuthenticationException(errorMsg);
		}
	}

	private String hexString(byte[] b) {
		if (b == null || b.length < 1)
			return "";
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			s.append(Integer.toHexString(b[i] & 0xFF));
		}
		return new String(s);
	}

	/**
	 * Get the currently authenticated user
	 * 
	 * @see org.openmrs.context.Context#getAuthenticatedUser()
	 */
	public User getAuthenticatedUser() {
		Session session = HibernateUtil.currentSession();
		try {
			if (user != null)
				session.merge(user);
		}
		catch (Exception e) {
			log.error("Possible attempted locking of user to double open session aka: " + e.getMessage());
		}
		//session.merge(user);
		return user;
	}

	/**
	 * Log the current user out of this context. isAuthenticated will now return
	 * false.
	 * 
	 * @see org.openmrs.context.Context#logout()
	 */
	public void logout() {
		user = null;
	}

	/**
	 * Get the privileges for the authenticated user
	 * 
	 * @see org.openmrs.context.Context#getPrivileges()
	 */
	public Set<Privilege> getPrivileges() {
		if (!isAuthenticated())
			return null;

		Session session = HibernateUtil.currentSession();
		session.merge(user);
		Set<Privilege> privileges = new HashSet<Privilege>();
		for (Iterator<Role> i = user.getRoles().iterator(); i.hasNext();) {
			Role role = i.next();
			privileges.addAll(role.getPrivileges());
		}
		return privileges;
	}

	/**
	 * Return whether or not the user has the given priviledge
	 * 
	 * @param String
	 *            privilege to authorize against
	 * @return boolean whether the user has the given privilege
	 * @see org.openmrs.context.Context#hasPrivilege(java.lang.String)
	 */
	public boolean hasPrivilege(String privilege) {
		if (isAuthenticated()) {
			User user = getAuthenticatedUser();
			return user.hasPrivilege(privilege);
		}
		return false;
	}

	/**
	 * Determine if a user is authenticated already
	 * 
	 * @see org.openmrs.context.Context#isAuthenticated()
	 */
	public boolean isAuthenticated() {
		return (user != null);
	}

	/**
	 * Get the Hibernate implementation of ConceptService
	 * 
	 * @see org.openmrs.context.Context#getConceptService()
	 */
	public ConceptService getConceptService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to concept service");
			return null;
		}
		if (conceptService == null)
			conceptService = new HibernateConceptService(this);
		return conceptService;
	}

	public EncounterService getEncounterService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to encounter service");
			return null;
		}
		if (encounterService == null)
			encounterService = new HibernateEncounterService(this);
		return encounterService;
	}

	/**
	 * Get Hibernate implementation of ObsService
	 * 
	 * @see org.openmrs.context.Context#getObsService()
	 */
	public ObsService getObsService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized request for obs service");
			return null;
		}
		if (obsService == null)
			obsService = new HibernateObsService(this);
		return obsService;
	}

	/**
	 * Get Hibernate implementation of PatientService
	 * 
	 * @see org.openmrs.context.Context#getPatientService()
	 */
	public PatientService getPatientService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized request for patient service");
			return null;
		}
		if (patientService == null)
			patientService = new HibernatePatientService(this);
		return patientService;
	}

	/**
	 * Get Hibernate implementation of UserService
	 * 
	 * @see org.openmrs.context.Context#getUserService()
	 */
	public UserService getUserService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to user service");
			return null;
			// TODO return null or throw exception?
			//throw new APIException("Unauthorized Access to UserService");
		}
		if (userService == null)
			userService = new HibernateUserService(this);
		return userService;
	}

	/**
	 * @return Returns the administrationService.
	 */
	public AdministrationService getAdministrationService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to administration service");
			return null;
		}
		if (administrationService == null)
			administrationService = new HibernateAdministrationService(this);
		return administrationService;
	}

	/**
	 * @return Returns the formService.
	 */
	public FormService getFormService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to form service");
			return null;
		}
		if (formService == null)
			formService = new HibernateFormService(this);
		return formService;
	}

	/**
	 * @return Returns the orderService.
	 */
	public OrderService getOrderService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to order service");
			return null;
		}
		if (orderService == null)
			orderService = new HibernateOrderService(this);
		return orderService;
	}

	/**
	 * Get the locale
	 * 
	 * @see org.openmrs.context.Context#getLocale()
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Set the locale
	 * 
	 * @see org.openmrs.context.Context#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @see org.openmrs.context.Context#beginTransaction()
	 */
	public void startTransaction() {

		log.debug("HibernateContext: Starting Transaction");
		//if (session == null)
		HibernateUtil.currentSession();
		
	}

	/**
	 * @see org.openmrs.context.Context#endTransaction()
	 */
	public void endTransaction() {
		
		log.debug("HibernateContext: Ending Transaction");
		/*TODO	tomcat loops adinfinitum at this point after several 
		 		redeploys (during development).
		 		Update #1: threadlocal incorrectly configured?
		 		Update #2: or it seems to be an issue with connections being left around |fixed|
		 		Update #3: Memory leak ?
		*/  
		HibernateUtil.closeSession();
		//session = null;
		
	}
	
}
