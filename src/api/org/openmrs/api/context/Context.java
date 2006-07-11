package org.openmrs.api.context;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.UserService;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.hibernate.HibernateDAOContext;
import org.openmrs.arden.ArdenService;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.hl7.HL7Service;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessagePreparator;
import org.openmrs.notification.MessageSender;
import org.openmrs.notification.MessageService;
import org.openmrs.notification.impl.MessageServiceImpl;
import org.openmrs.notification.mail.MailMessageSender;
import org.openmrs.notification.mail.velocity.VelocityMessagePreparator;
import org.openmrs.reporting.ReportObjectFactory;
import org.openmrs.reporting.ReportObjectFactoryModule;
import org.openmrs.reporting.ReportService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.timer.TimerSchedulerService;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Represents an OpenMRS <code>Context</code>, which may be used to
 * authenticate to the database and obtain services in order to interact with
 * the system.
 * 
 * Only one <code>User</code> may be authenticated within a context at any
 * given time.
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class Context implements ApplicationContextAware {

	private final Log log = LogFactory.getLog(getClass());

	// Global resources 
	private DAOContext daoContext;
	private ApplicationContext applicationContext;
	private Session mailSession;
	
	
	// User resources
	// TODO Move this into UserContext
	private User user = null;
	private Locale locale = Locale.US;	// every user's default locale
	private List<String> proxies = new Vector<String>();

	
	// Service resources
	private ConceptService conceptService;
	private EncounterService encounterService;
	private ObsService obsService;
	private PatientService patientService;
	private PatientSetService patientSetService;
	private UserService userService;
	private AdministrationService administrationService;
	private FormService formService;
	private OrderService orderService;
	private ReportService reportService;
	private FormEntryService formEntryService;
	private HL7Service hl7Service;
	private AlertService alertService;
	private static SchedulerService schedulerService;
	private static MessageService messageService;
	private ArdenService ardenService;


	/**
	 * Default public constructor
	 * 
	 */
	public Context() { }

	/**
	 * Set application context.  Callback method defined in ApplicationContextAware interface.
	 *
	 * @param   context   the spring application context 
	 */
	public void setApplicationContext(ApplicationContext context) {
		applicationContext = context;
	}

	public Object getBean(String beanName) {
		return applicationContext.getBean(beanName);
	}
	
	/**
	 * Gets the DAO context.
	 * 
	 * NOTE: Instantiates a new DAO context if one does not already exist. This
	 * means that a new DAO context is created for every context. If we have 100
	 * users currently logged in, that means there will be 100 DAO context
	 * instances in memory.
	 * 
	 * We should be using dependency injection. There is a context instance for
	 * each new http session that is created, so this becomes a bit more
	 * difficult. There are two separate paradigms in play here: (1)
	 * user-specific context (2) service locator. One is a user specific object,
	 * the other is an app-specific object. We should have define these more
	 * clearly and keep the two distinct to avoid issues like this. By the way,
	 * I changed the method to use camel-case for readability if/when we use
	 * dependency injection in the future.
	 * 
	 * TODO: Refactor into separate classes (UserContext vs. ServiceContext)
	 * or refactor into user context (ONLY) and use dependency injection in
	 * Controller (and other client) classes to access services layer.
	 * 
	 * TODO: Remove dependency of context within services layer.
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
	 * @param username
	 *            user's identifier token for login
	 * @param password
	 *            user's password for authenticating to context
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
	 * @return Returns the hl7Service.
	 */
	public HL7Service getHL7Service() {
		if (hl7Service == null)
			hl7Service = new HL7Service(this, getDaoContext());
		return hl7Service;
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
		
		//
		// TODO This is a temporary solution until report info is moved out of openmrs-servlet.xml
		//
		if (reportService == null) {
			List<ReportObjectFactoryModule> modules = new Vector<ReportObjectFactoryModule>();
			ReportObjectFactoryModule module = new ReportObjectFactoryModule();
			module.setName("PatientCharacteristicFilter");
			module.setDisplayName("Patient Characteristic Filter");
			module.setClassName("org.openmrs.reporting.PatientCharacteristicFilter");
			module.setType("Patient Filter");
			module.setValidatorClass("org.openmrs.reporting.PatientCharacteristicFilterValidator");
			modules.add(module);
			module = new ReportObjectFactoryModule();
			module.setName("NumericObsPatientFilter");
			module.setDisplayName("Numeric Observation Patient Filter");
			module.setClassName("org.openmrs.reporting.NumericObsPatientFilter");
			module.setType("Patient Filter");
			module.setValidatorClass("org.openmrs.reporting.NumericObsPatientFilterValidator");
			modules.add(module);
			module = new ReportObjectFactoryModule();
			module.setName("ShortDescriptionProducer");
			module.setDisplayName("Short Description Producer");
			module.setClassName("org.openmrs.reporting.ShortDescriptionProducer");
			module.setType("Patient Data Producer");
			module.setValidatorClass("org.openmrs.reporting.ShortDescriptionProducerValidator");
			modules.add(module);
			ReportObjectFactory factory = new ReportObjectFactory();
			factory.setDefaultValidator("org.openmrs.web.controller.report.ReportObjectValidator");
			factory.setModules(modules);
			
			reportService = new ReportService(this, getDaoContext(), factory);
		}
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
			administrationService = new AdministrationService(this,
					getDaoContext());
		return administrationService;
	}

	/*
	 * This doesn't really belong here - it's really in the src/web tree
	public FieldGenHandlerFactory getFieldGenHandlerFactory() {
		if (fieldGenHandlerFactory == null) {
			fieldGenHandlerFactory= (FieldGenHandlerFactory)applicationContext.getBean("fieldGenHandlerFactory");
		}
		return fieldGenHandlerFactory;
	}
	*/

	/**
	 * @return scheduler service
	 */
	public SchedulerService getSchedulerService() {
		if (schedulerService == null) { 
			schedulerService = new TimerSchedulerService(this);
			schedulerService.setDaoContext( getDaoContext() );
			schedulerService.startup();	// important!
		}
		return schedulerService;
	}
  

	/**
	 * Set the scheduler service.
	 * 
	 * @param service
	 */
	public void setSchedulerService(SchedulerService service) { 
		schedulerService = service;
	}
 	

	/**
	 * @return alert service
	 */
	public AlertService getAlertService() {
		if (alertService == null)
		  alertService = new AlertService(this, getDaoContext());
		return alertService;
	}

	/**
	 * @return arden service
	 */
	public ArdenService getArdenService() {
		if (ardenService == null)
		  ardenService = new ArdenService(this, getDaoContext());
		return ardenService;
	}
	
	/**
	 * Get the message service.
	 * 
	 * There are several ways to deal with the service layer objects.
	 * 
	 * (1) Dependency injection (preferred) (2) Instantiate new instance within
	 * service (current implementation) (3) Use bean factory to get reference to
	 * bean (4) Use application context to get reference to bean
	 * 
	 * NOTE: I prefer method (1) but will not be able to get it to work
	 * correctly until I can refactor the Context class. The main issue is that
	 * the Context object is instantiated all over the place instead of being
	 * defined once in the bean definition file. Therefore, I cannot "inject"
	 * the message service (or any other service) because the client has control
	 * over instantiating the object. I don't like method (2) because I don't
	 * want the context to instantiate as there is a lot of work that goes into
	 * setting up the message service object. I couldn't figure out to get the
	 * "openmrs-servlet.xml" resource so I abandoned method (3). Therefore, I
	 * have decided to go with method (4) for now. It ties us (somewhat loosely)
	 * to the spring framework as we now have the Context object implement
	 * ApplicationContextAware. However, my plan is to make Context an interface
	 * and implements this interface as the SpringContext so that certain Spring
	 * services can be used (i.e. event publishing).
	 * 
	 * @return message service
	 */
	public MessageService getMessageService() {
		if (messageService == null) {
			try { 
				//messageService = (MessageService) applicationContext.getBean("messageService");
				// Message service dependencies
				MessagePreparator preparator = getMessagePreparator();
				MessageSender sender = getMessageSender();
				
				messageService = new MessageServiceImpl(getDaoContext());
				messageService.setMessageSender(sender);
				messageService.setMessagePreparator(preparator);
				
			} catch (Exception e) { 
				log.error("Unable to create message service due to : " + e.getMessage(), e);
			}
		}
		return messageService;
	}
	
	/**
	 * Sets the message service to be used by the application.
	 * @param service
	 */
	public void setMessageService(MessageService service) { 
		this.messageService = service;
	}
	
	/**
	 * Gets the mail session required by the mail message service.
	 * 
	 * TODO I gave up trying to get this working within the Spring context.  
	 * 
	 * @return a java mail session
	 */
	private javax.mail.Session getMailSession() { 
		if ( mailSession == null ) { 
			
			Properties props = OpenmrsConstants.MAIL_PROPERTIES;

			Authenticator auth = new Authenticator() { 
				public PasswordAuthentication getPasswordAuthentication() { 
					return new PasswordAuthentication(OpenmrsConstants.MAIL_USER, OpenmrsConstants.MAIL_PASSWORD);				
				}
			};
			
			mailSession = Session.getInstance(props, auth);
		}
		return mailSession;
	}
	
	
	
	
	/**
	 * Convenience method to allow us to change the configuration more easily. 
	 * 
	 * TODO Ideally, we would be using Spring's method injection to set the dependencies
	 * for the message service.  However, we are currently tied to creating Context
	 * objects for each user and need to assign all dependencies within the code.
	 * @return
	 */	
	private MessageSender getMessageSender() { 
		return new MailMessageSender(getMailSession());
	}
	
	/**
	 * Convenience method to allow us to change the configuration more easily. 
	 * 
	 * TODO See todo for message sender. 
	 * @return
	 */
	private MessagePreparator getMessagePreparator() throws MessageException { 
		return new VelocityMessagePreparator();
	}
	
	/*
	 * public MessageService getMessageService() { if ( messageService == null ) {
	 * try { log.info("Instantiating message service"); Resource beanDefinition =
	 * new ClassPathResource("openmrs-servlet.xml"); XmlBeanFactory beanFactory =
	 * new XmlBeanFactory( beanDefinition ); messageService =
	 * (MessageService)beanFactory.getBean("messageService"); log.info("Message
	 * service = " + messageService); } catch (Exception e) {
	 * e.printStackTrace(); } } }
	 */

	/*
	 * public MessageService getMessageService() { if (messageService == null) {
	 * try { messageService = new MessageServiceImpl(getDaoContext());
	 * 
	 * javax.mail.Session mailSession = (javax.mail.Session) = new
	 * InitialContext().lookup("java:comp/env/mail/OpenmrsMailSession");
	 * 
	 * messageService.setMailSession( mailSession );
	 * messageService.setMessageSender( new MailMessageSender() );
	 * messageService.setMessagePreparator( new VelocityMessagePreparator() ); }
	 * catch (Exception e) { log.error( "Could not instantiate message service: ",
	 * e ); } } return messageService; }
	 */

	/**
	 * @return "active" user who has been authenticated, otherwise
	 *         <code>null</code>
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
	 * 
	 * @see #authenticate
	 */
	public void logout() {
		user = null;
		getDaoContext().logout();
	}
	
	/**
	 * Gets all the roles for the (un)authenticated user.
	 * Anonymous and Authenticated roles are appended if necessary
	 * 
	 * @return all expanded roles for a user
	 * @throws Exception
	 */
	public Set<Role> getAllRoles() throws Exception {
		return getAllRoles(user);
	}
	
	/**
	 * Gets all the roles for a user.  Anonymous and Authenticated roles are 
	 * appended if necessary
	 * 
	 * @param user
	 * @return all expanded roles for a user
	 */
	public Set<Role> getAllRoles(User user) throws Exception {
		Set<Role> roles = new HashSet<Role>();
		
		// add the Anonymous Role
		Role role = getUserService().getRole(OpenmrsConstants.ANONYMOUS_ROLE);
		if (role == null) {
			throw new RuntimeException("Database out of sync with code: "
					+ OpenmrsConstants.ANONYMOUS_ROLE + " role does not exist");
		}
		roles.add(role);
		
		// add the Authenticated role
		if (this.user != null && this.user.equals(user)) {
			roles.addAll(user.getAllRoles());
			Role authRole = getUserService().getRole(
					OpenmrsConstants.AUTHENTICATED_ROLE);
			if (authRole == null) {
				throw new RuntimeException("Database out of sync with code: "
						+ OpenmrsConstants.AUTHENTICATED_ROLE + " role does not exist");
			}
			roles.add(authRole);
		}
		
		return roles;
	}

	/**
	 * Tests whether or not currently authenticated user has a particular
	 * privilege
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

			Role authRole = getUserService().getRole(
					OpenmrsConstants.AUTHENTICATED_ROLE);
			if (authRole == null) {
				throw new RuntimeException("Database out of sync with code: "
						+ OpenmrsConstants.AUTHENTICATED_ROLE + " role does not exist");
			}
			if (authRole.hasPrivilege(privilege))
				return true;
		}

		log.debug("Checking '" + privilege + "' against proxes: " + proxies);
		// check proxied privileges
		for (String s : proxies)
			if (s.equals(privilege))
				return true;
		
		// check anonymous privileges
		Role role = getUserService().getRole(OpenmrsConstants.ANONYMOUS_ROLE);
		if (role == null) {
			throw new RuntimeException("Database out of sync with code: "
					+ OpenmrsConstants.ANONYMOUS_ROLE + " role does not exist");
		}
		if (role.hasPrivilege(privilege))
			return true;

		// default return value
		return false;
	}

	/**
	 * Gives the given privilege to all calls to hasPrivilege. This method was
	 * visualized as being used as follows:
	 * 
	 * <code>
	 * context.addProxyPrivilege("AAA");
	 * context.get*Service().methodRequiringAAAPrivilege();
	 * context.removeProxyPrivilege("AAA");
	 * </code>
	 * 
	 * @param privilege
	 *            to give to users
	 */
	public void addProxyPrivilege(String privilege) {
		proxies.add(privilege);
	}

	/**
	 * Will remove one instance of privilege from the privileges that are
	 * currently proxied
	 * 
	 * @param privilege
	 */
	public void removeProxyPrivilege(String privilege) {
		if (proxies.contains(privilege))
			proxies.remove(privilege);
	}

	/**
	 * @param locale
	 *            new locale for this context
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
