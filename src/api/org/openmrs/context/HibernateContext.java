package org.openmrs.context;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernateContext extends HibernateDaoSupport implements Context {

	private final Log log = LogFactory.getLog(getClass());

	private User user = null;
	private ConceptService conceptService;
	private EncounterService encounterService;
	private ObsService obsService;
	private PatientService patientService;
	private UserService userService;

	protected HibernateContext() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#authenticate(java.lang.String,
	 *      java.lang.String)
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

	public User getAuthenticatedUser() {
		return user;
	}

	public void logout() {
		user = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#getPrivileges()
	 */
	public Set getPrivileges() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#isAuthenticated()
	 */
	public boolean isAuthenticated() {
		// TODO Auto-generated method stub
		return (user != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#getConceptService()
	 */
	public ConceptService getConceptService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to concept service");
			return null;
		}
		return conceptService;
	}
	
	public EncounterService getEncounterService() {
		return encounterService;
	}

	protected void setConceptService(ConceptService conceptService) {
		this.conceptService = conceptService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setLocale(Locale locale) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#getObsService()
	 */
	public ObsService getObsService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized request for obs service");
			return null;
		}
		return obsService;
	}

	protected void setObsService(ObsService obsService) {
		this.obsService = obsService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#getPatientService()
	 */
	public PatientService getPatientService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized request for patient service");
			return null;
		}
		return patientService;
	}

	protected void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#getUserService()
	 */
	public UserService getUserService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to user service");
			return null;
		}
		return userService;
	}

	protected void setUserService(UserService userService) {
		this.userService = userService;
	}

}
