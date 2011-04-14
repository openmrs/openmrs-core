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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.ActiveListService;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DataSetService;
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
import org.openmrs.api.ReportService;
import org.openmrs.api.SerializationService;
import org.openmrs.api.UserService;
import org.openmrs.arden.ArdenService;
import org.openmrs.hl7.HL7Service;
import org.openmrs.logic.LogicService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.MessageService;
import org.openmrs.reporting.ReportObjectService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Represents an OpenMRS <code>Service Context</code>, which returns the services represented
 * throughout the system. <br/>
 * <br/>
 * This class should not be access directly, but rather used through the <code>Context</code> class. <br/>
 * <br/>
 * This class is essentially static and only one instance is kept because this is fairly
 * heavy-weight. Spring takes care of filling in the actual service implementations via dependency
 * injection. See the /metadata/api/spring/applicationContext-service.xml file. <br/>
 * <br/>
 * Module services are also accessed through this class. See {@link #getService(Class)}
 * 
 * @see org.openmrs.api.context.Context
 */
public class ServiceContext implements ApplicationContextAware {
	
	private static final Log log = LogFactory.getLog(ServiceContext.class);
	
	private static ServiceContext instance;
	
	private ApplicationContext applicationContext;
	
	private Boolean refreshingContext = new Boolean(false);
	
	/**
	 * Static variable holding whether or not to use the system classloader. By default this is
	 * false so the openmrs classloader is used instead
	 */
	private boolean useSystemClassLoader = false;
	
	// Cached service objects
	@SuppressWarnings("unchecked")
	Map<Class, Object> services = new HashMap<Class, Object>();
	
	// Advisors added to services by this service
	@SuppressWarnings("unchecked")
	Map<Class, Set<Advisor>> addedAdvisors = new HashMap<Class, Set<Advisor>>();
	
	// Advice added to services by this service
	@SuppressWarnings("unchecked")
	Map<Class, Set<Advice>> addedAdvice = new HashMap<Class, Set<Advice>>();
	
	/**
	 * Services implementing the OpenmrsService interface for each module.
	 * 
	 * @since 1.9
	 */
	@SuppressWarnings("unchecked")
	Map<String, List<OpenmrsService>> moduleOpenmrsServices = new HashMap<String, List<OpenmrsService>>();
	
	/**
	 * The default constructor is private so as to keep only one instance per java vm.
	 * 
	 * @see ServiceContext#getInstance()
	 */
	private ServiceContext() {
		log.debug("Instantiating service context");
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
		if (instance == null)
			instance = new ServiceContext();
		
		return instance;
	}
	
	/**
	 * Null out the current instance of the ServiceContext. This should be used when modules are
	 * refreshing (being added/removed) and/or openmrs is shutting down
	 */
	@SuppressWarnings("unchecked")
	public static void destroyInstance() {
		if (instance != null && instance.services != null) {
			if (log.isDebugEnabled()) {
				for (Map.Entry<Class, Object> entry : instance.services.entrySet()) {
					log.debug("Service - " + entry.getKey().getName() + ":" + entry.getValue());
				}
			}
			
			// Remove advice and advisors that this service added
			for (Class serviceClass : instance.services.keySet()) {
				instance.removeAddedAOP(serviceClass);
			}
			
			if (instance.services != null) {
				instance.services.clear();
				instance.services = null;
			}
			
			if (instance.addedAdvisors != null) {
				instance.addedAdvisors.clear();
				instance.addedAdvisors = null;
			}
			
			if (instance.addedAdvice != null) {
				instance.addedAdvice.clear();
				instance.addedAdvice = null;
			}
		}
		
		if (log.isDebugEnabled())
			log.debug("Destroying ServiceContext instance: " + instance);
		
		instance = null;
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
	 * @return patientset-related services
	 */
	public PatientSetService getPatientSetService() {
		return getService(PatientSetService.class);
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
	 * @return report object service
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public ReportObjectService getReportObjectService() {
		return getService(ReportObjectService.class);
	}
	
	/**
	 * @return serialization service
	 */
	public SerializationService getSerializationService() {
		return getService(SerializationService.class);
	}
	
	/**
	 * @return report service
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public ReportService getReportService() {
		return getService(ReportService.class);
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
	 * @return ardenService
	 */
	public ArdenService getArdenService() {
		return getService(ArdenService.class);
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
	 * @param ardenService
	 */
	public void setArdenService(ArdenService ardenService) {
		setService(ArdenService.class, ardenService);
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
	// TODO spring is demanding that this be hl7Service:setHl7Service and not hL7Service:setHL7Service. why?
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
	 * @param patientSetService the patientSetService to set
	 */
	public void setPatientSetService(PatientSetService patientSetService) {
		setService(PatientSetService.class, patientSetService);
	}
	
	/**
	 * @param reportObjectService the reportObjectService to set
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public void setReportObjectService(ReportObjectService reportObjectService) {
		setService(ReportObjectService.class, reportObjectService);
	}
	
	/**
	 * @param reportService
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public void setReportService(ReportService reportService) {
		setService(ReportService.class, reportService);
	}
	
	/**
	 * @param serializationService
	 */
	public void setSerializationService(SerializationService serializationService) {
		setService(SerializationService.class, serializationService);
	}
	
	/**
	 * @param dataSetService
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public void setDataSetService(DataSetService dataSetService) {
		setService(DataSetService.class, dataSetService);
	}
	
	/**
	 * @return the DataSetService
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public DataSetService getDataSetService() {
		return getService(DataSetService.class);
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
		return getService(MessageSourceService.class);
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
	 * Gets the ActiveListService used in the context.
	 * 
	 * @return ActiveListService
	 */
	public ActiveListService getActiveListService() {
		return getService(ActiveListService.class);
	}
	
	/**
	 * Sets the ActiveListService used in the context
	 */
	public void setActiveListService(ActiveListService activeListService) {
		setService(ActiveListService.class, activeListService);
	}
	
	/**
	 * @param cls
	 * @param advisor
	 */
	@SuppressWarnings("unchecked")
	public void addAdvisor(Class cls, Advisor advisor) {
		Advised advisedService = (Advised) services.get(cls);
		if (advisedService.indexOf(advisor) < 0)
			advisedService.addAdvisor(advisor);
		if (addedAdvisors.get(cls) == null)
			addedAdvisors.put(cls, new HashSet<Advisor>());
		getAddedAdvisors(cls).add(advisor);
	}
	
	/**
	 * @param cls
	 * @param advice
	 */
	@SuppressWarnings("unchecked")
	public void addAdvice(Class cls, Advice advice) {
		Advised advisedService = (Advised) services.get(cls);
		if (advisedService.indexOf(advice) < 0)
			advisedService.addAdvice(advice);
		if (addedAdvice.get(cls) == null)
			addedAdvice.put(cls, new HashSet<Advice>());
		getAddedAdvice(cls).add(advice);
	}
	
	/**
	 * @param cls
	 * @param advisor
	 */
	@SuppressWarnings("unchecked")
	public void removeAdvisor(Class cls, Advisor advisor) {
		Advised advisedService = (Advised) services.get(cls);
		advisedService.removeAdvisor(advisor);
		getAddedAdvisors(cls).remove(advisor);
	}
	
	/**
	 * @param cls
	 * @param advice
	 */
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
	private void removeAddedAOP(Class cls) {
		removeAddedAdvisors(cls);
		removeAddedAdvice(cls);
	}
	
	/**
	 * Removes all the advisors added by ServiceContext.
	 * 
	 * @param cls the class of the cached service to cleanup
	 */
	@SuppressWarnings("unchecked")
	private void removeAddedAdvisors(Class cls) {
		Advised advisedService = (Advised) services.get(cls);
		Set<Advisor> advisorsToRemove = addedAdvisors.get(cls);
		if (advisedService != null && advisorsToRemove != null) {
			for (Advisor advisor : advisorsToRemove.toArray(new Advisor[] {}))
				removeAdvisor(cls, advisor);
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
		return result == null ? Collections.EMPTY_SET : result;
	}
	
	/**
	 * Removes all the advice added by the ServiceContext.
	 * 
	 * @param cls the class of the caches service to cleanup
	 */
	@SuppressWarnings("unchecked")
	private void removeAddedAdvice(Class cls) {
		Advised advisedService = (Advised) services.get(cls);
		Set<Advice> adviceToRemove = addedAdvice.get(cls);
		if (advisedService != null && adviceToRemove != null) {
			for (Advice advice : adviceToRemove.toArray(new Advice[] {}))
				removeAdvice(cls, advice);
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
		return result == null ? Collections.EMPTY_SET : result;
	}
	
	/**
	 * Returns the current proxy that is stored for the Class <code>cls</code>
	 * 
	 * @param cls
	 * @return Object that is a proxy for the <code>cls</code> class
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> T getService(Class<? extends T> cls) {
		if (log.isTraceEnabled())
			log.trace("Getting service: " + cls);
		
		// if the context is refreshing, wait until it is
		// done -- otherwise a null service might be returned
		synchronized (refreshingContext) {
			if (refreshingContext.booleanValue())
				try {
					log.warn("Waiting to get service: " + cls + " while the context is being refreshed");
					refreshingContext.wait();
					log.warn("Finished waiting to get service " + cls + " while the context was being refreshed");
				}
				catch (InterruptedException e) {
					log.warn("Refresh lock was interrupted", e);
				}
		}
		
		Object service = services.get(cls);
		if (service == null)
			throw new APIException("Service not found: " + cls);
		
		return (T) service;
	}
	
	/**
	 * Allow other services to be added to our service layer
	 * 
	 * @param cls Interface to proxy
	 * @param classInstance the actual instance of the <code>cls</code> interface
	 */
	@SuppressWarnings("unchecked")
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
					} else
						advisedService = (Advised) classInstance;
					
					if (replacingService)
						moveAddedAOP(cachedService, advisedService);
					
					services.put(cls, advisedService);
				}
				log.debug("Service: " + cls + " set successfully");
			}
			catch (Exception e) {
				throw new APIException("Unable to create proxy factory for: " + classInstance.getClass().getName(), e);
			}
			
		}
	}
	
	/**
	 * Allow other services to be added to our service layer <br/>
	 * <br/>
	 * Classes will be found/loaded with the ModuleClassLoader <br/>
	 * <br/>
	 * <code>params</code>[0] = string representing the service interface<br/>
	 * <code>params</code>[1] = service instance
	 * 
	 * @param params list of parameters
	 */
	@SuppressWarnings("unchecked")
	public void setModuleService(List<Object> params) {
		String classString = (String) params.get(0);
		Object classInstance = params.get(1);
		
		if (classString == null || classInstance == null) {
			throw new APIException("Unable to find classString or classInstance in params");
		}
		
		Class cls = null;
		
		// load the given 'classString' class from either the openmrs class
		// loader or the system class loader depending on if we're in a testing
		// environment or not (system == testing, openmrs == normal)
		try {
			if (useSystemClassLoader == false) {
				cls = OpenmrsClassLoader.getInstance().loadClass(classString);
				
				if (cls != null && log.isDebugEnabled()) {
					try {
						log.debug("cls classloader: " + cls.getClass().getClassLoader() + " uid: "
						        + cls.getClass().getClassLoader().hashCode());
					}
					catch (Exception e) { /*pass*/}
				}
			} else if (useSystemClassLoader == true) {
				try {
					cls = Class.forName(classString);
					if (log.isDebugEnabled()) {
						log.debug("cls2 classloader: " + cls.getClass().getClassLoader() + " uid: "
						        + cls.getClass().getClassLoader().hashCode());
						log.debug("cls==cls2: " + String.valueOf(cls == cls));
					}
				}
				catch (Exception e) { /*pass*/}
			}
		}
		catch (ClassNotFoundException e) {
			throw new APIException("Unable to set module service: " + classString, e);
		}
		
		// add this module service to the normal list of services
		setService(cls, classInstance);
		
		//Run onStartup for all services implementing the OpenmrsService interface.
		if (OpenmrsService.class.isAssignableFrom(classInstance.getClass())) {
			addModuleOpenmrsService(classString, (OpenmrsService) classInstance);
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
	 * Should be called <b>right before</b> any spring context refresh This forces all calls to
	 * getService to wait until <code>doneRefreshingContext</code> is called
	 */
	public void startRefreshingContext() {
		synchronized (refreshingContext) {
			refreshingContext = true;
		}
	}
	
	/**
	 * Should be called <b>right after</b> any spring context refresh This wakes up all calls to
	 * getService that were waiting because <code>startRefreshingContext</code> was called
	 */
	public void doneRefreshingContext() {
		synchronized (refreshingContext) {
			refreshingContext.notifyAll();
			refreshingContext = false;
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
		return refreshingContext.booleanValue();
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
		log.debug("getRegisteredComponents(" + type + ") = " + m);
		return new ArrayList<T>(m.values());
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
		Map<String, T> components = new HashMap<String, T>();
		Map registeredComponents = context.getBeansOfType(type);
		log.debug("getRegisteredComponents(" + context + ", " + type + ") = " + registeredComponents);
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
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	/**
	 * Adds a module's service implementing the {@link OpenmrsService} interface, to the list.
	 * 
	 * @param classString the full name including package for the service class.
	 * @param openmrsService the service instance.
	 * @since 1.9
	 */
	private void addModuleOpenmrsService(String classString, OpenmrsService openmrsService) {
		
		final String PACKAGE_PREFIX = "org.openmrs.module.";
		
		//Assuming a naming convention which starts with org.openmrs.module.MODULEID
		//The logic service violates this in: "org.openmrs.logic.token.TokenService"
		
		//Look for the '.' character after the package prefix.
		int pos = classString.indexOf('.', PACKAGE_PREFIX.length());
		if (pos == -1 || !classString.contains(PACKAGE_PREFIX)) {
			//TODO Should i just have special handling for the logic services? 
			//May be not because we the logic service may not need to be shut down being a core module
			//and yet this storing of module services is used only when a module is stopping.
			log.warn(classString + " does not follow module naming convention.");
			return;
		}
		
		//The module package should end just before the '.' character which is after the module id.
		String modulePackage = classString.substring(0, pos);
		
		List<OpenmrsService> serviceList = moduleOpenmrsServices.get(modulePackage);
		if (serviceList == null) {
			serviceList = new ArrayList<OpenmrsService>();
			moduleOpenmrsServices.put(modulePackage, serviceList);
		}
		
		serviceList.add(openmrsService);
		
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
		new Thread() {
			
			@Override
			public void run() {
				try {
					synchronized (refreshingContext) {
						//Need to wait for application context to finish refreshing otherwise we get into trouble.
						refreshingContext.wait();
					}
					
					Daemon.runStartupForService(openmrsService);
				}
				catch (InterruptedException e) {
					log.warn("Refresh lock was interrupted while waiting to run OpenmrsService.onStartup() for "
					        + classString, e);
				}
			}
		}.start();
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
		return moduleOpenmrsServices.get(modulePackage);
	}
}
