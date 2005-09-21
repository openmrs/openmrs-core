package org.openmrs.context;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.hibernate.HibernatePatientService;
import org.openmrs.api.hibernate.HibernateUserService;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernateContext extends HibernateDaoSupport implements Context {

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

		List users = getHibernateTemplate().find(
				"from User as u where u.username = ?", username);

//		try {
//			MessageDigest md = MessageDigest.getInstance("MD5");
//			byte[] input = password.getBytes();
//			String hashedPassword = hexString(md.digest(input));
//
//			// If we get anything other than one result, then fail
//			if (users != null && users.size() == 1) {
//				User candidateUser = (User) users.get(0);
//				if (hashedPassword != null
//						&& hashedPassword.equals(candidateUser.getPassword()))
//					user = candidateUser;
//			}
//
//		} catch (NoSuchAlgorithmException e) {
//			// Yikes! Can't encode password...what to do?
//			errorMsg = "system cannot find password encryption algorithm";
//		}

		if (user == null) {
			log
					.info("Failed login (username=\"" + username + ") - "
							+ errorMsg);
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
	 * @see org.openmrs.context.Context#getAuthenticatedUser()
	 */
	public User getAuthenticatedUser() {
		return user;
	}

	/**
	 * Log the current user out of this context.  isAuthenticated will now return false.
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
	public Set getPrivileges() {
		if (!isAuthenticated())
			return null;
		
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Return whether or not the user has the given priviledge
	 * 
	 * @param String privilege to authorize against
	 * @return boolean whether the user has the given privilege
	 * @see org.openmrs.context.Context#hasPrivilege(java.lang.String)
	 */
	public boolean hasPrivilege(String privilege) {
		if (isAuthenticated()) {
			User user = getAuthenticatedUser();
			System.out.println(user.getClass().getName());
			getSession().merge(user);
			getHibernateTemplate().initialize(user.getRoles());
			boolean blah = user.hasPrivilege(privilege);
			return blah;
		}
		return false;
	}

	/**
	 * Determine if a user is authenticated already
	 * 
	 * @see org.openmrs.context.Context#isAuthenticated()
	 */
	public boolean isAuthenticated() {
		// TODO Auto-generated method stub
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
//		if (conceptService == null)
//			conceptService = new HibernateConceptService();
		return conceptService;
	}
	
	public EncounterService getEncounterService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to encounter service");
			return null;
		}
//		if (encounterService == null)
//			encounterService = new HibernateEncounterService();
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
//		if (obsService == null)
//			obsService = new HibernateObsService();
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
//		if (patientService == null)
//			patientService = new HibernatePatientService();
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
		}
//		if (userService == null)
//			userService = new HibernateUserService();
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
//		if (administrationService == null)
//			administrationService = new HibernateAdministrationService();
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
//		if (formService == null)
//			formService = new HibernateFormService();
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
//		if (orderService == null)
//			orderService = new HibernateOrderService();
		return orderService;
	}

	/**
	 * Get the locale
	 * 
	 * @see org.openmrs.context.Context#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Set the locale
	 * @see org.openmrs.context.Context#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale) {
		// TODO Auto-generated method stub

	}

}
