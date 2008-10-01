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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.openmrs.arden.ArdenService;
import org.openmrs.hl7.HL7Service;
import org.openmrs.logic.LogicService;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.MessageService;
import org.openmrs.reporting.ReportObjectService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;

/**
 * Represents an OpenMRS <code>Service Context</code>, which returns the 
 * services represented throughout the system.  
 * 
 * This class should not be access directly, but rather used through the
 * <code>Context</code> class.
 * 
 * This class is essentially static and only one instance is kept because
 * this is fairly heavy-weight. Spring takes care of filling in the actual
 * service implementations via dependency injection.  See the 
 * /metadata/api/spring/applicationContext-service.xml file.
 * 
 * Module services are also accessed through this class.  See 
 * {@link #getService(Class)} 
 * 
 * @see org.openmrs.api.context.Context
 */
public class ServiceContext {

	private static final Log log = LogFactory.getLog(ServiceContext.class);
	
	private static ServiceContext instance;
	private Boolean refreshingContext = new Boolean(false);
	
	/**
	 * Static variable holding whether or not to use the system classloader.
	 * By default this is false so the openmrs classloader is used instead
	 */
	private boolean useSystemClassLoader = false;
	
	// proxy factories used for programatically adding spring AOP  
	@SuppressWarnings("unchecked")
    Map<Class, ProxyFactory> proxyFactories = new HashMap<Class, ProxyFactory>();

	/**
	 * The default constructor is private so as to keep only one instance 
	 * per java vm.
	 * 
	 * @see ServiceContext#getInstance()
	 */
	private ServiceContext() { 
		log.debug("Instantiating service context");
	}
	
	/**
	 * There should only be one ServiceContext per openmrs (java virtual machine).
	 * 
	 * This method should be used when wanting to fetch the service context
	 * 
	 * Note: The ServiceContext shouldn't be used independently.  All calls
	 * should go through the Context
	 * 
	 * @return This VM's current ServiceContext.
	 * 
	 * @see org.openmrs.api.context.Context
	 */
	public static ServiceContext getInstance() {
		if (instance == null)
			instance = new ServiceContext();
		
		return instance;
	}
	
	/**
	 * Null out the current instance of the ServiceContext.  This should be used
	 * when modules are refreshing (being added/removed) and/or openmrs is shutting down
	 */
	@SuppressWarnings("unchecked")
    public static void destroyInstance() {
		if (instance != null && instance.proxyFactories != null) {
			if (log.isDebugEnabled()) {
				for (Map.Entry<Class, ProxyFactory> entry : instance.proxyFactories.entrySet()) {
					log.debug("Class:ProxyFactory - " + entry.getKey().getName() + ":" + entry.getValue());
				}
			}
			
			if (instance.proxyFactories != null)
				instance.proxyFactories.clear();
			
			instance.proxyFactories = null;
		}
		
		if (log.isDebugEnabled())
			log.debug("Destroying ServiceContext instance: " + instance);
		
		instance = null;
	}

	/**
	 * @return encounter-related services
	 */
	public EncounterService getEncounterService() {
		return (EncounterService)getService(EncounterService.class);
	}
	
	/**
	 * @return location services
	 */
	public LocationService getLocationService() {
		return (LocationService)getService(LocationService.class);
	}

	/**
	 * @return observation services
	 */
	public ObsService getObsService() {
		return (ObsService)getService(ObsService.class);
	}

	/**
	 * @return patientset-related services
	 */
	public PatientSetService getPatientSetService() {
		return (PatientSetService)getService(PatientSetService.class);
	}
	
	/**
	 * @return cohort related service
	 */
	public CohortService getCohortService() {
		return (CohortService) getService(CohortService.class);
	}
	
	/**
	 * @param cohort related service
	 */
	public void setCohortService(CohortService cs) {
		setService(CohortService.class, cs);
	}

	/**
	 * @return order service
	 */
	public OrderService getOrderService() {
		return (OrderService)getService(OrderService.class);
	}

