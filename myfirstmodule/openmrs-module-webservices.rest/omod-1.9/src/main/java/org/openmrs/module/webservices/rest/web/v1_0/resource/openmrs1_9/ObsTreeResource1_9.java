/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSet;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;

@Resource(name = RestConstants.VERSION_1 + "/obstree", supportedClass = SimpleObject.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class ObsTreeResource1_9 extends BaseDelegatingResource<SimpleObject> implements Searchable {

	@Override
	public SimpleObject newDelegate() {
		throw new ResourceDoesNotSupportOperationException();
	}

	@Override
	public SimpleObject save(SimpleObject delegate) {
		throw new ResourceDoesNotSupportOperationException("obstree doesn't support to this action");
	}

	@Override
	public SimpleObject getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException("obstree doesn't support to this action");
	}

	@Override
	protected void delete(SimpleObject delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("obstree doesn't support to this action");
	}

	@Override
	public void purge(SimpleObject delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("obstree doesn't support to this action");
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		return new DelegatingResourceDescription();
	}

	@Override
	public SimpleObject search(RequestContext context) throws ResponseException {
		String patientUuid = context.getRequest().getParameter("patient");
		String conceptUuid = context.getRequest().getParameter("concept");
		String fromDateString = context.getRequest().getParameter("fromDate");
		String toDateString = context.getRequest().getParameter("toDate");
		
		Date fromDate = fromDateString != null ? (Date) ConversionUtil.convert(fromDateString, Date.class) : null;
		Date toDate = toDateString != null ? (Date) ConversionUtil.convert(toDateString, Date.class) : null;
		
		Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
		    Patient.class)).getByUniqueId(patientUuid);
		if (patient == null) {
			throw new ObjectNotFoundException("No patient found with uuid " + patientUuid);
		}
		
		Concept concept = ((ConceptResource1_9) Context.getService(RestService.class)
		        .getResourceBySupportedClass(Concept.class)).getByUniqueId(conceptUuid);
		if (concept == null) {
			throw new ObjectNotFoundException("No concept found with uuid " + conceptUuid);
		}
		
		return getObsTree(patient, concept, fromDate, toDate);
	}
	
	private SimpleObject getObsTree(Patient patient, Concept concept, Date fromDate, Date toDate) {
		
		ObsService obsService = Context.getObsService();
		ConceptService conceptService = Context.getConceptService();
		List<Person> whom = Arrays.asList((Person)patient);
		List<Map<String, Object>> setMembers = new ArrayList<Map<String, Object>>();
		
		if (concept.getSet()) {
			setMembers = getSetMembers(whom, concept.getConceptSets(), fromDate, toDate, obsService, conceptService);
		}
		else {
			setMembers.add(getObsMap(whom, concept, fromDate, toDate, obsService, conceptService));
		}

		return getSetMap(concept, setMembers);
	}
	
	private List<Map<String, Object>> getSetMembers(List<Person> whom, Collection<ConceptSet> conceptSets, 
			Date fromDate, Date toDate, ObsService obsService, ConceptService conceptService) {
		
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		
		if (conceptSets == null || conceptSets.isEmpty()) {
			return mapList;
		}
		
		for (ConceptSet conceptSet : conceptSets) {
			Concept concept = conceptSet.getConcept();
			if (concept.getSet()) {
				List<Map<String, Object>> members = getSetMembers(whom, concept.getConceptSets(), fromDate, toDate, obsService, conceptService);
				mapList.add(getSetMap(concept, members));
			}
			else {
				mapList.add(getObsMap(whom, concept, fromDate, toDate, obsService, conceptService));
			}
		}
		
		return mapList;
	}
	
	private HashMap<String, Object> getObsMap(List<Person> whom, Concept concept, Date fromDate, Date toDate, 
			ObsService obsService, ConceptService conceptService) {
		
		List<Obs> obsList = obsService.getObservations(whom, null, Arrays.asList(concept), null, null, null, 
				null, null, null, fromDate, toDate, false);
		
		List<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();
		for (Obs obs : obsList) {
			HashMap<String, String> valueMap = new HashMap<String, String>();
			valueMap.put("value", obs.getValueAsString(Context.getLocale()));
			valueMap.put("obsDatetime", obs.getObsDatetime().toString());
			mapList.add(valueMap);
		}
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("display", concept.getDisplayString());
		map.put("conceptUuid", concept.getUuid());
		map.put("datatype", concept.getDatatype().getName());
		
		if (concept.isNumeric()) {
			ConceptNumeric conceptNumeric = conceptService.getConceptNumeric(concept.getConceptId());
			fillConceptNumericMetadata(map, conceptNumeric);
		}
		
		map.put("obs", mapList);
				
		return map;
	}
	

	private SimpleObject getSetMap(Concept concept, List<Map<String, Object>> subSets) {
		SimpleObject map = new SimpleObject();
		map.put("display", concept.getDisplayString());
		map.put("subSets", subSets);
		return map;
	}
	
	private void fillConceptNumericMetadata(HashMap<String, Object> map, ConceptNumeric conceptNumeric) {
		String units = conceptNumeric.getUnits();
		if (StringUtils.isNotBlank(units)) {
			map.put("units", units);
		}
		Double hiAbsolute = conceptNumeric.getHiAbsolute();
		if (hiAbsolute != null) {
			map.put("hiAbsolute", hiAbsolute);
		}
		Double hiCritical = conceptNumeric.getHiCritical();
		if (hiCritical != null) {
			map.put("hiCritical", hiCritical);
		}
		Double hiNormal = conceptNumeric.getHiNormal();
		if (hiNormal != null) {
			map.put("hiNormal", hiNormal);
		}
		Double lowAbsolute = conceptNumeric.getLowAbsolute();
		if (lowAbsolute != null) {
			map.put("lowAbsolute", lowAbsolute);
		}
		Double lowCritical = conceptNumeric.getLowCritical();
		if (lowCritical != null) {
			map.put("lowCritical", lowCritical);
		}
		Double lowNormal = conceptNumeric.getLowNormal();
		if (lowNormal != null) {
			map.put("lowNormal", lowNormal);
		}
	}
}
