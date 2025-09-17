package org.openmrs.api.context;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.annotation.OpenmrsProfileExcludeFilter;
import org.openmrs.annotation.OpenmrsProfileIncludeFilter;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConditionService;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.EventListeners;
import org.openmrs.api.FormService;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.LocationService;
import org.openmrs.api.MedicationDispenseService;
import org.openmrs.api.ObsService;
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
import org.openmrs.api.db.hibernate.AuditableInterceptor;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateSessionFactoryBean;
import org.openmrs.api.impl.AdministrationServiceImpl;
import org.openmrs.api.impl.GlobalLocaleList;
import org.openmrs.api.impl.OrderServiceImpl;
import org.openmrs.api.impl.PersonNameGlobalPropertyListener;
import org.openmrs.hl7.HL7Service;
import org.openmrs.logging.LoggingConfigurationGlobalPropertyListener;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.MessageService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.HttpClient;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.LocationUtility;
import org.openmrs.util.TestTypeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
@ComponentScan(
    basePackages = "org.openmrs",
    includeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Handler.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = OpenmrsProfileIncludeFilter.class)
    },
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TestTypeFilter.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = OpenmrsProfileExcludeFilter.class)
    }
)
public class OpenmrsApplicationContextConfig {

    @Bean
    public EventListeners clearOpenmrsEventListeners() {
        EventListeners eventListeners = new EventListeners();
        eventListeners.setGlobalPropertyListenersToEmpty(false);
        return eventListeners;
    }

    @Bean
    public LocaleUtility localeUtility() {
        return new LocaleUtility();
    }

    @Bean
    public LocationUtility locationUtility() {
        return new LocationUtility();
    }

    @Bean
    public ConfigUtil configUtilGlobalPropertyListener() {
        return new ConfigUtil();
    }

    @Bean
    public PersonNameGlobalPropertyListener personNameGlobalPropertyListener() {
        return new PersonNameGlobalPropertyListener();
    }

    @Bean
    public LoggingConfigurationGlobalPropertyListener loggingConfigurationGlobalPropertyListener() {
        return new LoggingConfigurationGlobalPropertyListener();
    }

    @Bean
    @DependsOn("clearOpenmrsEventListeners")
    public EventListeners openmrsEventListeners(LocaleUtility localeUtility, LocationUtility locationUtility,
            ConfigUtil configUtilGlobalPropertyListener,
            PersonNameGlobalPropertyListener personNameGlobalPropertyListener,
            LoggingConfigurationGlobalPropertyListener loggingConfigurationGlobalPropertyListener,
            GlobalLocaleList globalLocaleList, AdministrationServiceImpl adminService,
            OrderServiceImpl orderService) {
        EventListeners eventListeners = new EventListeners();
        List<GlobalPropertyListener> globalPropertyListeners = List.of(localeUtility, locationUtility,
                configUtilGlobalPropertyListener, personNameGlobalPropertyListener,
                loggingConfigurationGlobalPropertyListener, globalLocaleList, adminService, orderService);
        eventListeners.setGlobalPropertyListeners(globalPropertyListeners);
        return eventListeners;
    }

    @Bean(destroyMethod = "destroyInstance")
    public ServiceContext serviceContext(PatientService patientService, PersonService personService,
            ConceptService conceptService, UserService userService, ObsService obsService,
            EncounterService encounterService, LocationService locationService, OrderService orderService,
            ConditionService conditionService, DiagnosisService diagnosisService,
            MedicationDispenseService medicationDispenseService, OrderSetService orderSetService,
            FormService formService, AdministrationService adminService, DatatypeService datatypeService,
            ProgramWorkflowService programWorkflowService, CohortService cohortService, MessageService messageService,
            SerializationService serializationService, SchedulerService schedulerService, AlertService alertService,
            HL7Service hL7Service, MessageSourceService messageSourceService, VisitService visitService,
            ProviderService providerService) {
        ServiceContext serviceContext = ServiceContext.getInstance();
        serviceContext.setPatientService(patientService);
        serviceContext.setPersonService(personService);
        serviceContext.setConceptService(conceptService);
        serviceContext.setUserService(userService);
        serviceContext.setObsService(obsService);
        serviceContext.setEncounterService(encounterService);
        serviceContext.setLocationService(locationService);
        serviceContext.setOrderService(orderService);
        serviceContext.setConditionService(conditionService);
        serviceContext.setDiagnosisService(diagnosisService);
        serviceContext.setMedicationDispenseService(medicationDispenseService);
        serviceContext.setOrderSetService(orderSetService);
        serviceContext.setFormService(formService);
        serviceContext.setAdministrationService(adminService);
        serviceContext.setDatatypeService(datatypeService);
        serviceContext.setProgramWorkflowService(programWorkflowService);
        serviceContext.setCohortService(cohortService);
        serviceContext.setMessageService(messageService);
        serviceContext.setSerializationService(serializationService);
        serviceContext.setSchedulerService(schedulerService);
        serviceContext.setAlertService(alertService);
        serviceContext.setHl7Service(hL7Service);
        serviceContext.setMessageSourceService(messageSourceService);
        serviceContext.setVisitService(visitService);
        serviceContext.setProviderService(providerService);
        return serviceContext;
    }

    @Bean
    public Context context(ContextDAO contextDAO, ServiceContext serviceContext) {
        Context context = new Context();
        context.setServiceContext(serviceContext);
        context.setContextDAO(contextDAO);
        return context;
    }

    @Bean
    public List<Object> moduleTestingMappingJarLocations() {
        return new ArrayList<>();
    }

    @Bean
    public List<Object> mappingJarResources() {
        List<Object> mergedList = new ArrayList<>(moduleTestingMappingJarLocations());
        return mergedList;
    }

    @Bean
    public AuditableInterceptor auditableInterceptor() {
        return new AuditableInterceptor();
    }

    @Bean
    public HibernateSessionFactoryBean sessionFactory(Resource mappingJarResources) {
        HibernateSessionFactoryBean sessionFactory = new HibernateSessionFactoryBean();
        sessionFactory.setConfigLocations(new Resource[] {
                new ClassPathResource("hibernate.cfg.xml")
        });
        sessionFactory.setMappingJarLocations(mappingJarResources);
        sessionFactory.setPackagesToScan("org.openmrs");
        return sessionFactory;
    }

    @Bean
    public DbSessionFactory dbSessionFactory(SessionFactory sessionFactory) {
        return new DbSessionFactory(sessionFactory);
    }

    @Bean
    public HttpClient implementationIdHttpClient() throws MalformedURLException {
        HttpClient httpClient = new HttpClient("https://implementation.openmrs.org");
        return httpClient;
    }

}
