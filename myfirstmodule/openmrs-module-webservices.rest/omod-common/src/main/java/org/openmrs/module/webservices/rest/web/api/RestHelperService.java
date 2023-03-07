/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.api;

import org.openmrs.ConceptMap;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * It is provided as a workaround for missing API methods to fetch {@link ConceptMap}, etc.
 */
public interface RestHelperService {
	
	<T> T getObjectByUuid(Class<? extends T> type, String uuid);
	
	<T> T getObjectById(Class<? extends T> type, Serializable id);
	
	<T> List<T> getObjectsByFields(Class<? extends T> type, Field... fields);
	
	List<Patient> getPatients(Collection<Integer> patientIds);
	
	@Authorized({ "View Patients" })
	List<Patient> findPatientsByIdentifierStartingWith(String identifier, boolean includeAll);
	
	List<SearchHandler> getRegisteredSearchHandlers();
	
	List<DelegatingSubclassHandler> getRegisteredRegisteredSubclassHandlers();
	
	public static class Field {
		
		private final String name;
		
		private final Object value;
		
		public Field(String name, Object value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public Object getValue() {
			return value;
		}
	}
	
}
