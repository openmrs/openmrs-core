package org.openmrs.api.db;

import org.openmrs.User;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.formentry.db.FormEntryDAO;
import org.openmrs.hl7.db.HL7DAO;
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
	public void setAdministrationDAO(AdministrationDAO dao);
	
	public ConceptDAO getConceptDAO();
	public void setConceptDAO(ConceptDAO dao);
	
	public EncounterDAO getEncounterDAO();
	public void setEncounterDAO(EncounterDAO dao);
	
	public FormDAO getFormDAO();
	public void setFormDAO(FormDAO dao);
	
	public ObsDAO getObsDAO();
	public void setObsDAO(ObsDAO dao);
	
	public OrderDAO getOrderDAO();
	public void setOrderDAO(OrderDAO dao);
	
	public PatientDAO getPatientDAO();
	public void setPatientDAO(PatientDAO dao);
	
	public PatientSetDAO getPatientSetDAO();
	public void setPatientSetDAO(PatientSetDAO dao);
	
	public UserDAO getUserDAO();
	public void setUserDAO(UserDAO dao);
	
	public FormEntryDAO getFormEntryDAO();
	
	public HL7DAO getHL7DAO();
	
	public ReportDAO getReportDAO();
	public void setReportDAO(ReportDAO dao);
	
	public TemplateDAO getTemplateDAO();
	public void setTemplateDAO(TemplateDAO dao);
	
	public NoteDAO getNoteDAO();
	
	public void openSession();
	public void closeSession();
	
}
