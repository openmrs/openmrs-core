package org.openmrs.api.context;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.UserService;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.arden.ArdenService;
import org.openmrs.hl7.HL7Service;
import org.openmrs.logic.LogicService;
import org.openmrs.module.ModuleUtil;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessagePreparator;
import org.openmrs.notification.MessageSender;
import org.openmrs.notification.MessageService;
import org.openmrs.notification.impl.MessageServiceImpl;
import org.openmrs.notification.mail.MailMessageSender;
import org.openmrs.notification.mail.velocity.VelocityMessagePreparator;
import org.openmrs.reporting.ReportService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.aop.Advisor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

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

	private static final Log log = LogFactory.getLog(Context.class);
	
	// Global resources 
	private static ContextDAO contextDAO;
	private static ApplicationContext applicationContext;
	private static Session mailSession;
	
	// Using "wrapper" (Object array) around UserContext to avoid ThreadLocal bug in Java 1.5
	private static final ThreadLocal<Object[] /*UserContext */> userContextHolder = new ThreadLocal<Object[] /*UserContext*/>();
	private static ServiceContext serviceContext;
	private static Properties runtimeProperties = new Properties();
	
	// A place to store data that will persiste longer than a session, but won't persist beyond application restart
	// TODO: put an optional expiry date on these items
	private static Map<User, Map<String, Object>> volatileUserData = new HashMap<User, Map<String, Object>>();

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
		log.info("Setting application context");
		applicationContext = context;
	}

	public Object getBean(String beanName) {
		return applicationContext.getBean(beanName);
	}
	
	/**
	 * Gets the context's dao.
	 * 
	 * @return
	 */
	private static ContextDAO getContextDAO() {
		if (contextDAO == null) throw new APIException("contextDAO is null");
		return contextDAO;
	}

	/**
	 * Used to set the context's DAO for the application.
	 * 
	 * @param daoContext
	 */
	public void setContextDAO(ContextDAO dao) {
		contextDAO = dao;
	}

	/**
	 * Sets the user context on the thread local so that the service layer can 
	 * perform authentication/authorization checks.
     *
	 * TODO Make thread-safe because this might be accessed by serveral thread at the same time.
	 * Making this thread safe might make this a bottleneck.
	 * 
	 * @param userContext
	 */
	public static void setUserContext(UserContext ctx) { 
		log.info("Setting user context " + ctx);
		Object[] arr = new Object[] {ctx};
		ctx.setContextDAO(getContextDAO());
		userContextHolder.set(arr);
	}
	
	/**
	 * Clears the user context.
	 */
	public static void clearUserContext() {
		log.info("Clearing user context " + userContextHolder.get());
		//userContextHolder.set(null);
		userContextHolder.remove();
	}
	
	/**
	 * Gets the user context from the thread local.
	 * This might be accessed by serveral threads at the same time.
	 * 
	 * @return
	 */
	public static UserContext getUserContext() {
		Object[] arr = userContextHolder.get();
		
		if (log.isInfoEnabled())
			log.info("Getting user context " + arr + " from userContextHolder " + userContextHolder);
		
		if (arr == null) {
			log.debug("userContext is null. Creating new userContext");
            setUserContext(new UserContext());
        }
		return (UserContext)userContextHolder.get()[0];
	}
	
	/**
	 * Gets the service context.  
	 * 
	 * @return
	 */
	private static ServiceContext getServiceContext() {
		if (serviceContext == null) {
			log.error("serviceContext is null.  Creating new ServiceContext()");
			serviceContext = ServiceContext.getInstance();
		}
		log.debug("serviceContext: " + serviceContext);
		return ServiceContext.getInstance();
	}
	
	/**
	 * Sets the service context.
	 * 
	 * @param ctx
	 */
	public void setServiceContext(ServiceContext ctx) { 
		serviceContext = ctx;
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
	public static void authenticate(String username, String password) throws ContextAuthenticationException {
		log.debug("username: " + username);
		getUserContext().authenticate(username, password);
	}
	
	/**
	 * Become a different user. (You should only be able to do this as a superuser.)
	 * @param systemId
	 * @throws ContextAuthenticationException
	 */
	public static void becomeUser(String systemId) throws ContextAuthenticationException {
		log.debug("systemId: " + systemId);
		getUserContext().becomeUser(systemId);
	}
	
	public static Properties getRuntimeProperties() {
		log.debug("getting runtime properties. size: " + runtimeProperties.size());
		
		Properties props = new Properties();
		for (Map.Entry entry : runtimeProperties.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		
		return props;
	}
	
	public static void setRuntimeProperties(Properties props) {
		runtimeProperties = props;
	}

	/**
	 * @return concept dictionary-related services
	 */
	public static ConceptService getConceptService() {
		return getServiceContext().getConceptService();
	}

	/**
	 * @return encounter-related services
	 */
	public static EncounterService getEncounterService() {
		return getServiceContext().getEncounterService();
	}

	/**
	 * @return observation services
	 */
	public static ObsService getObsService() {
		return getServiceContext().getObsService();
	}

	/**
	 * @return patient-related services
	 */
	public static PatientService getPatientService() {
		return getServiceContext().getPatientService();
	}
	
	public static CohortService getCohortService() {
		return getServiceContext().getCohortService();
	}

	/**
	 * @return person-related services
	 */
	public static PersonService getPersonService() {
		return getServiceContext().getPersonService();
	}
	
	/**
	 * @return Returns the hl7Service.
	 */
	public static HL7Service getHL7Service() {
		return getServiceContext().getHL7Service();
	}

	/**
	 * @return patientset-related services
	 */
	public static PatientSetService getPatientSetService() {
		return getServiceContext().getPatientSetService();
	}

	/**
	 * @return user-related services
	 */
	public static UserService getUserService() {
		return getServiceContext().getUserService();
	}

	/**
	 * @return order service
	 */
	public static OrderService getOrderService() {
		return getServiceContext().getOrderService();
	}

	/**
	 * @return form service
	 */
	public static FormService getFormService() {
		return getServiceContext().getFormService();
	}

	/**
	 * @return report service
	 */
	public static ReportService getReportService() {
		return getServiceContext().getReportService();
	}
	
	/**
	 * @return logic service
	 */
	public static LogicService getLogicService() {
		return getServiceContext().getLogicService();
	}

	/**
	 * @return admin-related services
	 */
	public static AdministrationService getAdministrationService() {
		return getServiceContext().getAdministrationService();
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
	public static SchedulerService getSchedulerService() {
		return getServiceContext().getSchedulerService();
	}
	
	/**
	 * @return alert service
	 */
	public static AlertService getAlertService() {
		return getServiceContext().getAlertService();
	}

	/**
	 * @return arden service
	 */
	public static ArdenService getArdenService() {
		return getServiceContext().getArdenService();
	}
	
	/**
	 * @return program- and workflow-related services
	 */
	public static ProgramWorkflowService getProgramWorkflowService() {
		return getServiceContext().getProgramWorkflowService();
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
	public static MessageService getMessageService() {
		MessageService ms = getServiceContext().getMessageService();
		
		if (ms == null) {
			try { 
				//messageService = (MessageService) applicationContext.getBean("messageService");
				// Message service dependencies
				MessagePreparator preparator = getMessagePreparator();
				MessageSender sender = getMessageSender();
				
				ms = new MessageServiceImpl();
				ms.setMessageSender(sender);
				ms.setMessagePreparator(preparator);
				
			} catch (Exception e) { 
				log.error("Unable to create message service due to : " + e.getMessage(), e);
			}
		}
		return ms;
	}
	
	/**
	 * Gets the mail session required by the mail message service.
	 * 
	 * This function forces authentication via the getAdministrationService() method call
	 * 
	 * @return a java mail session
	 */
	private static javax.mail.Session getMailSession() { 
		if ( mailSession == null ) { 
			
			AdministrationService adminService = getAdministrationService();
			
			Properties props = new Properties();
			props.setProperty("mail.transport.protocol", adminService.getGlobalProperty("mail.transport_protocol"));
			props.setProperty("mail.smtp.host", adminService.getGlobalProperty("mail.smtp_host"));
			props.setProperty("mail.smtp.port", adminService.getGlobalProperty("mail.smtp_port"));
			props.setProperty("mail.from", adminService.getGlobalProperty("mail.from"));
			props.setProperty("mail.debug", adminService.getGlobalProperty("mail.debug"));
			props.setProperty("mail.smtp.auth", adminService.getGlobalProperty("mail.smtp_auth"));

			Authenticator auth = new Authenticator() { 
				public PasswordAuthentication getPasswordAuthentication() { 
					return new PasswordAuthentication(getAdministrationService().getGlobalProperty("mail.user"), getAdministrationService().getGlobalProperty("mail.password"));				
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
	private static MessageSender getMessageSender() { 
		return new MailMessageSender(getMailSession());
	}
	
	/**
	 * Convenience method to allow us to change the configuration more easily. 
	 * 
	 * TODO See todo for message sender. 
	 * @return
	 */
	private static MessagePreparator getMessagePreparator() throws MessageException { 
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
	public static User getAuthenticatedUser() {
		return getUserContext().getAuthenticatedUser();
	}

	/**
	 * @return true if user has been authenticated in this context
	 */
	public static boolean isAuthenticated() {
		return getAuthenticatedUser() != null;
	}

	/**
	 * logs out the "active" (authenticated) user within context
	 * 
	 * @see #authenticate
	 */
	public static void logout() {
		log.info("Logging out : " + getAuthenticatedUser());
		getUserContext().logout();
		clearUserContext();
	}
	
	/**
	 * Convenience method.  Passes through to userContext.getAllRoles(User)
	 */
	public static Set<Role> getAllRoles(User user) throws Exception {
		return getUserContext().getAllRoles();
	}
		
	/**
	 * Convenience method.  Passes through to userContext.hasPrivilege(String)
	 */
	public static boolean hasPrivilege(String privilege) {
		return getUserContext().hasPrivilege(privilege);
	}
	
	/**
	 * Convenience method.  Passes through to @see org.openmrs.api.context.UserContext#addProxyPrivilege(java.lang.String)
	 */
	public static void addProxyPrivilege(String privilege) {
		getUserContext().addProxyPrivilege(privilege);
	}
	
	/**
	 * Convenience method.  Passes through to userContext.removeProxyPrivilege(String)
	 */
	public static void removeProxyPrivilege(String privilege) {
		getUserContext().removeProxyPrivilege(privilege);
	}
	
	/**
	 * Convenience method.  Passes through to userContext.setLocale(Locale)
	 */
	public static void setLocale(Locale locale) {
		getUserContext().setLocale(locale);
	}
	
	/**
	 * Convenience method.  Passes through to userContext.getLocale()
	 */
	public static Locale getLocale() {
		return getUserContext().getLocale();
	}
	
	
	/**
	 * Used to define a unit of work.  All "units of work" should be surrounded by 
	 * openSession and closeSession calls.  
	 */
	public static void openSession() {
		log.info("opening session");
		getContextDAO().openSession();
	}

	/**
	 * Used to define a unit of work.  All "units of work" should be surrounded by 
	 * openSession and closeSession calls.  
	 */
	public static void closeSession() {
		log.info("closing session");
		getContextDAO().closeSession();
	}
	
	/**
	 * Used to clear cached objects out of a session in the middle of a unit of work.
	 */
	public static void clearSession() {
		log.info("clearing session");
		getContextDAO().clearSession();
	}
	
	/**
	 * Starts the OpenMRS System
	 * Should be called prior to any kind of activity
	 * @param Properties
	 */
	public static void startup(Properties props) {
		getContextDAO().startup(props);
		checkDatabaseVersion();
		
		// Loop over each "module" and startup each with the custom
		// properties
		ModuleUtil.startup(props);
		OpenmrsUtil.startup(props);
		SchedulerUtil.startup(props);
		
		checkCoreDataset();
	}
	
	/**
	 * Starts the OpenMRS System in a _non-webapp_ environment
	 * 
	 * @param url database url like "jdbc:mysql://localhost:3306/openmrs?autoReconnect=true"
	 * @param username connection username
	 * @param password connection password
	 * @param Properties other startup properties
	 */
	public static void startup(String url, String username, String password, Properties properties) {
		if (properties == null)
			properties = new Properties();
		
		properties.put("connection.url", url);
		properties.put("connection.username", username);
		properties.put("connection.password", password);
		setRuntimeProperties(properties);
		
		@SuppressWarnings("unused")
		AbstractApplicationContext ctx = new FileSystemXmlApplicationContext("/applicationContext-service.xml");
		
		startup(properties);
	}
	
	/**
	 * Stops the OpenMRS System
	 * Should be called after all activity has ended and application is closing
	 */
	public static void shutdown() {
		
		log.debug("Shutting down the scheduler");
		try {
			// Needs to be shutdown before Hibernate
			SchedulerUtil.shutdown();
		}
		catch (Exception e) {
			log.warn("Error while shutting down scheduler service", e);
		}
		
		log.debug("Shutting down the modeules");
		try {
			ModuleUtil.shutdown();
		}
		catch (Exception e) {
			log.warn("Error while shutting down module system", e);
		}
		
		log.debug("Shutting down the context");
		try {
			ContextDAO dao = null;
			try {
				dao = getContextDAO();
			}
			catch (APIException e) {
				// pass
			}
			if (dao != null)
				dao.shutdown();
		}
		catch (Exception e) {
			log.warn("Error while shutting down context dao", e);
		}
	}
	
	/**
	 * Used for getting services not in the previous get*Service() calls
	 * 
	 * @param cls
	 * @return
	 */
	public static Object getService(Class cls) {
		return getServiceContext().getService(cls);
	}
	
	/**
	 * Adds an AOP advisor around the given Class <code>cls</code>
	 * Advisors can wrap around a method and effect the method before or after
	 * 
	 * @param cls
	 * @param advisor
	 */
	public static void addAdvisor(Class cls, Advisor advisor) {
		getServiceContext().addAdvisor(cls, advisor);
	}
	
	/**
	 * Adds an AOP advice object around the given Class <code>cls</code>
	 * Advice comes in the form of before or afterReturning methods
	 * 
	 * @param cls
	 * @param advice
	 */
	public static void addAdvice(Class cls, Advice advice) {
		getServiceContext().addAdvice(cls, advice);
	}
	
	/**
	 * Removes the given AOP advisor from Class <code>cls</code>
	 * 
	 * @param cls
	 * @param advisor
	 */
	public static void removeAdvisor(Class cls, Advisor advisor) {
		getServiceContext().removeAdvisor(cls, advisor);
	}
	
	/**
	 * Removes the given AOP advice object from Class <code>cls</code>
	 * 
	 * @param cls
	 * @param advice
	 */
	public static void removeAdvice(Class cls, Advice advice) {
		getServiceContext().removeAdvice(cls, advice);
	}
	
	/**
	 * Runs through the core data (e.g. privileges and global properties) and 
	 * adds them if necessary.
	 */
	public static void checkCoreDataset() {
		getContextDAO().checkCoreDataset();
	}
	
	/**
	 * Selects the current database version out of the database from
	 * global_property.property = 'database_version'
	 * 
	 * Sets OpenmrsConstants.DATABASE_VERSION accordingly
	 * 
	 */
	private static void checkDatabaseVersion() {
		OpenmrsConstants.DATABASE_VERSION = getAdministrationService().getGlobalProperty("database_version");
	}
	
	public static Object getVolatileUserData(String key) {
		User u = getAuthenticatedUser();
		if (u == null)
			return null;
		Map<String, Object> myData = volatileUserData.get(u);
		if (myData == null)
			return null;
		else
			return myData.get(key);
	}
	
	public static void setVolatileUserData(String key, Object value) {
		User u = getAuthenticatedUser();
		if (u == null) // TODO: throw something here
			return;
		Map<String, Object> myData = volatileUserData.get(u);
		if (myData == null) {
			myData = new HashMap<String, Object>();
			volatileUserData.put(u, myData);
		}
		myData.put(key, value);
	}
	
	/**
	 * 
	 * @return SimpleDateFormat
	 */
	public static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat(
					OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(getLocale().toString().toLowerCase()), 
					getLocale());
	}
}
