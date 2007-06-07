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
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.UserService;
import org.openmrs.arden.ArdenService;
import org.openmrs.hl7.HL7Service;
import org.openmrs.logic.LogicService;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.MessageService;
import org.openmrs.reporting.ReportService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;

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
public class ServiceContext {

	private static final Log log = LogFactory.getLog(ServiceContext.class);
	
	private static ServiceContext instance;
	private Boolean refreshingContext = new Boolean(false);
	
	// proxy factories used for programatically adding spring AOP  
	Map<Class, ProxyFactory> proxyFactories = new HashMap<Class, ProxyFactory>();

	/**
	 * Default constructor
	 */
	private ServiceContext() { 
		log.debug("Instantiating service context");
	}
	
	public static ServiceContext getInstance() {
		if (instance == null)
			instance = new ServiceContext();
		
		return instance;
	}
	
	public static void destroyInstance() {
		if (log.isErrorEnabled()) {
			if (instance != null && instance.proxyFactories != null) {
				for (Map.Entry<Class, ProxyFactory> entry : instance.proxyFactories.entrySet()) {
					log.debug("Class:ProxyFactory - " + entry.getKey().getName() + ":" + entry.getValue());
				}
				if (instance.proxyFactories != null)
					instance.proxyFactories.clear();
				instance.proxyFactories = null;
				}
			
			log.debug("Destroying ServiceContext instance: " + instance);
		}
		instance = null;
	}

	/**
	 * @return encounter-related services
	 */
	public EncounterService getEncounterService() {
		return (EncounterService)getService(EncounterService.class);
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
	
	public CohortService getCohortService() {
		return (CohortService) getService(CohortService.class);
	}
	
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
	 * @return report service
	 */
	public ReportService getReportService() {
		return (ReportService)getService(ReportService.class);
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
	 * @param reportService the reportService to set
	 */
	public void setReportService(ReportService reportService) {
		setService(ReportService.class, reportService);
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
	public void addAdvisor(Class cls, Advisor advisor) {
		ProxyFactory factory = getFactory(cls);
		factory.addAdvisor(advisor);
	}
	
	/**
	 * 
	 * @param cls
	 * @param advice
	 */
	public void addAdvice(Class cls, Advice advice) {
		ProxyFactory factory = getFactory(cls);
		factory.addAdvice(advice);
	}
	
	/**
	 * 
	 * @param cls
	 * @param advisor
	 */
	public void removeAdvisor(Class cls, Advisor advisor) {
		ProxyFactory factory = getFactory(cls);
		factory.removeAdvisor(advisor);
	}
	
	/**
	 * 
	 * @param cls
	 * @param advice
	 */
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
	public Object getService(Class cls) {
		log.debug("Getting service: " + cls);
		
		// if the context is refreshing, wait until it is 
		// done -- otherwise a null service might be returned
		synchronized (refreshingContext) {
			if (refreshingContext.booleanValue())
				try {
					refreshingContext.wait();
				}
				catch (InterruptedException e) {
					log.warn("Refresh lock was interrupted", e);
				}
		}
		
		ProxyFactory factory = proxyFactories.get(cls);
		if (factory == null)
			throw new APIException("Service not found: " + cls);
		
		return factory.getProxy(OpenmrsClassLoader.getInstance());
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
	public void setModuleService(List<Object> params) {
		String classString = (String)params.get(0);
		Object classInstance = params.get(1);
		
		if (classString == null || classInstance == null) {
			throw new APIException("Unable to find classString or classInstance in params");
		}
		
		Class cls = null;
		
		try {
			//ModuleClassLoader mcl = ModuleFactory.getModuleClassLoader(moduleId);
			cls = OpenmrsClassLoader.getInstance().loadClass(classString);
			
			try {
				if (cls != null)
					log.error("cls classloader: " + cls.getClass().getClassLoader() + " uid: " + cls.getClass().getClassLoader().hashCode());
			}
			catch (Exception e) { /*pass*/ }
			try {
				Class cls2 = Class.forName(classString);
				log.error("cls2 classloader: " + cls2.getClass().getClassLoader() + " uid: " + cls.getClass().getClassLoader().hashCode());
				log.error("cls==cls2: " + String.valueOf(cls == cls2));
			}
			catch (Exception e) { /*pass*/ }
		}
		catch (ClassNotFoundException e) {
			throw new APIException("Unable to set module service: " + classString, e);
		}
		
		setService(cls, classInstance);
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
	
}
