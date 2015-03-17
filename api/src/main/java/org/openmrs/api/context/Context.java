/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Arrays;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.aopalliance.aop.Advice;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.ActiveListService;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.SerializationService;
import org.openmrs.api.UserService;
import org.openmrs.api.VisitService;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.arden.ArdenService;
import org.openmrs.hl7.HL7Service;
import org.openmrs.logic.LogicService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.ModuleMustStartException;
import org.openmrs.module.ModuleUtil;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessagePreparator;
import org.openmrs.notification.MessageSender;
import org.openmrs.notification.MessageService;
import org.openmrs.notification.NoteService;
import org.openmrs.notification.mail.MailMessageSender;
import org.openmrs.notification.mail.velocity.VelocityMessagePreparator;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.InputRequiredException;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.aop.Advisor;

/**
 * Represents an OpenMRS <code>Context</code>, which may be used to authenticate to the database and
 * obtain services in order to interact with the system.<br/>
 * <br/>
 * The Context is split into a {@link UserContext} and {@link ServiceContext}. The UserContext is
 * lightweight and there is an instance for every user logged into the system. The ServiceContext is
 * heavier and contains each service class. This is more static and there is only one ServiceContext
 * per OpenMRS instance. <br/>
 * <br/>
 * Both the {@link UserContext} and the {@link ServiceContext} should not be used directly. This
 * Context class has methods to pass through to the currently defined UserContext for the thread and
 * the currently defined ServiceContext. <br/>
 * <br/>
 * To use the OpenMRS api there are four things that have to be done:
 * <ol>
 * <li>Call {@link Context#startup(String, String, String, Properties)} to let the Context contact
 * the database</li>
 * <li>Call {@link Context#openSession()} to start a "unit of work".</li>
 * <li>Call {@link Context#authenticate(String, String)} to authenticate the current user on the
 * current thread</li>
 * <li>Call {@link Context#closeSession()} to end your "unit of work" and commit all changes to the
 * database.</li>
 * </ol>
 * <br/>
 * Example usage:
 * 
 * <pre>
 * 	public static void main(String[] args) {
 * 		Context.startup("jdbc:mysql://localhost:3306/db-name?autoReconnect=true", "openmrs-db-user", "3jknfjkn33ijt", new Properties());
 * 		try {
 * 			Context.openSession();
 * 			Context.authenticate("admin", "test");
 * 			List<Patients> patients = Context.getPatientService().getPatientsByName("Fred");
 * 			patients.get(0).setBirthdate(new Date());
 * 			Context.getPatientService().savePatient(patients.get(0));
 * 			...
 *        }
 * 		finally {
 * 			Context.closeSession();
 *        }
 *    }
 * </pre>
 * 
 * @see org.openmrs.api.context.UserContext
 * @see org.openmrs.api.context.ServiceContext
 */
public class Context {
	
	private static final Log log = LogFactory.getLog(Context.class);
	
	// Global resources
	private static ContextDAO contextDAO;
	
	private static Session mailSession;
	
	// Using "wrapper" (Object array) around UserContext to avoid ThreadLocal
	// bug in Java 1.5
	private static final ThreadLocal<Object[] /* UserContext */> userContextHolder = new ThreadLocal<Object[] /* UserContext */>();
	
	private static ServiceContext serviceContext;
	
	private static Properties runtimeProperties = new Properties();
	
	private static Properties configProperties = new Properties();
	
