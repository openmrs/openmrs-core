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
import org.openmrs.api.FormService;
import org.openmrs.api.MessageService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.UserService;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.hibernate.HibernateDAOContext;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.reporting.ReportService;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
public class Context implements ApplicationContextAware {

	private final Log log = LogFactory.getLog(getClass());

	DAOContext daoContext;
	private User user = null;
	private static MessageService messageService;
	private static ApplicationContext applicationContext;
	
	// Services
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
	

	/**
	 *  Default public constructor
	 *
	 */
	public Context() { }

	
	/**
	 *  Set application context
	 */
	public void setApplicationContext(ApplicationContext context) { 
		this.applicationContext = context;
	}

	
	/**
	 * Gets the DAO context.  
	 * 
	 * NOTE:  Instantiates a new DAO context if one does not already exist.  This means that a new DAO
	 * context is created for every context.  If we have 100 users currently logged in, that means there
	 * will be 100 DAO context instances in memory.  
	 * 
	 * We should be using dependency injection.  There is a context instance for each new http session
	 * that is created, so this becomes a bit more difficult.  There are two separate paradigms in play here:
	 * (1) user-specific context (2) service locator.  One is a user specific object, the other is 
	 * an app-specific object.  We should have define these more clearly and keep the two distinct to 
	 * avoid issues like this.   By the way, I changed the method to use camel-case for readability
	 * if/when we use dependency injection in the future.
	 * 
	 * TODO:  Refactor into separate classes (user context vs. service locator) or refactor into user 
	 * context (ONLY) and use dependency injection in Controller (and other client) classes to access
	 * services layer.  
	 * 
	 * TODO:  Remove dependency of context within services layer.  
	 * 
	 * @return
	 */
	public DAOContext getDaoContext() { 
		if (daoContext == null)
			daoContext = new HibernateDAOContext(this);
		return daoContext;
	}
	
	/**
	 * Used to set the DAO context for the application.
	 * 
	 * @param daoContext
	 */
	public void setDaoContext(DAOContext daoContext) { 
		this.daoContext = daoContext;
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
		getDaoContext().authenticate(username, password);
		user = getDaoContext().getAuthenticatedUser();
	}

	/**
	 * @return concept dictionary-related services
	 */
	public ConceptService getConceptService() {
		if (conceptService == null)
			conceptService = new ConceptService(this, getDaoContext());
		return conceptService;
	}
	
	/**
	 * @return encounter-related services
	 */
	public EncounterService getEncounterService() {
		if (encounterService == null)
			encounterService = new EncounterService(this, getDaoContext());
		return encounterService;
	}
	
	/**
	 * @return observation services
	 */
	public ObsService getObsService() {
		if (obsService == null)
			obsService = new ObsService(this, getDaoContext());
		return obsService;
	}

	/**
	 * @return patient-related services
	 */
	public PatientService getPatientService() {
		if (patientService == null)
			patientService = new PatientService(this, getDaoContext());
		return patientService;		
	}
	
	/**
	 * @return concept dictionary-related services
	 */
	public FormEntryService getFormEntryService() {
		if (formEntryService == null)
			formEntryService = new FormEntryService(this, getDaoContext());
		return formEntryService;
	}
	
	/**
	 * @return patientset-related services
	 */
	public PatientSetService getPatientSetService() {
		if (patientSetService == null) {
			patientSetService = new PatientSetService(this, getDaoContext());
		}
		return patientSetService;
	}

	/**
	 * @return user-related services
	 */
	public UserService getUserService() {
		if (userService == null)
			userService = new UserService(this, getDaoContext());
		return userService;
	}

	/** 
	 * @return order service
	 */
	public OrderService getOrderService() {
		if (orderService == null)
			orderService = new OrderService(this, getDaoContext());
		return orderService;
	}
	
	/** 
	 * @return form service
	 */
	public FormService getFormService() {
		if (formService == null)
			formService = new FormService(this, getDaoContext());
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
			reportService = new ReportService(this, getDaoContext());
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
			administrationService = new AdministrationService(this, getDaoContext());
		return administrationService;
	}

	/** 
	 * Get the message service.
	 * 
	 * There are several ways to deal with the service layer objects.
	 * 
	 * (1) Dependency injection (preferred)
	 * (2) Instantiate new instance within service (current implementation)
	 * (3) Use bean factory to get reference to bean
	 * (4) Use application context to get reference to bean
	 * 
	 * NOTE:  I prefer method (1) but will not be able to get it to work correctly until I can 
	 * refactor the Context class.  The main issue is that the Context object is instantiated all 
	 * over the place instead of being defined once in the bean definition file.  Therefore, I cannot "inject"
	 * the message service (or any other service) because the client has control over instantiating the object.
	 * I don't like method (2) because I don't want the context to instantiate as there is  
	 * a lot of work that goes into setting up the message service object.  I couldn't figure out to 
	 * get the "openmrs-servlet.xml" resource so I abandoned method (3).  Therefore, I 
	 * have decided to go with method (4) for now.  It ties us (somewhat loosely) to the spring framework
	 * as we now have the Context object implement ApplicationContextAware.  However, my plan is to make
	 * Context an interface and implements this interface as the SpringContext so that certain Spring services
	 * can be used (i.e. event publishing).  
	 *  
	 * @return message service
	 */
	public MessageService getMessageService() { 
		if ( messageService == null ) { 
			messageService = (MessageService) applicationContext.getBean("messageService");
		}
		return messageService;
	}
	 
	/*
	public MessageService getMessageService() { 
		if ( messageService == null ) { 
			try { 
				log.info("Instantiating message service");
				Resource beanDefinition = new ClassPathResource("openmrs-servlet.xml");
				XmlBeanFactory beanFactory = new XmlBeanFactory( beanDefinition );
				messageService = (MessageService)beanFactory.getBean("messageService");
				log.info("Message service = " + messageService);
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
	}	
	*/
	
	/*
	public MessageService getMessageService() {
		if (messageService == null) {
			try { 								
				messageService = new MessageServiceImpl(getDaoContext());
				
				javax.mail.Session mailSession = (javax.mail.Session) = 
					new InitialContext().lookup("java:comp/env/mail/OpenmrsMailSession");

				messageService.setMailSession( mailSession );
				messageService.setMessageSender( new MailMessageSender() );
				messageService.setMessagePreparator( new VelocityMessagePreparator() );
			} catch (Exception e) {
				log.error( "Could not instantiate message service: ", e );
			}
		}
		return messageService;
	}*/

	
	/**
	 * @return "active" user who has been authenticated, 
	 *         otherwise <code>null</code> 
	 */
	public User getAuthenticatedUser() {
		user = getDaoContext().getAuthenticatedUser();
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
		getDaoContext().logout();
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
		
		Role role = getUserService().getRole(OpenmrsConstants.ANONYMOUS_ROLE);
		if (role == null) {
			throw new RuntimeException("Database out of sync with code: " + OpenmrsConstants.ANONYMOUS_ROLE + " role does not exist");
		}
		if (role.hasPrivilege(privilege))
			return true;
	
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
		getDaoContext().openSession();
	}
	
	public void endTransaction() {
		getDaoContext().closeSession();
	}
}
