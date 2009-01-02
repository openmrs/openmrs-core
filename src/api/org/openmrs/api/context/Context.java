/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
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
import org.openmrs.api.DataSetService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.ReportService;
import org.openmrs.api.UserService;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.arden.ArdenService;
import org.openmrs.hl7.HL7Service;
import org.openmrs.logic.LogicService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.ModuleUtil;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessagePreparator;
import org.openmrs.notification.MessageSender;
import org.openmrs.notification.MessageService;
import org.openmrs.notification.mail.MailMessageSender;
import org.openmrs.notification.mail.velocity.VelocityMessagePreparator;
import org.openmrs.reporting.ReportObjectService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.InputRequiredException;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.aop.Advisor;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Represents an OpenMRS <code>Context</code>, which may be used to authenticate to the database and
 * obtain services in order to interact with the system.<br/>
 * <br/>
 * The Context is split into a {@link UserContext} and {@link ServiceContext}. The UserContext is
 * lightweight and there is an instance for every user logged into the system. The ServiceContext is
 * heavier and contains each service class. This is more static and there is only one ServiceContext
 * per OpenMRS instance. <br/>
 * <br/>
 * 
 * @see org.openmrs.api.context.UserContext
 * @see org.openmrs.api.context.ServiceContext
 */
public class Context {
	
	private static final Log log = LogFactory.getLog(Context.class);
	
	// Global resources 
	private static ContextDAO contextDAO;
	
	private static Session mailSession;
	
	// Using "wrapper" (Object array) around UserContext to avoid ThreadLocal bug in Java 1.5
	private static final ThreadLocal<Object[] /*UserContext */> userContextHolder = new ThreadLocal<Object[] /*UserContext*/>();
	
	private static ServiceContext serviceContext;
	
	private static Properties runtimeProperties = new Properties();
	
	// A place to store data that will persist longer than a session, but won't persist beyond application restart
	// TODO: put an optional expire date on these items
	private static Map<User, Map<String, Object>> volatileUserData = new HashMap<User, Map<String, Object>>();
	
	/**
	 * Default public constructor
	 */
	public Context() {
	}
	
