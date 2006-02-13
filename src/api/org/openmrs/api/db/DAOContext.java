package org.openmrs.api.db;

import org.openmrs.User;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.form.db.FormEntryDAO;
import org.openmrs.reporting.db.ReportDAO;

/**
 * Defines functions needed to access the database layer.
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public interface DAOContext {
	
	public User authenticate(String username, String password)
		throws ContextAuthenticationException;

	/**
	 * @return "active" user who has been authenticated, 
	 *         otherwise <code>null</code> 
	 */
	public User getAuthenticatedUser();
	
	public void logout();

	public AdministrationDAO getAdministrationDAO();
	
	public ConceptDAO getConceptDAO();
	
	public EncounterDAO getEncounterDAO();
	
	public FormDAO getFormDAO();
	
	public ObsDAO getObsDAO();
	
	public OrderDAO getOrderDAO();
	
	public PatientDAO getPatientDAO();
	
	public PatientSetDAO getPatientSetDAO();
	
	public UserDAO getUserDAO();
	
	public FormEntryDAO getFormEntryDAO();
	
	public ReportDAO getReportDAO();
	
	public void openSession();
	
	public void closeSession();
	
}
