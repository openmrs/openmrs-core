package org.openmrs.api.db;

import org.openmrs.User;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.reporting.db.ReportDAO;

/**
 * Defines functions needed to access the database layer.
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public interface DAOContext {
	
	public void authenticate(String username, String password)
		throws ContextAuthenticationException;

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

	public AdministrationDAO getAdministrationDAO();
	
	public ConceptDAO getConceptDAO();
	
	public EncounterDAO getEncounterDAO();
	
	public FormDAO getFormDAO();
	
	public ObsDAO getObsDAO();
	
	public OrderDAO getOrderDAO();
	
	public PatientDAO getPatientDAO();
	
	public UserDAO getUserDAO();
	
	public ReportDAO getReportDAO();
	
	public void openSession();
	
	public void closeSession();
	
}
