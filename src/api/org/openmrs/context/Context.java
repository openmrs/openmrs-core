package org.openmrs.context;

import java.util.Locale;

import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;

/**
 * Represents an OpenMRS <code>Context</code>, which may be used to
 * authenticate to the database and obtain services in order to 
 * interact with the system.
 * 
 * Only one <code>User</code> may be authenticated within a context
 * at any given time. 
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public interface Context {
	
	/**
	 * Used to authenticate user within the context
	 * 
	 * @param username user's identifier token for login
	 * @param password user's password for authenticating to context
	 * @throws ContextAuthenticationException
	 */
	public void authenticate(String username, String password)
			throws ContextAuthenticationException;

	/**
	 * @return concept dictionary-related services
	 */
	public ConceptService getConceptService();
	
	/**
	 * @return encounter-related services
	 */
	public EncounterService getEncounterService();
	
	/**
	 * @return observation services
	 */
	public ObsService getObsService();

	/**
	 * @return patient-related services
	 */
	public PatientService getPatientService();

	/**
	 * @return user-related services
	 */
	public UserService getUserService();

	/**
	 * @return admin-related services
	 */
	public AdministrationService getAdministrationService();

	/**
	 * @return "active" user who has been authenticated, 
	 *         otherwise <code>null</code> 
	 */
	public User getAuthenticatedUser();
	
	/**
	 * @return true if user has been authenticated in this context
	 */
	public boolean isAuthenticated();

	/**
	 * logs out the "active" (authenticated) user within context 
	 * @see #authenticate
	 */
	public void logout();

	/**
	 * Tests whether or not currently authenticated user has a 
	 * particular privilege
	 * 
	 * @param privilege
	 * @return true if authenticated user has given privilege
	 */
	public boolean hasPrivilege(String privilege);

	/**
	 * @param locale new locale for this context
	 */
	public void setLocale(Locale locale);

	/**
	 * @return current locale for this context
	 */
	public Locale getLocale();

}