	/**
	 * @return form service
	 */
	public FormService getFormService() {
		return (FormService)getService(FormService.class);
	}

	/**
	 * @return report object service
	 */
	public ReportObjectService getReportObjectService() {
		return (ReportObjectService)getService(ReportObjectService.class);
	}
	
	/** 
	 * @return report service
	 */
	public ReportService getReportService() {
		return (ReportService) getService(ReportService.class);
	}

	/**
	 * @return admin-related services
	 */
	public AdministrationService getAdministrationService() {
		return (AdministrationService)getService(AdministrationService.class);
	}
	

	/**
	 * @return programWorkflowService
	 */
	public ProgramWorkflowService getProgramWorkflowService() {
		return (ProgramWorkflowService)getService(ProgramWorkflowService.class);
	}
	
	/**
	 * @return ardenService
	 */
	public ArdenService getArdenService() {
		return (ArdenService)getService(ArdenService.class);
	}
	
	/**
	 * @return logicService
	 */
	public LogicService getLogicService() {
		return (LogicService)getService(LogicService.class);
	}

	/**
	 * @return scheduler service
	 */
	public SchedulerService getSchedulerService() {
		return (SchedulerService)getService(SchedulerService.class);
	}

	/**
	 * Set the scheduler service.
	 * 
	 * @param service
	 */
	public void setSchedulerService(SchedulerService schedulerService) { 
		setService(SchedulerService.class, schedulerService);
	}	

	/**
	 * @return alert service
	 */
	public AlertService getAlertService() {
		return (AlertService)getService(AlertService.class);
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
		return (MessageService)getService(MessageService.class);
	}
	
	/**
	 * Sets the message service.
	 * 
	 * @param service
	 */
	public void setMessageService(MessageService messageService) { 
		setService(MessageService.class, messageService);
	}

