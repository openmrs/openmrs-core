package org.openmrs.context;

import java.util.Locale;
import java.util.Set;

import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;

public class JDBCContext implements Context {

	User user = null;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#authenticate(java.lang.String,
	 *      java.lang.String)
	 */
	public void authenticate(String user, String password) {
		// TODO Auto-generated method stub

	}
	
	
	/* (non-Javadoc)
	 * @see org.openmrs.context.Context#getAuthenticatedUser()
	 */
	public User getAuthenticatedUser() {
		// TODO Auto-generated method stub
		return null;
	}


	public void logout() {
		user = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#getConceptService()
	 */
	public ConceptService getConceptService() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#isAuthenticated()
	 */
	public boolean isAuthenticated() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#getObsService()
	 */
	public ObsService getObsService() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#getPatientService()
	 */
	public PatientService getPatientService() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#getUserService()
	 */
	public UserService getUserService() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.context.Context#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale) {
		// TODO Auto-generated method stub

	}
	
	public EncounterService getEncounterService() {
		return null;
	}

}
