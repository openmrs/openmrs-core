package org.openmrs.api.context;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormEntryService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.UserService;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.hibernate.HibernateDAOContext;
import org.openmrs.reporting.ReportService;
import org.openmrs.util.OpenmrsConstants;

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
public class Context {

	private final Log log = LogFactory.getLog(getClass());

	DAOContext daoContext;

	private User user = null;
	private ConceptService conceptService;
	private EncounterService encounterService;
	private ObsService obsService;
	private PatientService patientService;
	private PatientSetService patientSetService;
	private UserService userService;
	private AdministrationService administrationService;
	private FormService formService;
	private OrderService orderService;
	private Locale locale = new Locale("en", "US");
	private ReportService reportService;
	private FormEntryService formEntryService;
	private List<String> proxies = new Vector<String>();

	public Context() {
		
	}
	
	private DAOContext getDAOContext() {
		if (daoContext == null)
			daoContext = new HibernateDAOContext(this);
		return daoContext;
	}
	
	/**
	 * Used to authenticate user within the context
	 * 
	 * @param username user's identifier token for login
	 * @param password user's password for authenticating to context
	 * @throws ContextAuthenticationException
	 */
	public void authenticate(String username, String password)
			throws ContextAuthenticationException {
		getDAOContext().authenticate(username, password);
		user = getDAOContext().getAuthenticatedUser();
	}

	/**
	 * @return concept dictionary-related services
	 */
	public ConceptService getConceptService() {
		if (conceptService == null)
			conceptService = new ConceptService(this, getDAOContext());
		return conceptService;
	}
	
	/**
	 * @return encounter-related services
	 */
	public EncounterService getEncounterService() {
		if (encounterService == null)
			encounterService = new EncounterService(this, getDAOContext());
		return encounterService;
	}
	
	/**
	 * @return observation services
	 */
	public ObsService getObsService() {
		if (obsService == null)
			obsService = new ObsService(this, getDAOContext());
		return obsService;
	}

	/**
	 * @return patient-related services
	 */
	public PatientService getPatientService() {
		if (patientService == null)
			patientService = new PatientService(this, getDAOContext());
		return patientService;		
	}
	
	/**
	 * @return concept dictionary-related services
	 */
	public FormEntryService getFormEntryService() {
		if (formEntryService == null)
			formEntryService = new FormEntryService(this, getDAOContext());
		return formEntryService;
	}
	
	/**
	 * @return patientset-related services
	 */
	public PatientSetService getPatientSetService() {
		if (patientSetService == null) {
			patientSetService = new PatientSetService(this, getDAOContext());
		}
		return patientSetService;
	}

	/**
	 * @return user-related services
	 */
	public UserService getUserService() {
		if (userService == null)
			userService = new UserService(this, getDAOContext());
		return userService;
	}

	/** 
	 * @return order service
	 */
	public OrderService getOrderService() {
		if (orderService == null)
			orderService = new OrderService(this, getDAOContext());
		return orderService;
	}
	
	/** 
	 * @return form service
	 */
	public FormService getFormService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to form service");
			return null;
		}
		if (formService == null)
			formService = new FormService(this, getDAOContext());
		return formService;
	}
	
	/** 
	 * @return report service
	 */
	public ReportService getReportService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to report service");
			return null;
		}
		if (reportService == null)
			reportService = new ReportService(this, getDAOContext());
		return reportService;
	}

	/**
	 * @return admin-related services
	 */
	public AdministrationService getAdministrationService() {
		// TODO Add authentication on a per function level
		if (!isAuthenticated()) {
			log.warn("unauthorized access to administration service");
			return null;
		}
		if (administrationService == null)
			administrationService = new AdministrationService(this, getDAOContext());
		return administrationService;
	}

	/**
	 * @return "active" user who has been authenticated, 
	 *         otherwise <code>null</code> 
	 */
	public User getAuthenticatedUser() {
		user = getDAOContext().getAuthenticatedUser();
		return user;
	}
	
	/**
	 * @return true if user has been authenticated in this context
	 */
	public boolean isAuthenticated() {
		return user != null;
	}

	/**
	 * logs out the "active" (authenticated) user within context 
	 * @see #authenticate
	 */
	public void logout() {
		user = null;
		getDAOContext().logout();
	}

	/**
	 * Tests whether or not currently authenticated user has a 
	 * particular privilege
	 * 
	 * @param privilege
	 * @return true if authenticated user has given privilege
	 */
	public boolean hasPrivilege(String privilege) {
		
		// if a user has logged in, check their privileges
		if (isAuthenticated()) {
			// check user's privileges
			if (user.hasPrivilege(privilege))
				return true;
			
			// check proxied privileges
			for (String s : proxies)
				if (s.equals(privilege))
					return true;
			
			Role auth = getUserService().getRole(OpenmrsConstants.AUTHENTICATED_ROLE);
			for (Privilege p : auth.getPrivileges())
				if (p.getPrivilege().equals(privilege))
					return true;
		}
		else {
			Role role = getUserService().getRole(OpenmrsConstants.ANONYMOUS_ROLE);
			if (role.hasPrivilege(privilege))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Gives the given privilege to all calls to hasPrivilege.  This method was visualized as being
	 * used as follows:
	 * 
	 * <code>
	 * context.addProxyPrivilege("AAA");
	 * context.get*Service().methodRequiringAAAPrivilege();
	 * context.removeProxyPrivilege("AAA");
	 * </code>
	 * 
	 * @param privilege to give to users
	 */
	public void addProxyPrivilege(String privilege) {
		proxies.add(privilege);
	}
	
	/**
	 * Will remove one instance of privilege from the privileges that are currently proxied
	 * @param privilege
	 */
	public void removeProxyPrivilege(String privilege) {
		if (proxies.contains(privilege))
			proxies.remove(privilege);
	}
	
	/**
	 * @param locale new locale for this context
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return current locale for this context
	 */
	public Locale getLocale() {
		return locale;
	}
	
	public void startTransaction() {
		getDAOContext().openSession();
	}
	
	public void endTransaction() {
		getDAOContext().closeSession();
	}
}