	/**
	 * @return the hl7Service
	 */
	public HL7Service getHL7Service() {
		return (HL7Service)getService(HL7Service.class);
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
	 */
	public void setReportObjectService(ReportObjectService reportObjectService) {
		setService(ReportObjectService.class, reportObjectService);
	}
	
	/**
	 * @param reportService
	 */
	public void setReportService(ReportService reportService) {
		setService(ReportService.class, reportService);
	}
	
	/**
	 * @param dataSetService
	 */
	public void setDataSetService(DataSetService dataSetService) {
		setService(DataSetService.class, dataSetService);
	}

	/** 
	 * @return
	 */
	public DataSetService getDataSetService() {
		return (DataSetService) getService(DataSetService.class);
	}

	/**
	 * @return patient related services
	 */
	public PatientService getPatientService() {
		return (PatientService)getService(PatientService.class);
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
		return (PersonService)getService(PersonService.class);
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
		return (ConceptService)getService(ConceptService.class);
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
		return (UserService)getService(UserService.class);
	}
	
	/**
	 * @param userService the userService to set
	 */
	public void setUserService(UserService userService) {
		setService(UserService.class, userService);
	}
	
	/**
	 * Get the proxy factory object for the given Class
	 * @param cls
	 * @return
	 */
	@SuppressWarnings("unchecked")
    private ProxyFactory getFactory(Class cls) {
		ProxyFactory factory = proxyFactories.get(cls);
		if (factory == null)
			throw new APIException("A proxy factory for: '" + cls + "' doesn't exist");
		return factory;
	}
	
	/**
	 * 
	 * @param cls
	 * @param advisor
	 */
	@SuppressWarnings("unchecked")
    public void addAdvisor(Class cls, Advisor advisor) {
		ProxyFactory factory = getFactory(cls);
		factory.addAdvisor(advisor);
	}
	
	/**
	 * 
	 * @param cls
	 * @param advice
	 */
	@SuppressWarnings("unchecked")
    public void addAdvice(Class cls, Advice advice) {
		ProxyFactory factory = getFactory(cls);
		factory.addAdvice(advice);
	}
	
	/**
	 * 
	 * @param cls
	 * @param advisor
	 */
	@SuppressWarnings("unchecked")
    public void removeAdvisor(Class cls, Advisor advisor) {
		ProxyFactory factory = getFactory(cls);
		factory.removeAdvisor(advisor);
	}
	
	/**
	 * 
	 * @param cls
	 * @param advice
	 */
	@SuppressWarnings("unchecked")
    public void removeAdvice(Class cls, Advice advice) {
		ProxyFactory factory = getFactory(cls);
		factory.removeAdvice(advice);
	}
	
	/**
	 * Returns the current proxy that is stored for the 
	 * Class <code>cls</code>
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
		
		ProxyFactory factory = proxyFactories.get(cls);
		if (factory == null)
			throw new APIException("Service not found: " + cls);
		
		return (T)factory.getProxy(OpenmrsClassLoader.getInstance());
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
				Class[] interfaces = {cls};
				ProxyFactory factory = new ProxyFactory(interfaces);
				factory.setTarget(classInstance);
				proxyFactories.put(cls, factory);
				log.debug("Service: " + cls + " set successfully");
			}
			catch (Exception e) {
				throw new APIException("Unable to create proxy factory for: " + classInstance.getClass().getName(), e);
			}
			
		}
	}
	
	/**
	 * Allow other services to be added to our service layer
	 * 
	 * Classes will be found/loaded with the ModuleClassLoader
	 * 
	 * <code>params</code>[0] = string representing the service interface
	 * <code>params</code>[1] = service instance
	 * 
	 * @param list list of parameters
	 */
	@SuppressWarnings("unchecked")
    public void setModuleService(List<Object> params) {
		String classString = (String)params.get(0);
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
						log.debug("cls classloader: " + cls.getClass().getClassLoader() + " uid: " + cls.getClass().getClassLoader().hashCode());
					}
					catch (Exception e) { /*pass*/ }
				}
			}
			else if (useSystemClassLoader == true) {
				try {
					cls = Class.forName(classString);
					if (log.isDebugEnabled()) {
						log.debug("cls2 classloader: " + cls.getClass().getClassLoader() + " uid: " + cls.getClass().getClassLoader().hashCode());
						log.debug("cls==cls2: " + String.valueOf(cls == cls));
					}
				}
				catch (Exception e) { /*pass*/ }
			}
		}
		catch (ClassNotFoundException e) {
			throw new APIException("Unable to set module service: " + classString, e);
		}
		
		// add this module service to the normal list of services
		setService(cls, classInstance);
	}
	
	/**
	 * Set this service context to use the system class loader if the 
	 * <code>useSystemClassLoader</code> is set to true.  If false, the openmrs 
	 * class loader is used to load module services
	 * 
	 * @param useSystemClassLoader true/false whether to use the system class loader
	 */
	public void setUseSystemClassLoader(boolean useSystemClassLoader) {
		this.useSystemClassLoader = useSystemClassLoader;
	}
	
	/**
	 * Should be called <b>right before</b> any spring context refresh
	 * 
	 * This forces all calls to getService to wait until 
	 * <code>doneRefreshingContext</code> is called
	 */
	public void startRefreshingContext() {
		synchronized (refreshingContext) {
			refreshingContext = true;
		}
	}
	
	/**
	 * Should be called <b>right after</b> any spring context refresh
	 * 
	 * This wakes up all calls to getService that were waiting
	 * because <code>startRefreshingContext</code> was called
	 */	
	public void doneRefreshingContext() {
		synchronized(refreshingContext) {
			refreshingContext.notifyAll();
			refreshingContext = false;
		}
	}
	
	/**
	 * Returns true/false whether startRefreshingContext() has been called
	 * without a subsequent call to doneRefreshingContext() yet.  All methods
	 * involved in starting/stopping a module should call this if a service
	 * method is needed -- otherwise a deadlock will occur.
	 * 
	 * @return true/false whether the services are currently blocking waiting 
	 * 			for a call to doneRefreshingContext() 
	 */
	public boolean isRefreshingContext() {
		return refreshingContext.booleanValue();
	}
	
}
