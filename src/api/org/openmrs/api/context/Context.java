package org.openmrs.api.context;

import java.util.Locale;

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
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.hibernate.HibernateDAOContext;
import org.openmrs.reporting.ReportService;

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
	private UserService userService;
	private AdministrationService administrationService;
	private FormService formService;
	private OrderService orderService;
	private Locale locale = new Locale("en", "US");
	private ReportService reportService;

	public Context() {
		
	}
	
	public DAOContext getDAOContext() {
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
		if (!isAuthenticated()) {
			log.warn("unauthorized access to concept service");
			return null;
		}
		if (conceptService == null)
			conceptService = new ConceptService(this);
		return conceptService;
	}
	
	/**
	 * @return encounter-related services
	 */
	public EncounterService getEncounterService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to encounter service");
			return null;
		}
		if (encounterService == null)
			encounterService = new EncounterService(this);
		return encounterService;
	}
	
	/**
	 * @return observation services
	 */
	public ObsService getObsService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to obs service");
			return null;
		}
		if (obsService == null)
			obsService = new ObsService(this);
		return obsService;
	}

	/**
	 * @return patient-related services
	 */
	public PatientService getPatientService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to patient service");
			return null;
		}
		if (patientService == null)
			patientService = new PatientService(this);
		return patientService;		
	}

	/**
	 * @return user-related services
	 */
	public UserService getUserService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to user service");
			return null;
		}
		if (userService == null)
			userService = new UserService(this);
		return userService;
	}

	/** 
	 * @return order service
	 */
	public OrderService getOrderService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to order service");
			return null;
		}
		if (orderService == null)
			orderService = new OrderService(this);
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
			formService = new FormService(this);
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
			reportService = new ReportService(this);
		return reportService;
	}

	/**
	 * @return admin-related services
	 */
	public AdministrationService getAdministrationService() {
		if (!isAuthenticated()) {
			log.warn("unauthorized access to administration service");
			return null;
		}
		if (administrationService == null)
			administrationService = new AdministrationService(this);
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
		if (isAuthenticated())
			return user.hasPrivilege(privilege);
		return false;
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
