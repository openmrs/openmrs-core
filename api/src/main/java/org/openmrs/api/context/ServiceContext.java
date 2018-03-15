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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.openmrs.api.APIException;
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
import org.openmrs.api.OrderSetService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.SerializationService;
import org.openmrs.api.UserService;
import org.openmrs.api.VisitService;
import org.openmrs.api.ConditionService;
import org.openmrs.api.DiagnosisService;
import org.openmrs.hl7.HL7Service;
import org.openmrs.logic.LogicService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.messagesource.impl.DefaultMessageSourceServiceImpl;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.MessageService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.util.OpenmrsClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Represents an OpenMRS <code>Service Context</code>, which returns the services represented
 * throughout the system. <br>
 * <br>
 * This class should not be access directly, but rather used through the <code>Context</code> class. <br>
 * <br>
 * This class is essentially static and only one instance is kept because this is fairly
 * heavy-weight. Spring takes care of filling in the actual service implementations via dependency
 * injection. See the /metadata/api/spring/applicationContext-service.xml file. <br>
 * <br>
 * Module services are also accessed through this class. See {@link #getService(Class)}
 *
 * @see org.openmrs.api.context.Context
 */
public class ServiceContext implements ApplicationContextAware {
	
	private static final Logger log = LoggerFactory.getLogger(ServiceContext.class);

	private ApplicationContext applicationContext;
	
	private static boolean refreshingContext = false;
	
	private static final Object refreshingContextLock = new Object();
	
	/**
	 * Static variable holding whether or not to use the system classloader. By default this is
	 * false so the openmrs classloader is used instead
	 */
	private boolean useSystemClassLoader = false;
	
	// Cached service objects
	Map<Class, Object> services = new HashMap<>();
	
	// Advisors added to services by this service
	Map<Class, Set<Advisor>> addedAdvisors = new HashMap<>();
	
	// Advice added to services by this service
	Map<Class, Set<Advice>> addedAdvice = new HashMap<>();
	
	/**
	 * Services implementing the OpenmrsService interface for each module. The map is keyed by the
	 * full class name including package.
	 *
	 * @since 1.9
	 */
	Map<String, OpenmrsService> moduleOpenmrsServices = new HashMap<>();
	
	/**
	 * The default constructor is private so as to keep only one instance per java vm.
	 *
	 * @see ServiceContext#getInstance()
	 */
	private ServiceContext() {
		log.debug("Instantiating service context");
	}
	
	private static class ServiceContextHolder {

		private ServiceContextHolder() {
		}

		private static ServiceContext instance = null;
	}
	
	/**
	 * There should only be one ServiceContext per openmrs (java virtual machine). This method
	 * should be used when wanting to fetch the service context Note: The ServiceContext shouldn't
	 * be used independently. All calls should go through the Context
	 *
	 * @return This VM's current ServiceContext.
	 * @see org.openmrs.api.context.Context
	 */
	public static ServiceContext getInstance() {
		if (ServiceContextHolder.instance == null) {
			ServiceContextHolder.instance = new ServiceContext();
		}
		
		return ServiceContextHolder.instance;
	}
	
	/**
	 * Null out the current instance of the ServiceContext. This should be used when modules are
	 * refreshing (being added/removed) and/or openmrs is shutting down
	 */
	public static void destroyInstance() {
		if (ServiceContextHolder.instance != null && ServiceContextHolder.instance.services != null) {
			if (log.isDebugEnabled()) {
				for (Map.Entry<Class, Object> entry : ServiceContextHolder.instance.services.entrySet()) {
					log.debug("Service - " + entry.getKey().getName() + ":" + entry.getValue());
				}
			}
			
			// Remove advice and advisors that this service added
			for (Class serviceClass : ServiceContextHolder.instance.services.keySet()) {
				ServiceContextHolder.instance.removeAddedAOP(serviceClass);
			}
			
			if (ServiceContextHolder.instance.services != null) {
				ServiceContextHolder.instance.services.clear();
				ServiceContextHolder.instance.services = null;
			}
			
			if (ServiceContextHolder.instance.addedAdvisors != null) {
				ServiceContextHolder.instance.addedAdvisors.clear();
				ServiceContextHolder.instance.addedAdvisors = null;
			}
			
			if (ServiceContextHolder.instance.addedAdvice != null) {
				ServiceContextHolder.instance.addedAdvice.clear();
				ServiceContextHolder.instance.addedAdvice = null;
			}
		}
		
		if (ServiceContextHolder.instance != null) {
			ServiceContextHolder.instance.applicationContext = null;
			
			if (ServiceContextHolder.instance.moduleOpenmrsServices != null) {
				ServiceContextHolder.instance.moduleOpenmrsServices.clear();
				ServiceContextHolder.instance.moduleOpenmrsServices = null;
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Destroying ServiceContext instance: " + ServiceContextHolder.instance);
		}
		
		ServiceContextHolder.instance = null;
	}
	
	/**
	 * @return encounter-related services
	 */
	public EncounterService getEncounterService() {
		return getService(EncounterService.class);
	}
	
	/**
	 * @return location services
	 */
	public LocationService getLocationService() {
		return getService(LocationService.class);
	}
	
	/**
	 * @return observation services
	 */
	public ObsService getObsService() {
		return getService(ObsService.class);
	}
	
	/**
	 * @return condition related service
	 * 
	 * @since 2.2
	 */
	public ConditionService getConditionService() {
		return getService(ConditionService.class);
	}
	
	/**
	 * @param conditionService condition related service
	 *            
	 * @since 2.2   
	 */
	public void setConditionService(ConditionService conditionService) {
		setService(ConditionService.class, conditionService);
	}

	/**
	 * @return diagnosis related service
	 *
	 * @since 2.2
	 */
	public DiagnosisService getDiagnosisService() {
		return getService(DiagnosisService.class);
	}

	/**
	 * @param diagnosisService diagnosis related service
	 *
	 * @since 2.2
	 */
	public void setDiagnosisService(DiagnosisService diagnosisService) {
		setService(DiagnosisService.class, diagnosisService);
	}
	
	/**
	 * @return cohort related service
	 */
	public CohortService getCohortService() {
		return getService(CohortService.class);
	}
	
	/**
	 * @param cs cohort related service
	 */
	public void setCohortService(CohortService cs) {
		setService(CohortService.class, cs);
	}
	
	/**
	 * @return order set service
	 */
	public OrderSetService getOrderSetService() {
		return getService(OrderSetService.class);
	}
	
	/**
	 * @return order service
	 */
	public OrderService getOrderService() {
		return getService(OrderService.class);
	}
	
	/**
	 * @return form service
	 */
	public FormService getFormService() {
		return getService(FormService.class);
	}
	
	/**
	 * @return serialization service
	 */
	public SerializationService getSerializationService() {
		return getService(SerializationService.class);
	}
	
	/**
	 * @return admin-related services
	 */
	public AdministrationService getAdministrationService() {
		return getService(AdministrationService.class);
	}
	
	/**
	 * @return programWorkflowService
	 */
	public ProgramWorkflowService getProgramWorkflowService() {
		return getService(ProgramWorkflowService.class);
	}

	/**
	 * @return logicService
	 */
	public LogicService getLogicService() {
		return getService(LogicService.class);
	}
	
	/**
	 * @return scheduler service
	 */
	public SchedulerService getSchedulerService() {
		return getService(SchedulerService.class);
	}
	
	/**
	 * Set the scheduler service.
	 *
	 * @param schedulerService
	 */
	public void setSchedulerService(SchedulerService schedulerService) {
		setService(SchedulerService.class, schedulerService);
	}
	
	/**
	 * @return alert service
	 */
	public AlertService getAlertService() {
		return getService(AlertService.class);
	}
	
	/**
	 * @param alertService
	 */
	public void setAlertService(AlertService alertService) {
		setService(AlertService.class, alertService);
	}
	
	/**
	 * @param programWorkflowService
	 */
	public void setProgramWorkflowService(ProgramWorkflowService programWorkflowService) {
		setService(ProgramWorkflowService.class, programWorkflowService);
	}

	/**
	 * @param logicService
	 */
	public void setLogicService(LogicService logicService) {
		setService(LogicService.class, logicService);
	}
	
	/**
	 * @return message service
	 */
	public MessageService getMessageService() {
		return getService(MessageService.class);
	}
	
	/**
	 * Sets the message service.
	 *
	 * @param messageService
	 */
	public void setMessageService(MessageService messageService) {
		setService(MessageService.class, messageService);
	}
	
	/**
	 * @return the hl7Service
	 */
	public HL7Service getHL7Service() {
		return getService(HL7Service.class);
	}
	
	/**
	 * @param hl7Service the hl7Service to set
	 */
	public void setHl7Service(HL7Service hl7Service) {
		setService(HL7Service.class, hl7Service);
	}
	
	/**
	 * @param administrationService the administrationService to set
	 */
	public void setAdministrationService(AdministrationService administrationService) {
		setService(AdministrationService.class, administrationService);
	}
	
	/**
	 * @param encounterService the encounterService to set
	 */
	public void setEncounterService(EncounterService encounterService) {
		setService(EncounterService.class, encounterService);
	}
	
	/**
	 * @param locationService the LocationService to set
	 */
	public void setLocationService(LocationService locationService) {
		setService(LocationService.class, locationService);
	}
	
	/**
	 * @param formService the formService to set
	 */
	public void setFormService(FormService formService) {
		setService(FormService.class, formService);
	}
	
	/**
	 * @param obsService the obsService to set
	 */
	public void setObsService(ObsService obsService) {
		setService(ObsService.class, obsService);
	}

	/**
	 * @param orderService the orderService to set
	 */
	public void setOrderService(OrderService orderService) {
		setService(OrderService.class, orderService);
	}
	
	/**
	 * @param orderSetService the orderSetService to set
	 */
	public void setOrderSetService(OrderSetService orderSetService) {
		setService(OrderSetService.class, orderSetService);
	}
	
	/**
	 * @param serializationService
	 */
	public void setSerializationService(SerializationService serializationService) {
		setService(SerializationService.class, serializationService);
	}
	
	/**
	 * @return patient related services
	 */
	public PatientService getPatientService() {
		return getService(PatientService.class);
	}
	
	/**
	 * @param patientService the patientService to set
	 */
	public void setPatientService(PatientService patientService) {
		setService(PatientService.class, patientService);
	}
	
	/**
	 * @return person related services
	 */
	public PersonService getPersonService() {
		return getService(PersonService.class);
	}
	
	/**
	 * @param personService the personService to set
	 */
	public void setPersonService(PersonService personService) {
		setService(PersonService.class, personService);
	}
	
	/**
	 * @return concept related services
	 */
	public ConceptService getConceptService() {
		return getService(ConceptService.class);
	}
	
	/**
	 * @param conceptService the conceptService to set
	 */
	public void setConceptService(ConceptService conceptService) {
		setService(ConceptService.class, conceptService);
	}
	
	/**
	 * @return user-related services
	 */
	public UserService getUserService() {
		return getService(UserService.class);
	}
	
	/**
	 * @param userService the userService to set
	 */
	public void setUserService(UserService userService) {
		setService(UserService.class, userService);
	}
	
	/**
	 * Gets the MessageSourceService used in the context.
	 *
	 * @return MessageSourceService
	 */
	public MessageSourceService getMessageSourceService() {
		try {
			return getService(MessageSourceService.class);
		}
		catch (APIException ex) {
			//must be a service not found exception because of spring not being started
			return DefaultMessageSourceServiceImpl.getInstance();
		}
	}
	
	/**
	 * Sets the MessageSourceService used in the context.
	 *
	 * @param messageSourceService the MessageSourceService to use
	 */
	public void setMessageSourceService(MessageSourceService messageSourceService) {
		setService(MessageSourceService.class, messageSourceService);
	}
	
	/**
	 * @param cls
	 * @param advisor
	 */
	public void addAdvisor(Class cls, Advisor advisor) {
		Advised advisedService = (Advised) services.get(cls);
		if (advisedService.indexOf(advisor) < 0) {
			advisedService.addAdvisor(advisor);
		}
		addedAdvisors.computeIfAbsent(cls, k -> new HashSet<>());
		getAddedAdvisors(cls).add(advisor);
	}
	
	/**
	 * @param cls
	 * @param advice
	 */
	public void addAdvice(Class cls, Advice advice) {
		Advised advisedService = (Advised) services.get(cls);
		if (advisedService.indexOf(advice) < 0) {
			advisedService.addAdvice(advice);
		}
		addedAdvice.computeIfAbsent(cls, k -> new HashSet<>());
		getAddedAdvice(cls).add(advice);
	}
	
	/**
	 * @param cls
	 * @param advisor
	 */
	public void removeAdvisor(Class cls, Advisor advisor) {
		Advised advisedService = (Advised) services.get(cls);
		advisedService.removeAdvisor(advisor);
		getAddedAdvisors(cls).remove(advisor);
	}
	
	/**
	 * @param cls
	 * @param advice
	 */
	public void removeAdvice(Class cls, Advice advice) {
		Advised advisedService = (Advised) services.get(cls);
		advisedService.removeAdvice(advice);
		getAddedAdvice(cls).remove(advice);
	}
	
	/**
	 * Moves advisors and advice added by ServiceContext from the source service to the target one.
	 *
	 * @param source the existing service
	 * @param target the new service
	 */
	private void moveAddedAOP(Advised source, Advised target) {
		Class serviceClass = source.getClass();
		Set<Advisor> existingAdvisors = getAddedAdvisors(serviceClass);
		for (Advisor advisor : existingAdvisors) {
			target.addAdvisor(advisor);
			source.removeAdvisor(advisor);
		}
		
		Set<Advice> existingAdvice = getAddedAdvice(serviceClass);
		for (Advice advice : existingAdvice) {
			target.addAdvice(advice);
			source.removeAdvice(advice);
		}
	}
	
	/**
	 * Removes all advice and advisors added by ServiceContext.
	 *
	 * @param cls the class of the cached service to cleanup
	 */
	private void removeAddedAOP(Class cls) {
		removeAddedAdvisors(cls);
		removeAddedAdvice(cls);
	}
	
	/**
	 * Removes all the advisors added by ServiceContext.
	 *
	 * @param cls the class of the cached service to cleanup
	 */
	private void removeAddedAdvisors(Class cls) {
		Advised advisedService = (Advised) services.get(cls);
		Set<Advisor> advisorsToRemove = addedAdvisors.get(cls);
		if (advisedService != null && advisorsToRemove != null) {
			for (Advisor advisor : advisorsToRemove.toArray(new Advisor[] {})) {
				removeAdvisor(cls, advisor);
			}
		}
	}
	
	/**
	 * Returns the set of advisors added by ServiceContext.
	 *
	 * @param cls the class of the cached service
	 * @return the set of advisors or an empty set
	 */
	@SuppressWarnings("unchecked")
	private Set<Advisor> getAddedAdvisors(Class cls) {
		Set<Advisor> result = addedAdvisors.get(cls);
		return (Set<Advisor>) (result == null ? Collections.emptySet() : result);
	}
	
	/**
	 * Removes all the advice added by the ServiceContext.
	 *
	 * @param cls the class of the caches service to cleanup
	 */
	private void removeAddedAdvice(Class cls) {
		Advised advisedService = (Advised) services.get(cls);
		Set<Advice> adviceToRemove = addedAdvice.get(cls);
		if (advisedService != null && adviceToRemove != null) {
			for (Advice advice : adviceToRemove.toArray(new Advice[] {})) {
				removeAdvice(cls, advice);
			}
		}
	}
	
	/**
	 * Returns the set of advice added by ServiceContext.
	 *
	 * @param cls the class of the cached service
	 * @return the set of advice or an empty set
	 */
	@SuppressWarnings("unchecked")
	private Set<Advice> getAddedAdvice(Class cls) {
		Set<Advice> result = addedAdvice.get(cls);
		return (Set<Advice>) (result == null ? Collections.emptySet() : result);
	}
	
	/**
	 * Returns the current proxy that is stored for the Class <code>cls</code>
	 *
	 * @param cls
	 * @return Object that is a proxy for the <code>cls</code> class
	 */
	@SuppressWarnings("unchecked")
	public <T> T getService(Class<? extends T> cls) {
		if (log.isTraceEnabled()) {
			log.trace("Getting service: " + cls);
		}
		
		// if the context is refreshing, wait until it is
		// done -- otherwise a null service might be returned
		synchronized (refreshingContextLock) {
			try {
				while (refreshingContext) {
					if (log.isDebugEnabled()) {
						log.debug("Waiting to get service: " + cls + " while the context is being refreshed");
					}
					
					refreshingContextLock.wait();
					
					if (log.isDebugEnabled()) {
						log.debug("Finished waiting to get service " + cls + " while the context was being refreshed");
					}
				}
				
			}
			catch (InterruptedException e) {
				log.warn("Refresh lock was interrupted", e);
			}
		}
		
		Object service = services.get(cls);
		if (service == null) {
			throw new APIException("Service not found: " + cls);
		}
		
		return (T) service;
	}
	
	/**
	 * Allow other services to be added to our service layer
	 *
	 * @param cls Interface to proxy
	 * @param classInstance the actual instance of the <code>cls</code> interface
	 */
	public void setService(Class cls, Object classInstance) {
		
		log.debug("Setting service: " + cls);
		
		if (cls != null && classInstance != null) {
			try {
				Advised cachedService = (Advised) services.get(cls);
				boolean noExistingService = cachedService == null;
				boolean replacingService = cachedService != null && cachedService != classInstance;
				boolean serviceAdvised = classInstance instanceof Advised;
				
				if (noExistingService || replacingService) {
					
					Advised advisedService;
					
					if (!serviceAdvised) {
						// Adding a bare service, wrap with AOP proxy
						Class[] interfaces = { cls };
						ProxyFactory factory = new ProxyFactory(interfaces);
						factory.setTarget(classInstance);
						advisedService = (Advised) factory.getProxy(OpenmrsClassLoader.getInstance());
					} else {
						advisedService = (Advised) classInstance;
					}
					
					if (replacingService) {
						moveAddedAOP(cachedService, advisedService);
					}
					
					services.put(cls, advisedService);
				}
				log.debug("Service: " + cls + " set successfully");
			}
			catch (Exception e) {
				throw new APIException("service.unable.create.proxy.factory", new Object[] { classInstance.getClass()
				        .getName() }, e);
			}
			
		}
	}
	
	/**
	 * Allow other services to be added to our service layer <br>
	 * <br>
	 * Classes will be found/loaded with the ModuleClassLoader <br>
	 * <br>
	 * <code>params</code>[0] = string representing the service interface<br>
	 * <code>params</code>[1] = service instance
	 *
	 * @param params list of parameters
	 */
	public void setModuleService(List<Object> params) {
		String classString = (String) params.get(0);
		Object classInstance = params.get(1);
		
		if (classString == null || classInstance == null) {
			throw new APIException("service.unable.find", (Object[]) null);
		}
		
		Class cls = null;
		
		// load the given 'classString' class from either the openmrs class
		// loader or the system class loader depending on if we're in a testing
		// environment or not (system == testing, openmrs == normal)
		try {
			if (!useSystemClassLoader) {
				cls = OpenmrsClassLoader.getInstance().loadClass(classString);
				
				if (cls != null && log.isDebugEnabled()) {
					try {
						log.debug("cls classloader: " + cls.getClass().getClassLoader() + " uid: "
						        + cls.getClass().getClassLoader().hashCode());
					}
					catch (Exception e) { /*pass*/}
				}
			} else if (useSystemClassLoader) {
				try {
					cls = Class.forName(classString);
					if (log.isDebugEnabled()) {
						log.debug("cls2 classloader: " + cls.getClass().getClassLoader() + " uid: "
						        + cls.getClass().getClassLoader().hashCode());
						//pay attention that here, cls = Class.forName(classString), the system class loader and
						//cls2 is the openmrs class loader, like above.
						log.debug("cls==cls2: "
						        + String.valueOf(cls == OpenmrsClassLoader.getInstance().loadClass(classString)));
					}
				}
				catch (Exception e) { /*pass*/}
			}
		}
		catch (ClassNotFoundException e) {
			throw new APIException("service.unable.set", new Object[] { classString }, e);
		}
		
		// add this module service to the normal list of services
		setService(cls, classInstance);
		
		//Run onStartup for all services implementing the OpenmrsService interface.
		if (OpenmrsService.class.isAssignableFrom(classInstance.getClass())) {
			moduleOpenmrsServices.put(classString, (OpenmrsService) classInstance);
			runOpenmrsServiceOnStartup((OpenmrsService) classInstance, classString);
		}
	}
	
	/**
	 * Set this service context to use the system class loader if the
	 * <code>useSystemClassLoader</code> is set to true. If false, the openmrs class loader is used
	 * to load module services
	 *
	 * @param useSystemClassLoader true/false whether to use the system class loader
	 */
	public void setUseSystemClassLoader(boolean useSystemClassLoader) {
		this.useSystemClassLoader = useSystemClassLoader;
	}
	
	/**
	 * Checks if we are using the system class loader.
	 *
	 * @return true if using the system class loader, else false.
	 */
	public boolean isUseSystemClassLoader() {
		return useSystemClassLoader;
	}
	
	public static void setRefreshingContext(boolean refreshingContext) {
		ServiceContext.refreshingContext = refreshingContext;
	}
	
	/**
	 * Should be called <b>right before</b> any spring context refresh This forces all calls to
	 * getService to wait until <code>doneRefreshingContext</code> is called
	 */
	public void startRefreshingContext() {
		synchronized (refreshingContextLock) {
			log.info("Refreshing Context");
			setRefreshingContext(true);
		}
	}
	
	/**
	 * Should be called <b>right after</b> any spring context refresh This wakes up all calls to
	 * getService that were waiting because <code>startRefreshingContext</code> was called
	 */
	public void doneRefreshingContext() {
		synchronized (refreshingContextLock) {
			log.info("Done refreshing Context");
			setRefreshingContext(false);
			refreshingContextLock.notifyAll();
		}
	}
	
	/**
	 * Returns true/false whether startRefreshingContext() has been called without a subsequent call
	 * to doneRefreshingContext() yet. All methods involved in starting/stopping a module should
	 * call this if a service method is needed -- otherwise a deadlock will occur.
	 *
	 * @return true/false whether the services are currently blocking waiting for a call to
	 *         doneRefreshingContext()
	 */
	public boolean isRefreshingContext() {
		synchronized (refreshingContextLock) {
			return refreshingContext;
		}
	}
	
	/**
	 * Retrieves all Beans which have been registered in the Spring {@link ApplicationContext} that
	 * match the given object type (including subclasses).
	 * <p>
	 * <b>NOTE: This method introspects top-level beans only.</b> It does <i>not</i> check nested
	 * beans which might match the specified type as well.
	 *
	 * @see ApplicationContext#getBeansOfType(Class)
	 * @param type the type of Bean to retrieve from the Spring {@link ApplicationContext}
	 * @return a List of all registered Beans that are valid instances of the passed type
	 * @since 1.5
	 * @should return a list of all registered beans of the passed type
	 * @should return beans registered in a module
	 * @should return an empty list if no beans have been registered of the passed type
	 */
	
	public <T> List<T> getRegisteredComponents(Class<T> type) {
		Map<String, T> m = getRegisteredComponents(applicationContext, type);
		if (log.isTraceEnabled()) {
			log.trace("getRegisteredComponents(" + type + ") = " + m);
		}
		return new ArrayList<>(m.values());
	}
	
	/**
	 * Retrieves a bean that match the given type (including subclasses) and name.
	 *
	 * @param beanName the name of registered bean to retrieve
	 * @param type the type of bean to retrieve 
	 * @return bean of passed type
	 *
	 * @since 1.9.4
	 */
	public <T> T getRegisteredComponent(String beanName, Class<T> type) throws APIException {
		try {
			return applicationContext.getBean(beanName, type);
		}
		catch (BeansException beanException) {
			throw new APIException("service.error.during.getting.component", null, beanException);
		}
	}
	
	/**
	 * Private method which returns all components registered in a Spring applicationContext of a
	 * given type This method recurses through each parent ApplicationContext
	 *
	 * @param context - The applicationContext to check
	 * @param type - The type of component to retrieve
	 * @return all components registered in a Spring applicationContext of a given type
	 */
	@SuppressWarnings("unchecked")
	private <T> Map<String, T> getRegisteredComponents(ApplicationContext context, Class<T> type) {
		Map<String, T> components = new HashMap<>();
		Map registeredComponents = context.getBeansOfType(type);
		if (log.isTraceEnabled()) {
			log.trace("getRegisteredComponents(" + context + ", " + type + ") = " + registeredComponents);
		}
		if (registeredComponents != null) {
			components.putAll(registeredComponents);
		}
		if (context.getParent() != null) {
			components.putAll(getRegisteredComponents(context.getParent(), type));
		}
		return components;
	}
	
	/**
	 * @param applicationContext the applicationContext to set
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	/**
	 * Calls the {@link OpenmrsService#onStartup()} method for an instance implementing the
	 * {@link OpenmrsService} interface.
	 *
	 * @param openmrsService instance implementing the {@link OpenmrsService} interface.
	 * @param classString the full instance class name including the package name.
	 * @since 1.9
	 */
	private void runOpenmrsServiceOnStartup(final OpenmrsService openmrsService, final String classString) {
		new Thread(() -> {
			try {
				synchronized (refreshingContextLock) {
					//Need to wait for application context to finish refreshing otherwise we get into trouble.
					while (refreshingContext) {
						if (log.isDebugEnabled()) {
							log.debug("Waiting to get service: " + classString + " while the context"
							        + " is being refreshed");
						}

						refreshingContextLock.wait();

						if (log.isDebugEnabled()) {
							log.debug("Finished waiting to get service " + classString
							        + " while the context was being refreshed");
						}
					}
				}

				Daemon.runStartupForService(openmrsService);
			}
			catch (InterruptedException e) {
				log.warn("Refresh lock was interrupted while waiting to run OpenmrsService.onStartup() for "
				        + classString, e);
			}
		}).start();
	}
	
	/**
	 * Gets a list of services implementing the {@link OpenmrsService} interface, for a given
	 * module.
	 *
	 * @param modulePackage the module's package name.
	 * @return the list of service instances.
	 * @since 1.9
	 */
	public List<OpenmrsService> getModuleOpenmrsServices(String modulePackage) {
		List<OpenmrsService> openmrsServices = new ArrayList<>();
		
		for (Entry<String, OpenmrsService> entry : moduleOpenmrsServices.entrySet()) {
			if (entry.getKey().startsWith(modulePackage)) {
				openmrsServices.add(entry.getValue());
			}
		}
		
		return openmrsServices;
	}
	
	/**
	 * Gets the visit service.
	 *
	 * @return visit service.
	 * @since 1.9
	 **/
	public VisitService getVisitService() {
		return getService(VisitService.class);
	}
	
	/**
	 * Sets the visit service.
	 *
	 * @param visitService the visitService to set
	 * @since 1.9
	 **/
	public void setVisitService(VisitService visitService) {
		setService(VisitService.class, visitService);
	}
	
	/**
	 * Gets the provider service.
	 *
	 * @return provider service.
	 * @since 1.9
	 **/
	
	public ProviderService getProviderService() {
		return getService(ProviderService.class);
	}
	
	/**
	 * Sets the provider service.
	 *
	 * @param providerService the providerService to set
	 * @since 1.9
	 **/
	public void setProviderService(ProviderService providerService) {
		setService(ProviderService.class, providerService);
	}
	
	/**
	 * Gets the datatype service
	 *
	 * @return custom datatype service
	 * @since 1.9
	 */
	public DatatypeService getDatatypeService() {
		return getService(DatatypeService.class);
	}
	
	/**
	 * Sets the datatype service
	 *
	 * @param datatypeService the datatypeService to set
	 * @since 1.9
	 */
	public void setDatatypeService(DatatypeService datatypeService) {
		setService(DatatypeService.class, datatypeService);
	}
	
}
