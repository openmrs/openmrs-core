package org.openmrs.api.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.UserService;
import org.openmrs.arden.ArdenService;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.hl7.HL7Service;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.MessageService;
import org.openmrs.reporting.ReportService;
import org.openmrs.scheduler.SchedulerService;

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

	private final Log log = LogFactory.getLog(getClass());

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
	private ReportService reportService;
	private FormEntryService formEntryService;
	private HL7Service hl7Service;
	private SchedulerService schedulerService;
	private MessageService messageService;
	private AlertService alertService;
	private ArdenService ardenService;
	private ProgramWorkflowService programWorkflowService;
	

	/**
	 * Default constructor
	 */
	public ServiceContext() { 
		log.info("Instantiating service context");
	}

	/**
	 * @return concept dictionary-related services
	 */
	public ConceptService getConceptService() {
		return conceptService;
	}

	/**
	 * @return encounter-related services
	 */
	public EncounterService getEncounterService() {
		return encounterService;
	}

	/**
	 * @return observation services
	 */
	public ObsService getObsService() {
		return obsService;
	}

	/**
	 * @return patient-related services
	 */
	public PatientService getPatientService() {
		return patientService;
	}
	
	/**
	 * @param patientService
	 */
	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}

	/**
	 * @return concept dictionary-related services
	 */
	public FormEntryService getFormEntryService() {
		return formEntryService;
	}

	/**
	 * @return Returns the hl7Service.
	 */
	public HL7Service getHL7Service() {
		return hl7Service;
	}

	/**
	 * @return patientset-related services
	 */
	public PatientSetService getPatientSetService() {
		return patientSetService;
	}

	/**
	 * @return user-related services
	 */
	public UserService getUserService() {
		return userService;
	}

	/**
	 * @return order service
	 */
	public OrderService getOrderService() {
		return orderService;
	}

	/**
	 * @return form service
	 */
	public FormService getFormService() {
		return formService;
	}

	/**
	 * @return report service
	 */
	public ReportService getReportService() {
		return reportService;
	}

	/**
	 * @return admin-related services
	 */
	public AdministrationService getAdministrationService() {
		return administrationService;
	}
	

	/**
	 * @param programWorkflowService
	 */
	public ProgramWorkflowService getProgramWorkflowService() {
		return this.programWorkflowService;
	}
	
	/**
	 * @param ardenService
	 */
	public ArdenService getArdenService() {
		return this.ardenService;
	}


	/**
	 * @return scheduler service
	 */
	public SchedulerService getSchedulerService() {
		return schedulerService;
	}

	/**
	 * Set the scheduler service.
	 * 
	 * @param service
	 */
	public void setSchedulerService(SchedulerService service) { 
		this.schedulerService = service;
	}	

	/**
	 * @return alert service
	 */
	public AlertService getAlertService() {
		return alertService;
	}

	/**
	 * @param alertService
	 */
	public void setAlertService(AlertService alertService) {
		this.alertService = alertService;
	}

	/**
	 * @param programWorkflowService
	 */
	public void setProgramWorkflowService(ProgramWorkflowService programWorkflowService) {
		this.programWorkflowService = programWorkflowService;
	}
	
	/**
	 * @param ardenService
	 */
	public void setArdenService(ArdenService ardenService) {
		this.ardenService = ardenService;
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
		return messageService;
	}
	
	/**
	 * Sets the message service.
	 * 
	 * @param service
	 */
	public void setMessageService(MessageService service) { 
		this.messageService = service;
	}

	/**
	 * @return the hl7Service
	 */
	public HL7Service getHl7Service() {
		return hl7Service;
	}

	/**
	 * @param hl7Service the hl7Service to set
	 */
	public void setHl7Service(HL7Service hl7Service) {
		this.hl7Service = hl7Service;
	}

	/**
	 * @param administrationService the administrationService to set
	 */
	public void setAdministrationService(AdministrationService administrationService) {
		this.administrationService = administrationService;
	}

	/**
	 * @param conceptService the conceptService to set
	 */
	public void setConceptService(ConceptService conceptService) {
		this.conceptService = conceptService;
	}

	/**
	 * @param encounterService the encounterService to set
	 */
	public void setEncounterService(EncounterService encounterService) {
		this.encounterService = encounterService;
	}

	/**
	 * @param formEntryService the formEntryService to set
	 */
	public void setFormEntryService(FormEntryService formEntryService) {
		this.formEntryService = formEntryService;
	}

	/**
	 * @param formService the formService to set
	 */
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	/**
	 * @param obsService the obsService to set
	 */
	public void setObsService(ObsService obsService) {
		this.obsService = obsService;
	}

	/**
	 * @param orderService the orderService to set
	 */
	public void setOrderService(OrderService orderService) {
		this.orderService = orderService;
	}

	/**
	 * @param patientSetService the patientSetService to set
	 */
	public void setPatientSetService(PatientSetService patientSetService) {
		this.patientSetService = patientSetService;
	}

	/**
	 * @param reportService the reportService to set
	 */
	public void setReportService(ReportService reportService) {
		this.reportService = reportService;
	}

	/**
	 * @param userService the userService to set
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}


}