	/**
	 * Gets the context's data access object
	 * 
	 * @return ContextDAO
	 */
	private static ContextDAO getContextDAO() {
		if (contextDAO == null)
			throw new APIException("contextDAO is null");
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
	 * Loads a class with an instance of the OpenmrsClassLoader. Convenience method equivalent to
	 * OpenmrsClassLoader.getInstance().loadClass(className);
	 * 
	 * @param className the class to load
	 * @return the class that was loaded
	 * @throws ClassNotFoundException
	 * @should load class with the OpenmrsClassLoader
	 */
	public static Class<?> loadClass(String className) throws ClassNotFoundException {
		return OpenmrsClassLoader.getInstance().loadClass(className);
	}
	
	/**
	 * Sets the user context on the thread local so that the service layer can perform
	 * authentication/authorization checks. TODO Make thread-safe because this might be accessed by
	 * several thread at the same time. Making this thread safe might make this a bottleneck.
	 * 
	 * @param userContext
	 */
	public static void setUserContext(UserContext ctx) {
		if (log.isTraceEnabled())
			log.trace("Setting user context " + ctx);
		
		Object[] arr = new Object[] { ctx };
		userContextHolder.set(arr);
	}
	
	/**
	 * Clears the user context from the threadlocal.
	 */
	public static void clearUserContext() {
		if (log.isTraceEnabled())
			log.trace("Clearing user context " + userContextHolder.get());
		
		userContextHolder.remove();
	}
	
	/**
	 * Gets the user context from the thread local. This might be accessed by several threads at the
	 * same time.
	 * 
	 * @return
	 */
	public static UserContext getUserContext() {
		Object[] arr = userContextHolder.get();
		
		if (log.isTraceEnabled())
			log.trace("Getting user context " + arr + " from userContextHolder " + userContextHolder);
		
		if (arr == null) {
			log.trace("userContext is null. Creating new userContext");
			setUserContext(new UserContext());
		}
		return (UserContext) userContextHolder.get()[0];
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
		
		if (log.isTraceEnabled())
			log.trace("serviceContext: " + serviceContext);
		
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
	 * @param username user's identifier token for login
	 * @param password user's password for authenticating to context
	 * @throws ContextAuthenticationException
	 */
	public static void authenticate(String username, String password) throws ContextAuthenticationException {
		if (log.isDebugEnabled())
			log.debug("Authenticating with username: " + username);
		
		getUserContext().authenticate(username, password, getContextDAO());
	}
	
	/**
	 * Become a different user. (You should only be able to do this as a superuser.)
	 * 
	 * @param systemId
	 * @throws ContextAuthenticationException
	 */
	public static void becomeUser(String systemId) throws ContextAuthenticationException {
		if (log.isInfoEnabled())
			log.info("systemId: " + systemId);
		
		getUserContext().becomeUser(systemId);
	}
	
	/**
	 * Get the runtime properties that this OpenMRS instance was started with
	 * 
	 * @return copy of the runtime properties
	 */
	public static Properties getRuntimeProperties() {
		if (log.isTraceEnabled())
			log.trace("getting runtime properties. size: " + runtimeProperties.size());
		
		Properties props = new Properties();
		props.putAll(runtimeProperties);
		
		return props;
	}
	
	/**
	 * Set the runtime properties to be used by this OpenMRS instance
	 * 
	 * @param props runtime properties
	 */
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
	 * @return location services
	 */
	public static LocationService getLocationService() {
		return getServiceContext().getLocationService();
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
	 * @return report object service
	 */
	public static ReportObjectService getReportObjectService() {
		return getServiceContext().getReportObjectService();
	}
	
	/**
	 * @return dataset service
	 */
	public static DataSetService getDataSetService() {
		return getServiceContext().getDataSetService();
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
	
	/**
	 * @return MessageSourceService
	 */
	public static MessageSourceService getMessageSourceService() {
		return getServiceContext().getMessageSourceService();
	}
	
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
	 * @return message service
	 */
	public static MessageService getMessageService() {
		MessageService ms = getServiceContext().getMessageService();
		try {
			// Message service dependencies
			if (ms.getMessagePreparator() == null)
				ms.setMessagePreparator(getMessagePreparator());
			
			if (ms.getMessageSender() == null)
				ms.setMessageSender(getMessageSender());
			
		}
		catch (Exception e) {
			log.error("Unable to create message service due", e);
		}
		return ms;
	}
	
	/**
	 * Gets the mail session required by the mail message service. This function forces
	 * authentication via the getAdministrationService() method call
	 * 
	 * @return a java mail session
	 */
	private static javax.mail.Session getMailSession() {
		if (mailSession == null) {
			
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
					return new PasswordAuthentication(getAdministrationService().getGlobalProperty("mail.user"),
					        getAdministrationService().getGlobalProperty("mail.password"));
				}
			};
			
			mailSession = Session.getInstance(props, auth);
		}
		return mailSession;
	}
	
	/**
	 * Convenience method to allow us to change the configuration more easily. TODO Ideally, we
	 * would be using Spring's method injection to set the dependencies for the message service.
	 * 
	 * @return
	 */
	private static MessageSender getMessageSender() {
		return new MailMessageSender(getMailSession());
	}
	
	/**
	 * Convenience method to allow us to change the configuration more easily. TODO See todo for
	 * message sender.
	 * 
	 * @return
	 */
	private static MessagePreparator getMessagePreparator() throws MessageException {
		return new VelocityMessagePreparator();
	}
	
	/**
	 * @return "active" user who has been authenticated, otherwise <code>null</code>
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
		if (log.isDebugEnabled())
			log.debug("Logging out : " + getAuthenticatedUser());
		
		getUserContext().logout();
		clearUserContext();
	}
	
	/**
	 * Convenience method. Passes through to userContext.getAllRoles(User)
	 */
	public static Set<Role> getAllRoles(User user) throws Exception {
		return getUserContext().getAllRoles();
	}
	
	/**
	 * Convenience method. Passes through to userContext.hasPrivilege(String)
	 */
	public static boolean hasPrivilege(String privilege) {
		return getUserContext().hasPrivilege(privilege);
	}
	
	/**
	 * Throws an exception if the currently authenticated user does not have the specified
	 * privilege.
	 * 
	 * @param privilege
	 * @throws ContextAuthenticationException
	 */
	public static void requirePrivilege(String privilege) throws ContextAuthenticationException {
		if (!hasPrivilege(privilege))
			throw new ContextAuthenticationException();
	}
	
	/**
	 * Convenience method. Passes through to {@link UserContext#addProxyPrivilege(String)}
	 */
	public static void addProxyPrivilege(String privilege) {
		getUserContext().addProxyPrivilege(privilege);
	}
	
	/**
	 * Convenience method. Passes through to {@link UserContext#removeProxyPrivilege(String)}
	 */
	public static void removeProxyPrivilege(String privilege) {
		getUserContext().removeProxyPrivilege(privilege);
	}
	
	/**
	 * Convenience method. Passes through to {@link UserContext#setLocale(Locale)}
	 */
	public static void setLocale(Locale locale) {
		getUserContext().setLocale(locale);
	}
	
	/**
	 * Convenience method. Passes through to userContext.getLocale()
	 */
	public static Locale getLocale() {
		return getUserContext().getLocale();
	}
	
	/**
	 * Used to define a unit of work. All "units of work" should be surrounded by openSession and
	 * closeSession calls.
	 */
	public static void openSession() {
		log.trace("opening session");
		getContextDAO().openSession();
	}
	
	/**
	 * Used to define a unit of work. All "units of work" should be surrounded by openSession and
	 * closeSession calls.
	 */
	public static void closeSession() {
		log.trace("closing session");
		getContextDAO().closeSession();
	}
	
	/**
	 * Used to clear cached objects out of a session in the middle of a unit of work.
	 */
	public static void clearSession() {
		log.trace("clearing session");
		getContextDAO().clearSession();
	}
	
	/**
	 * Used to clear a cached object out of a session in the middle of a unit of work. Future
	 * updates to this object will not be saved. Future gets of this object will not fetch this
	 * cached copy
	 * 
	 * @param obj The object to evict/remove from the session
	 */
	public static void evictFromSession(Object obj) {
		log.trace("clearing session");
		getContextDAO().evictFromSession(obj);
	}
	
	/**
	 * Starts the OpenMRS System Should be called prior to any kind of activity <br/>
	 * <br/>
	 * If an {@link InputRequiredException} is thrown, a call to {@link DatabaseUpdater#update(Map)}
	 * will be required with a mapping from question prompt to user answer before startup can be
	 * called again.
	 * 
	 * @param Properties runtime properties to use for startup
	 * @throws InputRequiredException if the {@link DatabaseUpdater} has determined that updates
	 *             cannot continue without input from the user
	 * @see InputRequiredException#getRequiredInput() InputRequiredException#getRequiredInput() for
	 *      the required question/datatypes
	 */
	public static void startup(Properties props) throws DatabaseUpdateException, InputRequiredException {
		// do any context database specific startup
		getContextDAO().startup(props);
		
		// find/set/check whether the current database version is compatible
		checkForDatabaseUpdates(props);
		
		// this should be first in the startup routines so that the application
		// data directory can be set from the runtime properties
		OpenmrsUtil.startup(props);
		
		// Loop over each module and startup each with these custom properties
		ModuleUtil.startup(props);
		
		// start the scheduled tasks
		SchedulerUtil.startup(props);
		
		// add any privileges/roles that /must/ exist for openmrs to work correctly.
		// TODO: Should this be one of the first things executed at startup? 
		checkCoreDataset();
	}
	
	/**
	 * Starts the OpenMRS System in a _non-webapp_ environment<br/>
	 * <br/>
	 * If an {@link InputRequiredException} is thrown, a call to {@link DatabaseUpdater#update(Map)}
	 * will be required with a mapping from question prompt to user answer before startup can be
	 * called again.
	 * 
	 * @param url database url like "jdbc:mysql://localhost:3306/openmrs?autoReconnect=true"
	 * @param username connection username
	 * @param password connection password
	 * @param Properties other startup properties
	 * @throws InputRequiredException if the {@link DatabaseUpdater} has determined that updates
	 *             cannot continue without input from the user
	 * @see #startup(Properties)
	 * @see InputRequiredException#getRequiredInput() InputRequiredException#getRequiredInput() for
	 *      the required question/datatypes
	 */
	public static void startup(String url, String username, String password, Properties properties)
	                                                                                               throws DatabaseUpdateException,
	                                                                                               InputRequiredException {
		if (properties == null)
			properties = new Properties();
		
		properties.put("connection.url", url);
		properties.put("connection.username", username);
		properties.put("connection.password", password);
		setRuntimeProperties(properties);
		
		@SuppressWarnings("unused")
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-service.xml");
		
		startup(properties);
	}
	
	/**
	 * Stops the OpenMRS System Should be called after all activity has ended and application is
	 * closing
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
		
		log.debug("Shutting down the modules");
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
	public static <T extends Object> T getService(Class<? extends T> cls) {
		return getServiceContext().getService(cls);
	}
	
	/**
	 * Adds an AOP advisor around the given Class <code>cls</code> Advisors can wrap around a method
	 * and effect the method before or after
	 * 
	 * @param cls
	 * @param advisor
	 */
	@SuppressWarnings("unchecked")
	public static void addAdvisor(Class cls, Advisor advisor) {
		getServiceContext().addAdvisor(cls, advisor);
	}
	
	/**
	 * Adds an AOP advice object around the given Class <code>cls</code> Advice comes in the form of
	 * before or afterReturning methods
	 * 
	 * @param cls
	 * @param advice
	 */
	@SuppressWarnings("unchecked")
	public static void addAdvice(Class cls, Advice advice) {
		getServiceContext().addAdvice(cls, advice);
	}
	
	/**
	 * Removes the given AOP advisor from Class <code>cls</code>
	 * 
	 * @param cls
	 * @param advisor
	 */
	@SuppressWarnings("unchecked")
	public static void removeAdvisor(Class cls, Advisor advisor) {
		getServiceContext().removeAdvisor(cls, advisor);
	}
	
	/**
	 * Removes the given AOP advice object from Class <code>cls</code>
	 * 
	 * @param cls
	 * @param advice
	 */
	@SuppressWarnings("unchecked")
	public static void removeAdvice(Class cls, Advice advice) {
		getServiceContext().removeAdvice(cls, advice);
	}
	
	/**
	 * Runs through the core data (e.g. privileges and global properties) and adds them if
	 * necessary.
	 */
	public static void checkCoreDataset() {
		getContextDAO().checkCoreDataset();
	}
	
	/**
	 * Sets {@link OpenmrsConstants#DATABASE_VERSION} accordingly for backwards compatibility,
	 * however, this is no longer needed because we are using individual liquibase updates now that
	 * can be run out of order.<br/>
	 * <br/>
	 * If an {@link InputRequiredException} is thrown, a call to {@link DatabaseUpdater#update(Map)}
	 * will be required with a mapping from question prompt to user answer
	 * 
	 * @param props the runtime properties
	 * @throws InputRequiredException if the {@link DatabaseUpdater} has determined that updates
	 *             cannot continue without input from the user
	 * @see InputRequiredException#getRequiredInput() InputRequiredException#getRequiredInput() for
	 *      the required question/datatypes
	 */
	private static void checkForDatabaseUpdates(Properties props) throws DatabaseUpdateException, InputRequiredException {
		try {
			Context.addProxyPrivilege("");
			OpenmrsConstants.DATABASE_VERSION = getAdministrationService().getGlobalProperty("database_version");
		}
		finally {
			Context.removeProxyPrivilege("");
		}
		
		// TODO make sure the user has "permission" to run these updates by checking the runtime property for auto updating
		
		DatabaseUpdater.update();
	}
	
	/**
	 * Get a piece of information for the currently authenticated user. This information is stored
	 * only temporarily. When a new module is loaded or the server is restarted, this information
	 * will disappear. If there is not information by this key, null is returned TODO: This needs to
	 * be refactored/removed
	 * 
	 * @param key identifying string for the information
	 * @return the information stored
	 */
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
	
	/**
	 * Set a piece of information for the currently authenticated user. This information is stored
	 * only temporarily. When a new module is loaded or the server is restarted, this information
	 * will disappear TODO: This needs to be refactored/removed
	 * 
	 * @param key identifying string for this information
	 * @param value information to be stored
	 */
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
	 * Gets the simple date format for the current user's locale. The format will be similar in size
	 * to mm/dd/yyyy
	 * 
	 * @return SimpleDateFormat
	 * @see org.openmrs.util.OpenmrsConstants#OPENMRS_LOCALE_DATE_PATTERNS()
	 */
	public static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS()
		        .get(getLocale().toString().toLowerCase()), getLocale());
	}
	
	/**
	 * @return true/false whether the service context is currently being refreshed
	 * @see org.openmrs.api.context.ServiceContext#isRefreshingContext()
	 */
	public static boolean isRefreshingContext() {
		return getServiceContext().isRefreshingContext();
	}
}