	// A place to store data that will persist longer than a session, but won't
	// persist beyond application restart
	@Deprecated
	private static Map<User, Map<String, Object>> volatileUserData = new WeakHashMap<User, Map<String, Object>>();
	
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
		if (contextDAO == null) {
			throw new APIException("error.context.null", (Object[]) null);
		}
		return contextDAO;
	}
	
	/**
	 * Used to set the context's DAO for the application.
	 * 
	 * @param dao ContextDAO to set
	 */
	public void setContextDAO(ContextDAO dao) {
		setDAO(dao);
	}
	
	public static void setDAO(ContextDAO dao) {
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
	 * authentication/authorization checks.<br/>
	 * <br />
	 * This is thread safe since it stores the given user context in ThreadLocal.
	 * 
	 * @param ctx UserContext to set
	 */
	public static void setUserContext(UserContext ctx) {
		if (log.isTraceEnabled()) {
			log.trace("Setting user context " + ctx);
		}
		
		Object[] arr = new Object[] { ctx };
		userContextHolder.set(arr);
	}
	
	/**
	 * Clears the user context from the threadlocal.
	 */
	public static void clearUserContext() {
		if (log.isTraceEnabled()) {
			log.trace("Clearing user context " + userContextHolder.get());
		}
		
		userContextHolder.remove();
	}
	
	/**
	 * Gets the user context from the thread local. This might be accessed by several threads at the
	 * same time.
	 * 
	 * @return The current UserContext for this thread.
	 * @should fail if session hasnt been opened
	 */
	public static UserContext getUserContext() {
		Object[] arr = userContextHolder.get();
		
		if (log.isTraceEnabled()) {
			log.trace("Getting user context " + Arrays.toString(arr) + " from userContextHolder " + userContextHolder);
		}
		
		if (arr == null) {
			log.trace("userContext is null.");
			throw new APIException(
			        "A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API");
		}
		return (UserContext) userContextHolder.get()[0];
	}
	
	/**
	 * Gets the currently defined service context. If one is not defined, one will be created and
	 * then returned.
	 * 
	 * @return the current ServiceContext
	 */
	static ServiceContext getServiceContext() {
		if (serviceContext == null) {
			log.error("serviceContext is null.  Creating new ServiceContext()");
			serviceContext = ServiceContext.getInstance();
		}
		
		if (log.isTraceEnabled()) {
			log.trace("serviceContext: " + serviceContext);
		}
		
		return ServiceContext.getInstance();
	}
	
	/**
	 * Sets the service context.
	 * 
	 * @param ctx
	 */
	public void setServiceContext(ServiceContext ctx) {
		setContext(ctx);
	}
	
	public static void setContext(ServiceContext ctx) {
		serviceContext = ctx;
	}
	
	/**
	 * Used to authenticate user within the context
	 * 
	 * @param username user's identifier token for login
	 * @param password user's password for authenticating to context
	 * @throws ContextAuthenticationException
	 * @should not authenticate with null username and password
	 * @should not authenticate with null password
	 * @should not authenticate with null username
	 * @should not authenticate with null password and proper username
	 * @should not authenticate with null password and proper system id
	 */
	public static void authenticate(String username, String password) throws ContextAuthenticationException {
		if (log.isDebugEnabled()) {
			log.debug("Authenticating with username: " + username);
		}
		
		if (Daemon.isDaemonThread()) {
			log.error("Authentication attempted while operating on a "
			        + "daemon thread, authenticating is not necessary or allowed");
			return;
		}
		
		getUserContext().authenticate(username, password, getContextDAO());
	}
	
	/**
	 * Refresh the authenticated user object in the current UserContext. This should be used when
	 * updating information in the database about the current user and it needs to be reflecting in
	 * the (cached) {@link #getAuthenticatedUser()} User object.
	 * 
	 * @since 1.5
	 * @should get fresh values from the database
	 */
	public static void refreshAuthenticatedUser() {
		if (Daemon.isDaemonThread()) {
			return;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Refreshing authenticated user");
		}
		
		getUserContext().refreshAuthenticatedUser();
	}
	
	/**
	 * Become a different user. (You should only be able to do this as a superuser.)
	 * 
	 * @param systemId
	 * @throws ContextAuthenticationException
	 * @should change locale when become another user
	 */
	public static void becomeUser(String systemId) throws ContextAuthenticationException {
		if (log.isInfoEnabled()) {
			log.info("systemId: " + systemId);
		}
		
		User user = getUserContext().becomeUser(systemId);
		
		// if assuming identity procedure finished successfully, we should change context locale parameter
		Locale locale = null;
		if (user.getUserProperties().containsKey(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)) {
			String localeString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE);
			locale = LocaleUtility.fromSpecification(localeString);
		}
		// when locale parameter is not valid or does not exist
		if (locale == null) {
			locale = LocaleUtility.getDefaultLocale();
		}
		Context.setLocale(locale);
	}
	
	/**
	 * Get the runtime properties that this OpenMRS instance was started with
	 * 
	 * @return copy of the runtime properties
	 */
	public static Properties getRuntimeProperties() {
		if (log.isTraceEnabled()) {
			log.trace("getting runtime properties. size: " + runtimeProperties.size());
		}
		
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
	 * @return note service
	 */
	public static NoteService getNoteService() {
		return getServiceContext().getNoteService();
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
	 * @return serialization service
	 * @since 1.5
	 */
	public static SerializationService getSerializationService() {
		return getServiceContext().getSerializationService();
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
			if (ms.getMessagePreparator() == null) {
				ms.setMessagePreparator(getMessagePreparator());
			}
			
			if (ms.getMessageSender() == null) {
				ms.setMessageSender(getMessageSender());
			}
			
		}
		catch (Exception e) {
			log.error("Unable to create message service due", e);
		}
		return ms;
	}
	
	/**
	 * @return active list service
	 */
	public static ActiveListService getActiveListService() {
		return getServiceContext().getActiveListService();
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
				
				@Override
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
	 * @return the ServiceContext
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
		if (Daemon.isDaemonThread()) {
			return contextDAO.getUserByUuid(Daemon.DAEMON_USER_UUID);
		}
		
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
	 * @should not fail if session hasnt been opened yet
	 */
	public static void logout() {
		if (!isSessionOpen()) {
			return; // fail early if there isn't even a session open
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Logging out : " + getAuthenticatedUser());
		}
		
		getUserContext().logout();
		
		// reset the UserContext object (usually cleared out by closeSession()
		// soon after this)
		setUserContext(new UserContext());
	}
	
	/**
	 * Convenience method. Passes through to userContext.getAllRoles(User)
	 */
	public static Set<Role> getAllRoles(User user) throws Exception {
		return getUserContext().getAllRoles();
	}
	
	/**
	 * Convenience method. Passes through to userContext.hasPrivilege(String)
	 * 
	 * @should give daemon user full privileges
	 */
	public static boolean hasPrivilege(String privilege) {
		
		// the daemon threads have access to all things
		if (Daemon.isDaemonThread()) {
			return true;
		}
		
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
		if (!hasPrivilege(privilege)) {
			String errorMessage = null;
			if (StringUtils.isNotBlank(privilege)) {
				errorMessage = Context.getMessageSourceService().getMessage("error.privilegesRequired",
				    new Object[] { privilege }, null);
			} else {
				//Should we even be here if the privilege is blank?
				errorMessage = Context.getMessageSourceService().getMessage("error.privilegesRequiredNoArgs");
			}
			
			throw new ContextAuthenticationException(errorMessage);
		}
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
	 * Convenience method. Passes through to {@link UserContext#getLocale()}
	 * 
	 * @should not fail if session hasnt been opened
	 */
	public static Locale getLocale() {
		// if a session hasn't been opened, just fetch the default
		if (!isSessionOpen()) {
			return LocaleUtility.getDefaultLocale();
		}
		
		return getUserContext().getLocale();
	}
	
	/**
	 * Used to define a unit of work. All "units of work" should be surrounded by openSession and
	 * closeSession calls.
	 */
	public static void openSession() {
		log.trace("opening session");
		setUserContext(new UserContext()); // must be cleared out in
		// closeSession()
		getContextDAO().openSession();
	}
	
	/**
	 * Used to define a unit of work. All "units of work" should be surrounded by openSession and
	 * closeSession calls.
	 */
	public static void closeSession() {
		log.trace("closing session");
		clearUserContext(); // because we set a UserContext on the current
		// thread in openSession()
		getContextDAO().closeSession();
	}
	
	/**
	 * Used to define a unit of work which does not require clearing out the currently authenticated
	 * user. Remember to call closeSessionWithCurrentUser in a, preferably, finally block after this
	 * work.
	 * 
	 * @since 1.10
	 */
	public static void openSessionWithCurrentUser() {
		getContextDAO().openSession();
	}
	
	/**
	 * Used when the a unit of work which started with a call for openSessionWithCurrentUser has
	 * finished. This should be in a, preferably, finally block.
	 * 
	 * @since 1.10
	 */
	public static void closeSessionWithCurrentUser() {
		getContextDAO().closeSession();
		;
	}
	
	/**
	 * Clears cached changes made so far during this unit of work without writing them to the
	 * database. If you call this method, and later call closeSession() or flushSession() your
	 * changes are still lost.
	 */
	public static void clearSession() {
		log.trace("clearing session");
		getContextDAO().clearSession();
	}
	
	/**
	 * Forces any changes made so far in this unit of work to be written to the database
	 * 
	 * @since 1.6
	 */
	public static void flushSession() {
		log.trace("flushing session");
		getContextDAO().flushSession();
	}
	
	/**
	 * This method tells whether {@link #openSession()} has been called or not already. If it hasn't
	 * been called, some methods won't work correctly because a {@link UserContext} isn't available.
	 * 
	 * @return true if {@link #openSession()} has been called already.
	 * @since 1.5
	 * @should return true if session is closed
	 */
	public static boolean isSessionOpen() {
		return userContextHolder.get() != null;
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
	 * @param props Runtime properties to use for startup
	 * @throws InputRequiredException if the {@link DatabaseUpdater} has determined that updates
	 *             cannot continue without input from the user
	 * @throws DatabaseUpdateException if database updates are required, see
	 *             {@link DatabaseUpdater#executeChangelog()}
	 * @throws ModuleMustStartException if a module that should be started is not able to
	 * @see InputRequiredException#getRequiredInput() InputRequiredException#getRequiredInput() for
	 *      the required question/datatypes
	 */
	public static void startup(Properties props) throws DatabaseUpdateException, InputRequiredException,
	        ModuleMustStartException {
		// do any context database specific startup
		getContextDAO().startup(props);
		
		// find/set/check whether the current database version is compatible
		checkForDatabaseUpdates(props);
		
		// this should be first in the startup routines so that the application
		// data directory can be set from the runtime properties
		OpenmrsUtil.startup(props);
		
		// Loop over each module and startup each with these custom properties
		ModuleUtil.startup(props);
		
		// add any privileges/roles that /must/ exist for openmrs to work
		// correctly.
		// TODO: Should this be one of the first things executed at startup?
		checkCoreDataset();
		
		getContextDAO().setupSearchIndex();
	}
	
	/**
	 * Starts the OpenMRS System in a _non-webapp_ environment<br/>
	 * <br/>
	 * If an {@link InputRequiredException} is thrown, a call to {@link DatabaseUpdater#update(Map)}
	 * will be required with a mapping from question prompt to user answer before startup can be
	 * called again. <br/>
	 * <br/>
	 * <b>Note:</b> This method calls {@link Context#openSession()}, so you must call
	 * {@link Context#closeSession()} somewhere on the same thread of this application so as to not
	 * leak memory.
	 * 
	 * @param url database url like "jdbc:mysql://localhost:3306/openmrs?autoReconnect=true"
	 * @param username Connection username
	 * @param password Connection password
	 * @param properties Other startup properties
	 * @throws InputRequiredException if the {@link DatabaseUpdater} has determined that updates
	 *             cannot continue without input from the user
	 * @throws DatabaseUpdateException if the database must be updated. See {@link DatabaseUpdater}
	 * @throws ModuleMustStartException if a module that should start is not able to
	 * @see #startup(Properties)
	 * @see InputRequiredException#getRequiredInput() InputRequiredException#getRequiredInput() for
	 *      the required question/datatypes
	 */
	public static void startup(String url, String username, String password, Properties properties)
	        throws DatabaseUpdateException, InputRequiredException, ModuleMustStartException {
		if (properties == null) {
			properties = new Properties();
		}
		
		properties.put("connection.url", url);
		properties.put("connection.username", username);
		properties.put("connection.password", password);
		setRuntimeProperties(properties);
		
		openSession(); // so that the startup method can use proxyPrivileges
		
		startup(properties);
		
		// start the scheduled tasks
		SchedulerUtil.startup(properties);
		
		closeSession();
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
			if (dao != null) {
				dao.shutdown();
			}
		}
		catch (Exception e) {
			log.warn("Error while shutting down context dao", e);
		}
	}
	
	/**
	 * Used for getting services not in the previous get*Service() calls
	 * 
	 * @param cls The Class of the service to get
	 * @return The requested Service
	 * @should return the same object when called multiple times for the same class
	 */
	public static <T extends Object> T getService(Class<? extends T> cls) {
		return getServiceContext().getService(cls);
	}
	
	/**
	 * Adds an AOP advisor around the given Class <code>cls</code>
	 * <p>
	 * Advisors can wrap around a method and effect the method before or after
	 * 
	 * @param cls
	 * @param advisor
	 */
	@SuppressWarnings("unchecked")
	public static void addAdvisor(Class cls, Advisor advisor) {
		getServiceContext().addAdvisor(cls, advisor);
	}
	
	/**
	 * Adds an AOP advice object around the given Class <code>cls</code>
	 * <p>
	 * Advice comes in the form of before or afterReturning methods
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
	 * Runs through the core data (e.g. privileges, roles, and global properties) and adds them if
	 * necessary.
	 */
	public static void checkCoreDataset() {
		// setting core roles
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
			Set<String> currentRoleNames = new HashSet<String>();
			for (Role role : Context.getUserService().getAllRoles()) {
				currentRoleNames.add(role.getRole().toUpperCase());
			}
			Map<String, String> map = OpenmrsUtil.getCoreRoles();
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String roleName = entry.getKey();
				if (!currentRoleNames.contains(roleName.toUpperCase())) {
					Role role = new Role();
					role.setRole(roleName);
					role.setDescription(entry.getValue());
					Context.getUserService().saveRole(role);
				}
			}
		}
		catch (Exception e) {
			log.error("Error while setting core roles for openmrs system", e);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		}
		
		// setting core privileges
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_PRIVILEGES);
			Set<String> currentPrivilegeNames = new HashSet<String>();
			for (Privilege privilege : Context.getUserService().getAllPrivileges()) {
				currentPrivilegeNames.add(privilege.getPrivilege().toUpperCase());
			}
			Map<String, String> map = OpenmrsUtil.getCorePrivileges();
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String privilegeName = entry.getKey();
				if (!currentPrivilegeNames.contains(privilegeName.toUpperCase())) {
					Privilege p = new Privilege();
					p.setPrivilege(privilegeName);
					p.setDescription(entry.getValue());
					Context.getUserService().savePrivilege(p);
				}
			}
		}
		catch (Exception e) {
			log.error("Error while setting core privileges", e);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_PRIVILEGES);
		}
		
		// setting core global properties
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
			Context.addProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
			Set<String> currentPropNames = new HashSet<String>();
			Map<String, GlobalProperty> propsMissingDescription = new HashMap<String, GlobalProperty>();
			Map<String, GlobalProperty> propsMissingDatatype = new HashMap<String, GlobalProperty>();
			for (GlobalProperty prop : Context.getAdministrationService().getAllGlobalProperties()) {
				currentPropNames.add(prop.getProperty().toUpperCase());
				if (prop.getDescription() == null) {
					propsMissingDescription.put(prop.getProperty().toUpperCase(), prop);
				}
				if (prop.getDatatypeClassname() == null) {
					propsMissingDatatype.put(prop.getProperty().toUpperCase(), prop);
				}
			}
			
			for (GlobalProperty coreProp : OpenmrsConstants.CORE_GLOBAL_PROPERTIES()) {
				String corePropName = coreProp.getProperty().toUpperCase();
				// if the prop doesn't exist, save it
				if (!currentPropNames.contains(corePropName)) {
					Context.getAdministrationService().saveGlobalProperty(coreProp);
					currentPropNames.add(corePropName); // add to list in case
					// of duplicates
				} else {
					// if the prop is missing its description, update it
					GlobalProperty propToUpdate = propsMissingDescription.get(corePropName);
					if (propToUpdate != null) {
						propToUpdate.setDescription(coreProp.getDescription());
						Context.getAdministrationService().saveGlobalProperty(propToUpdate);
					}
					// set missing datatypes
					propToUpdate = propsMissingDatatype.get(corePropName);
					if (propToUpdate != null && coreProp.getDatatypeClassname() != null) {
						propToUpdate.setDatatypeClassname(coreProp.getDatatypeClassname());
						propToUpdate.setDatatypeConfig(coreProp.getDatatypeConfig());
						propToUpdate.setPreferredHandlerClassname(coreProp.getPreferredHandlerClassname());
						propToUpdate.setHandlerConfig(coreProp.getHandlerConfig());
						Context.getAdministrationService().saveGlobalProperty(propToUpdate);
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Error while setting core global properties", e);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
		}
	}
	
	/**
	 * Runs any needed updates on the current database if the user has the allow_auto_update runtime
	 * property set to true. If not set to true, then {@link #updateDatabase(Map)} must be called.<br/>
	 * <br/>
	 * If an {@link InputRequiredException} is thrown, a call to {@link #updateDatabase(Map)} is
	 * required with a mapping from question prompt to user answer.
	 * 
	 * @param props the runtime properties
	 * @throws InputRequiredException if the {@link DatabaseUpdater} has determined that updates
	 *             cannot continue without input from the user
	 * @see InputRequiredException#getRequiredInput() InputRequiredException#getRequiredInput() for
	 *      the required question/datatypes
	 */
	private static void checkForDatabaseUpdates(Properties props) throws DatabaseUpdateException, InputRequiredException {
		boolean updatesRequired = true;
		try {
			updatesRequired = DatabaseUpdater.updatesRequired();
		}
		catch (Exception e) {
			throw new DatabaseUpdateException("Unable to check if database updates are required", e);
		}
		
		// this must be the first thing run in case it changes database mappings
		if (updatesRequired) {
			if (DatabaseUpdater.allowAutoUpdate()) {
				DatabaseUpdater.executeChangelog();
			} else {
				throw new DatabaseUpdateException(
				        "Database updates are required.  Call Context.updateDatabase() before .startup() to continue.");
			}
		}
	}
	
	/**
	 * Updates the openmrs database to the latest. This is only needed if using the API alone. <br/>
	 * <br/>
	 * The typical use-case would be: Try to {@link #startup(String, String, String, Properties)},
	 * if that fails, call this method to get the database up to speed.
	 * 
	 * @param userInput (can be null) responses from the user about needed input
	 * @throws DatabaseUpdateException if an error occurred while updating
	 * @throws InputRequiredException if user input is required
	 * @since 1.5
	 */
	public static void updateDatabase(Map<String, Object> userInput) throws DatabaseUpdateException, InputRequiredException {
		DatabaseUpdater.executeChangelog(null, userInput);
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
	@Deprecated
	public static Object getVolatileUserData(String key) {
		User u = getAuthenticatedUser();
		if (u == null) {
			throw new APIAuthenticationException();
		}
		Map<String, Object> myData = volatileUserData.get(u);
		if (myData == null) {
			return null;
		} else {
			return myData.get(key);
		}
	}
	
	/**
	 * Set a piece of information for the currently authenticated user. This information is stored
	 * only temporarily. When a new module is loaded or the server is restarted, this information
	 * will disappear
	 * 
	 * @param key identifying string for this information
	 * @param value information to be stored
	 */
	@Deprecated
	public static void setVolatileUserData(String key, Object value) {
		User u = getAuthenticatedUser();
		if (u == null) {
			throw new APIAuthenticationException();
		}
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
	 * @return SimpleDateFormat for the user's current locale
	 * @see org.openmrs.util.OpenmrsUtil#getDateFormat(Locale)
	 * @should return a pattern with four y characters in it
	 */
	public static SimpleDateFormat getDateFormat() {
		return OpenmrsUtil.getDateFormat(getLocale());
	}
	
	/**
	 * Gets the simple time format for the current user's locale. The format will be similar to
	 * hh:mm a
	 * 
	 * @return SimpleDateFormat for the user's current locale
	 * @see org.openmrs.util.OpenmrsUtil#getTimeFormat(Locale)
	 * @should return a pattern with two h characters in it
	 */
	public static SimpleDateFormat getTimeFormat() {
		return OpenmrsUtil.getTimeFormat(getLocale());
	}
	
	/**
	 * Gets the simple datetime format for the current user's locale. The format will be similar to
	 * mm/dd/yyyy hh:mm a
	 * 
	 * @return SimpleDateFormat for the user's current locale
	 * @see org.openmrs.util.OpenmrsUtil#getDateTimeFormat(Locale)
	 * @should return a pattern with four y characters and two h characters in it
	 */
	public static SimpleDateFormat getDateTimeFormat() {
		return OpenmrsUtil.getDateTimeFormat(getLocale());
	}
	
	/**
	 * @return true/false whether the service context is currently being refreshed
	 * @see org.openmrs.api.context.ServiceContext#isRefreshingContext()
	 */
	public static boolean isRefreshingContext() {
		return getServiceContext().isRefreshingContext();
	}
	
	/**
	 * @since 1.5
	 * @see ServiceContext#getRegisteredComponents(Class)
	 */
	public static <T extends Object> List<T> getRegisteredComponents(Class<T> type) {
		return getServiceContext().getRegisteredComponents(type);
	}
	
	/**
	 * @see ServiceContext#getRegisteredComponent(String, Class)
	 * @since 1.9.4
	 */
	public static <T> T getRegisteredComponent(String beanName, Class<T> type) throws APIException {
		return getServiceContext().getRegisteredComponent(beanName, type);
	}
	
	/**
	 * @see ServiceContext#getModuleOpenmrsServices(String)
	 * @since 1.9
	 */
	public static List<OpenmrsService> getModuleOpenmrsServices(String modulePackage) {
		return getServiceContext().getModuleOpenmrsServices(modulePackage);
	}
	
	/**
	 * @since 1.9
	 * @see ServiceContext#getVisitService()
	 */
	public static VisitService getVisitService() {
		return getServiceContext().getVisitService();
	}
	
	/**
	 * @since 1.9
	 * @see ServiceContext#getProviderService()
	 */
	public static ProviderService getProviderService() {
		return getServiceContext().getProviderService();
	}
	
	/**
	 * @since 1.9
	 * @see ServiceContext#getDatatypeService()
	 */
	public static DatatypeService getDatatypeService() {
		return getServiceContext().getDatatypeService();
	}
	
	/**
	 * Add or replace a property in the config properties list
	 * 
	 * @param key name of the property
	 * @param value value of the property
	 * @since 1.9
	 */
	public static void addConfigProperty(Object key, Object value) {
		configProperties.put(key, value);
	}
	
	/**
	 * Remove a property from the list of config properties
	 * 
	 * @param key name of the property
	 * @since 1.9
	 */
	public static void removeConfigProperty(Object key) {
		configProperties.remove(key);
	}
	
	/**
	 * Get the config properties that have been added to this OpenMRS instance
	 * 
	 * @return copy of the module properties
	 * @since 1.9
	 */
	public static Properties getConfigProperties() {
		Properties props = new Properties();
		props.putAll(configProperties);
		return props;
	}
	
	/**
	 * Updates the search index. It is a blocking operation, which may take even a few minutes
	 * depending on the index size.
	 * <p>
	 * There is no need to call this method in normal usage since the index is automatically updated
	 * whenever DB transactions are committed.
	 * <p>
	 * The method is designated to be used in tests, which rollback transactions. Note that if the
	 * transaction is rolled back, changes to the index will not be reverted.
	 * 
	 * @since 1.11
	 */
	public static void updateSearchIndex() {
		getContextDAO().updateSearchIndex();
	}
	
	/**
	 * Updates the search index for objects of the given type.
	 * 
	 * @see #updateSearchIndex()
	 * @param type
	 * @since 1.11
	 */
	public static void updateSearchIndexForType(Class<?> type) {
		getContextDAO().updateSearchIndexForType(type);
	}
	
	/**
	 * Updates the search index for the given object.
	 * 
	 * @see #updateSearchIndex()
	 * @param object
	 * @since 1.11
	 */
	public static void updateSearchIndexForObject(Object object) {
		getContextDAO().updateSearchIndexForObject(object);
	}
	
	/**
	 * @see org.openmrs.api.context.ServiceContext#setUseSystemClassLoader(boolean)
	 * @since 1.10
	 */
	public static void setUseSystemClassLoader(boolean useSystemClassLoader) {
		getServiceContext().setUseSystemClassLoader(useSystemClassLoader);
	}
	
	/**
	 * @see org.openmrs.api.context.ServiceContext#isUseSystemClassLoader()
	 * @since 1.10
	 */
	public static boolean isUseSystemClassLoader() {
		return getServiceContext().isUseSystemClassLoader();
	}
}
