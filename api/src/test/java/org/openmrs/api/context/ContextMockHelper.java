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

import org.mockito.InjectMocks;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.ReportService;
import org.openmrs.api.SerializationService;
import org.openmrs.api.UserService;
import org.openmrs.api.VisitService;
import org.openmrs.messagesource.MessageSourceService;

import java.util.HashMap;
import java.util.Map;

/**
 * Helps to mock or spy on services. It can be used with {@link InjectMocks}. See
 * {@link org.openmrs.module.ModuleUtilTest} for example. In general you should always try to refactor code first so
 * that this class is not needed. In practice it is mostly enough to replace calls to
 * Context.get...Service with fields, which are injected through a constructor.
 *
 * @deprecated Avoid using this by not calling Context.get...Service() in your code.
 * @since 1.10
 */
@Deprecated
public class ContextMockHelper {
	
	/**
	 * Mockito does not call setters if there are no fields so you must add both a setter and a
	 * field.
	 */
	AdministrationService administrationService;
	
	CohortService cohortService;
	
	ConceptService conceptService;
	
	DatatypeService datatypeService;
	
	EncounterService encounterService;
	
	FormService formService;
	
	LocationService locationService;
	
	MessageSourceService messageSourceService;
	
	ObsService obsService;
	
	OrderService orderService;
	
	PatientService patientService;
	
	ProviderService providerService;
	
	ReportService reportService;
	
	SerializationService serializationService;
	
	UserService userService;
	
	VisitService visitService;
	
	UserContext userContext;
	
	Map<Class<?>, Object> realServices = new HashMap<Class<?>, Object>();
	
	UserContext realUserContext;
	
	boolean userContextMocked = false;
	
	public ContextMockHelper() {
	}
	
	public void revertMocks() {
		for (Map.Entry<Class<?>, Object> realService : realServices.entrySet()) {
			Context.getServiceContext().setService(realService.getKey(), realService.getValue());
		}
		realServices.clear();
		
		if (userContextMocked) {
			Context.setUserContext(realUserContext);
			realUserContext = null;
			userContextMocked = false;
		}
	}
	
	public void setService(Class<?> type, Object service) {
		if (!realServices.containsKey(type)) {
			Object realService = null;
			try {
				realService = Context.getService(type);
			}
			catch (Exception e) {
				//let's not fail if context is not configured
			}
			
			realServices.put(type, realService);
		}
		
		Context.getServiceContext().setService(type, service);
	}
	
	public void setAdministrationService(AdministrationService administrationService) {
		setService(AdministrationService.class, administrationService);
		this.administrationService = administrationService;
	}
	
	public void setCohortService(CohortService cohortService) {
		setService(CohortService.class, cohortService);
		this.cohortService = cohortService;
	}
	
	public void setConceptService(ConceptService conceptService) {
		setService(ConceptService.class, conceptService);
		this.conceptService = conceptService;
	}
	
	public void setDatatypeService(DatatypeService datatypeService) {
		setService(DatatypeService.class, datatypeService);
		this.datatypeService = datatypeService;
	}
	
	public void setEncounterService(EncounterService encounterService) {
		setService(EncounterService.class, encounterService);
		this.encounterService = encounterService;
	}
	
	public void setFormService(FormService formService) {
		setService(FormService.class, formService);
		this.formService = formService;
	}
	
	public void setLocationService(LocationService locationService) {
		setService(LocationService.class, locationService);
		this.locationService = locationService;
	}
	
	public void setMessageSourceService(MessageSourceService messageSourceService) {
		setService(MessageSourceService.class, messageSourceService);
		this.messageSourceService = messageSourceService;
	}
	
	public void setObsService(ObsService obsService) {
		setService(ObsService.class, obsService);
		this.obsService = obsService;
	}
	
	public void setOrderService(OrderService orderService) {
		setService(OrderService.class, orderService);
		this.orderService = orderService;
	}
	
	public void setPatientService(PatientService patientService) {
		setService(PatientService.class, patientService);
		this.patientService = patientService;
	}
	
	public void setProviderService(ProviderService providerService) {
		setService(ProviderService.class, providerService);
		this.providerService = providerService;
	}
	
	public void setReportService(ReportService reportService) {
		setService(ReportService.class, reportService);
		this.reportService = reportService;
	}
	
	public void setSerializationService(SerializationService serializationService) {
		setService(SerializationService.class, serializationService);
		this.serializationService = serializationService;
	}
	
	public void setUserService(UserService userService) {
		setService(UserService.class, userService);
		this.userService = userService;
	}
	
	public void setVisitService(VisitService visitService) {
		setService(VisitService.class, visitService);
		this.visitService = visitService;
	}
	
	public void setUserContext(UserContext userContext) {
		if (!userContextMocked) {
			try {
				realUserContext = Context.getUserContext();
			}
			catch (Exception e) {
				//let's not fail if context is not configured
			}
			userContextMocked = true;
		}
		
		Context.setUserContext(userContext);
		this.userContext = userContext;
	}
	
}
