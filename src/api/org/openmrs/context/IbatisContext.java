package org.openmrs.context;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.ibatis.IbatisPatientService;
import org.openmrs.api.ibatis.IbatisUserService;
import org.openmrs.api.ibatis.SqlMap;

public class IbatisContext implements Context {

	private final Log log = LogFactory.getLog(getClass());

	private User user;
	private Locale locale;
	private ConceptService conceptService;
	private EncounterService encounterService;
	private ObsService obsService;
	private PatientService patientService;
	private UserService userService;

	/** @see org.openmrs.context.Context#authenticate(String, String) */
	public void authenticate(String username, String password)
			throws ContextAuthenticationException {
		User authenticatingUser = getUserService().getUserByUsername(username);
		if (authenticatingUser == null)
			throw new ContextAuthenticationException("user '" + username + "' not found");
		String passwordOnRecord;
		try {
			passwordOnRecord = (String) SqlMap.instance().queryForObject(
					"getUserPassword", authenticatingUser.getUserId());
		} catch (SQLException e) {
			log.error("failed to retrieve password for user "
					+ authenticatingUser.getUserId(), e);
			throw new ContextAuthenticationException(
					"error retrieving credentials for user");
		}

		String errorMsg = "invalid credentials";
		try {
			/* TODO - select encryption algorithm from configuration file
			 * instead of hardcoding here */ 
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] input = password.getBytes();
			String hashedPassword = hexString(md.digest(input));
			
			if (hashedPassword.equals(passwordOnRecord)) {
				user = authenticatingUser;
			}

		} catch (NoSuchAlgorithmException e) {
			// Yikes! Can't encode password...what to do?
			errorMsg = "system cannot find password encryption algorithm";
		}

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

	/** @see org.openmrs.context.Context#getAuthenticatedUser() */
	public User getAuthenticatedUser() {
		return user;
	}


	/** @see org.openmrs.context.Context#getLocale() */
	public Locale getLocale() {
		return locale;
	}

	/** @see org.openmrs.context.Context#getConceptService() */
	public ConceptService getConceptService() {
		return conceptService;
	}
	
	/** @see org.openmrs.context.Context#getEncounterService() */
	public EncounterService getEncounterService() {
		return encounterService;
	}

	/** @see org.openmrs.context.Context#getObsService() */
	public ObsService getObsService() {
		return obsService;
	}

	/** @see org.openmrs.context.Context#getPatientService() */
	public PatientService getPatientService() {
		if (patientService == null)
			patientService = new IbatisPatientService(this);
		return patientService;
	}

	/** @see org.openmrs.context.Context#getUserService() */
	public UserService getUserService() {
		if (userService == null)
			userService = new IbatisUserService(this);
		return userService;
	}

	/** @see org.openmrs.context.Context#hasPrivilege(String) */
	public boolean hasPrivilege(String privilege) {
		if (isAuthenticated())
			return getAuthenticatedUser().hasPrivilege(privilege);
		return false;
	}

	/** @see org.openmrs.context.Context#isAuthenticated() */
	public boolean isAuthenticated() {
		return (user != null);
	}

	/** @see org.openmrs.context.Context#logout() */
	public void logout() {
		user = null;
	}

	/** @see org.openmrs.context.Context#setLocale(Locale) */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

}
