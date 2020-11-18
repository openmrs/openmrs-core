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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.aopalliance.aop.Advice;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Allergen;
import org.openmrs.GlobalProperty;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConditionService;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.OrderSetService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.SerializationService;
import org.openmrs.api.UserService;
import org.openmrs.api.VisitService;
import org.openmrs.api.db.ContextDAO;
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
import org.openmrs.validator.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;

/**
 * Represents an OpenMRS <code>Context</code>, which may be used to authenticate to the database and
 * obtain services in order to interact with the system.<br>
 * <br>
 * The Context is split into a {@link UserContext} and {@link ServiceContext}. The UserContext is
 * lightweight and there is an instance for every user logged into the system. The ServiceContext is
 * heavier and it contains each service class. This is more static and there is only one ServiceContext
 * per OpenMRS instance. <br>
 * <br>
 * Both the {@link UserContext} and the {@link ServiceContext} should not be used directly. This
 * context class has methods to pass through to the currently defined UserContext for the thread and
 * the currently defined ServiceContext. <br>
 * <br>
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
 * <br>
 * Example usage:
 *
 * <pre>
 * 	public static void main(String[] args) {
 * 		Context.startup("jdbc:mysql://localhost:3306/db-name?autoReconnect=true", "openmrs-db-user", "3jknfjkn33ijt", new Properties());
 * 		try {
 * 			Context.openSession();
 * 			Context.authenticate("admin", "test");
 * 			List&lt;Patients&gt; patients = Context.getPatientService().getPatientsByName("Fred");
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

	private static final Logger log = LoggerFactory.getLogger(Context.class);

	// Global resources
	private static ContextDAO contextDAO;

	private static Session mailSession;

	// Using "wrapper" (Object array) around UserContext to avoid ThreadLocal
	// bug in Java 1.5
	private static final ThreadLocal<Object[] /* UserContext */> userContextHolder = new ThreadLocal<>();

	private static ServiceContext serviceContext;

	private static Properties runtimeProperties = new Properties();

	private static Properties configProperties = new Properties();

	private static AuthenticationScheme authenticationScheme;

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
	static ContextDAO getContextDAO() {
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
	 * Spring init method that sets the authentication scheme.
	 */
	private static void setAuthenticationScheme() {

		authenticationScheme = new UsernamePasswordAuthenticationScheme();

		try {
			authenticationScheme = Context.getServiceContext().getApplicationContext().getBean(AuthenticationScheme.class); // manual autowiring (from a module)
			log.info("An authentication scheme override was provided. Using this one in place of the OpenMRS default authentication scheme.");
		}
		catch(NoUniqueBeanDefinitionException e) {
			log.error("Multiple authentication schemes overrides are being provided, this is currently not supported. Sticking to OpenMRS default authentication scheme.");
		}
		catch(NoSuchBeanDefinitionException e) {
			log.debug("No authentication scheme override was provided. Sticking to OpenMRS default authentication scheme.");
		}
		catch(BeansException e){
			log.error("Fatal error encountered when injecting the authentication scheme override. Sticking to OpenMRS default authentication scheme.");
		}
	}

	/**
	 * Loads a class with an instance of the OpenmrsClassLoader. Convenience method equivalent to
	 * OpenmrsClassLoader.getInstance().loadClass(className);
	 *
	 * @param className the class to load
	 * @return the class that was loaded
	 * @throws ClassNotFoundException
	 * <strong>Should</strong> load class with the OpenmrsClassLoader
	 */
	public static Class<?> loadClass(String className) throws ClassNotFoundException {
		return OpenmrsClassLoader.getInstance().loadClass(className);
	}

	/**
	 * Sets the user context on the thread local so that the service layer can perform
	 * authentication/authorization checks.<br>
	 * <br>
	 * This is thread safe since it stores the given user context in ThreadLocal.
	 *
	 * @param ctx UserContext to set
	 */
	public static void setUserContext(UserContext ctx) {
		log.trace("Setting user context {}", ctx);

		Object[] arr = new Object[] { ctx };
		userContextHolder.set(arr);
	}

	/**
	 * Clears the user context from the threadlocal.
	 */
	public static void clearUserContext() {
		log.trace("Clearing user context {}", Arrays.toString(userContextHolder.get()));

		userContextHolder.remove();
	}

	/**
	 * Gets the user context from the thread local. This might be accessed by several threads at the
	 * same time.
	 *
	 * @return The current UserContext for this thread.
	 * <strong>Should</strong> fail if session hasn't been opened
	 */
	public static UserContext getUserContext() {
		Object[] arr = userContextHolder.get();
		log.trace("Getting user context {} from userContextHolder {}", Arrays.toString(arr), userContextHolder);

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
			synchronized (Context.class) {
				if (serviceContext == null) {
					log.error("serviceContext is null.  Creating new ServiceContext()");
					serviceContext = ServiceContext.getInstance();
				}
			}
		}
		log.trace("serviceContext: {}", serviceContext);

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
	 * OpenMRS provides its default authentication scheme that authenticates via DAO with OpenMRS usernames and passwords.
	 * 
	 * Any module can provide an authentication scheme override by Spring wiring a custom implementation of {@link AuthenticationScheme}.
	 * This method would return Core's default authentication scheme unless a Spring override is provided somewhere else.
	 * 
	 * @return The enforced authentication scheme.
	 */
	public static AuthenticationScheme getAuthenticationScheme() {
		return authenticationScheme;
	}

	/**
	 * @deprecated as of 2.3.0, replaced by {@link #authenticate(Credentials)}
	 * 
	 * Used to authenticate user within the context
	 *
	 * @param username user's identifier token for login
	 * @param password user's password for authenticating to context
	 * @throws ContextAuthenticationException
	 * <strong>Should</strong> not authenticate with null username and password
	 * <strong>Should</strong> not authenticate with null password
	 * <strong>Should</strong> not authenticate with null username
	 * <strong>Should</strong> not authenticate with null password and proper username
	 * <strong>Should</strong> not authenticate with null password and proper system id
	 */
	@Deprecated
	public static void authenticate(String username, String password) throws ContextAuthenticationException {
		authenticate(new UsernamePasswordCredentials(username, password));
	}

	/**
	 * @param credentials
	 * @throws ContextAuthenticationException
	 * 
	 * @since 2.3.0
	 */
	public static Authenticated authenticate(Credentials credentials) throws ContextAuthenticationException {

		if (Daemon.isDaemonThread()) {
			log.error("Authentication attempted while operating on a "
					+ "daemon thread, authenticating is not necessary or allowed");
			return new BasicAuthenticated(Daemon.getDaemonThreadUser(), "No auth scheme used by Context - Daemon user is always authenticated.");
		}

		if (credentials == null) {
			throw new ContextAuthenticationException("Context cannot authenticate with null credentials.");
		}

		return getUserContext().authenticate(credentials);
	}

	/**
	 * Refresh the authenticated user object in the current UserContext. This should be used when
	 * updating information in the database about the current user and it needs to be reflecting in
	 * the (cached) {@link #getAuthenticatedUser()} User object.
	 *
	 * @since 1.5
	 * <strong>Should</strong> get fresh values from the database
	 */
	public static void refreshAuthenticatedUser() {
		if (Daemon.isDaemonThread()) {
			return;
		}
		log.debug("Refreshing authenticated user");

		getUserContext().refreshAuthenticatedUser();
	}

	/**
	 * Become a different user. (You should only be able to do this as a superuser.)
	 *
	 * @param systemId
	 * @throws ContextAuthenticationException
	 * <strong>Should</strong> change locale when become another user
	 */
	public static void becomeUser(String systemId) throws ContextAuthenticationException {
		log.info("systemId: {}", systemId);

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
		log.trace("getting runtime properties. size: {}", runtimeProperties.size());

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
	 * @return condition-related services
	 * 
	 * @since 2.2
	 */
	public static ConditionService getConditionService(){
		return getServiceContext().getConditionService();
	}

	/**
	 * @return diagnosis-related services
	 *
	 * @since 2.2
	 */
	public static DiagnosisService getDiagnosisService(){
		return getServiceContext().getDiagnosisService();
	}

	/**
	 * @return Returns the hl7Service.
	 */
	public static HL7Service getHL7Service() {
		return getServiceContext().getHL7Service();
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
	 * @return orderSet service
	 * @since 1.12
	 */
	public static OrderSetService getOrderSetService() {
		return getServiceContext().getOrderSetService();
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
	 * Gets the mail session required by the mail message service. This function forces
	 * authentication via the getAdministrationService() method call
	 *
	 * @return a java mail session
	 */
	private static Session getMailSession() {
		if (mailSession == null) {
			synchronized (Context.class) {
				if (mailSession == null) {
					AdministrationService adminService = getAdministrationService();

					Properties props = new Properties();
					props.setProperty("mail.transport.protocol", adminService.getGlobalProperty("mail.transport_protocol"));
					props.setProperty("mail.smtp.host", adminService.getGlobalProperty("mail.smtp_host"));
					props.setProperty("mail.smtp.port", adminService.getGlobalProperty("mail.smtp_port"));
					props.setProperty("mail.from", adminService.getGlobalProperty("mail.from"));
					props.setProperty("mail.debug", adminService.getGlobalProperty("mail.debug"));
					props.setProperty("mail.smtp.auth", adminService.getGlobalProperty("mail.smtp_auth"));
					props.setProperty(OpenmrsConstants.GP_MAIL_SMTP_STARTTLS_ENABLE, adminService.getGlobalProperty(OpenmrsConstants.GP_MAIL_SMTP_STARTTLS_ENABLE));

					Authenticator auth = new Authenticator() {

						@Override
						public PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(getAdministrationService().getGlobalProperty("mail.user"),
									getAdministrationService().getGlobalProperty("mail.password"));
						}
					};

					mailSession = Session.getInstance(props, auth);
				}
			}

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
			return Daemon.getDaemonThreadUser();
		}

		return getUserContext().getAuthenticatedUser();
	}

	/**
	 * @return true if user has been authenticated in this context
	 */
	public static boolean isAuthenticated() {
		if (Daemon.isDaemonThread()) {
			return true;
		} else {
			return getAuthenticatedUser() != null;
		}
	}

	/**
	 * logs out the "active" (authenticated) user within context
	 *
	 * @see #authenticate
	 * <strong>Should</strong> not fail if session hasn't been opened yet
	 */
	public static void logout() {
		if (!isSessionOpen()) {
			return; // fail early if there isn't even a session open
		}
		log.debug("Logging out : {}", getAuthenticatedUser());

		getUserContext().logout();

		// reset the UserContext object (usually cleared out by closeSession()
		// soon after this)
		setUserContext(new UserContext(getAuthenticationScheme()));
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
	 * <strong>Should</strong> give daemon user full privileges
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
			String errorMessage;
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
	 * <strong>Should</strong> not fail if session hasn't been opened
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
		setUserContext(new UserContext(getAuthenticationScheme())); // must be cleared out in
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
	 * <strong>Should</strong> return true if session is closed
	 */
	public static boolean isSessionOpen() {
		return userContextHolder.get() != null;
	}

	/**
	 * Used to re-read the state of the given instance from the underlying database.
	 * @since 2.0
	 * @param obj The object to refresh from the database in the session
	 */
	public static void refreshEntity(Object obj) {
		log.trace("refreshing object: {}", obj);
		getContextDAO().refreshEntity(obj);
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
	 * Starts the OpenMRS System Should be called prior to any kind of activity
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
	public static synchronized void startup(Properties props) throws DatabaseUpdateException, InputRequiredException,
	ModuleMustStartException {
		// do any context database specific startup
		getContextDAO().startup(props);

		// find/set/check whether the current database version is compatible
		checkForDatabaseUpdates(props);

		// this should be first in the startup routines so that the application
		// data directory can be set from the runtime properties
		OpenmrsUtil.startup(props);

		openSession();
		clearSession();

		// add any privileges/roles that /must/ exist for openmrs to work
		// correctly.
		checkCoreDataset();

		getContextDAO().setupSearchIndex();

		// Loop over each module and startup each with these custom properties
		ModuleUtil.startup(props);
	}

	/**
	 * Starts the OpenMRS System in a _non-webapp_ environment<br>
	 * <br>
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
	public static synchronized void startup(String url, String username, String password, Properties properties)
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
	 * <strong>Should</strong> return the same object when called multiple times for the same class
	 */
	public static <T> T getService(Class<? extends T> cls) {
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
	 * Runs through the core data (e.g. privileges, roles, and global properties) and adds them if
	 * necessary.
	 */
	public static void checkCoreDataset() {
		// setting core roles
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
			Set<String> currentRoleNames = new HashSet<>();
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
			Set<String> currentPrivilegeNames = new HashSet<>();
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
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			Set<String> currentPropNames = new HashSet<>();
			Map<String, GlobalProperty> propsMissingDescription = new HashMap<>();
			Map<String, GlobalProperty> propsMissingDatatype = new HashMap<>();
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
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}

		// setting default validation rule
		AdministrationService as = Context.getAdministrationService();
		Boolean disableValidation = Boolean.valueOf(as.getGlobalProperty(OpenmrsConstants.GP_DISABLE_VALIDATION, "false"));
		ValidateUtil.setDisableValidation(disableValidation);

		PersonName.setFormat(Context.getAdministrationService().getGlobalProperty(
				OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT));

		Allergen.setOtherNonCodedConceptUuid(Context.getAdministrationService().getGlobalProperty(
				OpenmrsConstants.GP_ALLERGEN_OTHER_NON_CODED_UUID));
	}

	/**
	 * Runs any needed updates on the current database if the user has the allow_auto_update runtime
	 * property set to true. If not set to true, then {@link #updateDatabase(Map)} must be called.<br>
	 * <br>
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
		boolean updatesRequired;
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
	 * Updates the openmrs database to the latest. This is only needed if using the API alone. <br>
	 * <br>
	 * The typical use-case would be: Try to {@link #startup(String, String, String, Properties)},
	 * if that fails, call this method to get the database up to speed.
	 *
	 * @param userInput (can be null) responses from the user about needed input
	 * @throws DatabaseUpdateException if an error occurred while updating
	 * @since 1.5
	 * @deprecated as of 2.4
	 * 
	 */
	@Deprecated
	public static void updateDatabase(Map<String, Object> userInput) throws DatabaseUpdateException {
		throw new UnsupportedOperationException("As of 2.4, this method is not longer implemented");
	}

	/**
	 * Gets the simple date format for the current user's locale. The format will be similar in size
	 * to mm/dd/yyyy
	 *
	 * @return SimpleDateFormat for the user's current locale
	 * @see org.openmrs.util.OpenmrsUtil#getDateFormat(Locale)
	 * <strong>Should</strong> return a pattern with four y characters in it
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
	 * <strong>Should</strong> return a pattern with two h characters in it
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
	 * <strong>Should</strong> return a pattern with four y characters and two h characters in it
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
	public static <T> List<T> getRegisteredComponents(Class<T> type) {
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
	 * Updates the search index. It is an asynchronous operation.
	 * <p>
	 * There is no need to call this method in normal usage since the index is automatically updated
	 * whenever DB transactions are committed.
	 * <p>
	 *
	 * @return object representing the result of the started asynchronous operation
	 */
	public static Future<?> updateSearchIndexAsync() {
		return getContextDAO().updateSearchIndexAsync();
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
