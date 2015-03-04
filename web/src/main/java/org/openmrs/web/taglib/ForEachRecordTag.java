/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Role;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

public class ForEachRecordTag extends BodyTagSupport {
	
	public static final long serialVersionUID = 1232300L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private String name;
	
	private Object select;
	
	private String reportObjectType;
	
	private String concept;
	
	private String conceptSet;
	
	private Iterator<?> records;
	
	public int doStartTag() {
		
		records = null;
		
		Locale locale = Context.getLocale();
		
		if (name.equals("patientIdentifierType")) {
			PatientService ps = Context.getPatientService();
			records = ps.getAllPatientIdentifierTypes().iterator();
		} else if (name.equals("relationshipType")) {
			PersonService ps = Context.getPersonService();
			records = ps.getAllRelationshipTypes().iterator();
		} else if (name.equals("encounterType")) {
			EncounterService es = Context.getEncounterService();
			records = es.getAllEncounterTypes().iterator();
		} else if (name.equals("location")) {
			LocationService locServ = Context.getLocationService();
			records = locServ.getAllLocations().iterator();
		} else if (name.equals("locationHierarchy")) {
			List<LocationAndDepth> locationAndDepths = new ArrayList<LocationAndDepth>();
			List<Location> locations = Context.getLocationService().getRootLocations(true);
			populateLocationAndDepthList(locationAndDepths, locations, 0);
			records = locationAndDepths.iterator();
		} else if (name.equals("cohort")) {
			List<Cohort> cohorts = Context.getCohortService().getAllCohorts();
			records = cohorts.iterator();
		} else if (name.equals("conceptSource")) {
			List<ConceptSource> conceptSources = Context.getConceptService().getAllConceptSources();
			records = conceptSources.iterator();
		} else if (name.equals("form")) {
			List<Form> forms = Context.getFormService().getAllForms();
			records = forms.iterator();
		} else if (name.equals("role")) {
			List<Role> roles = Context.getUserService().getAllRoles();
			records = roles.iterator();
		} else if (name.equals("conceptMapType")) {
			List<ConceptMapType> mapTypes = Context.getConceptService().getActiveConceptMapTypes();
			records = mapTypes.iterator();
		} else if (name.equals("civilStatus")) {
			ConceptService cs = Context.getConceptService();
			Concept civilStatus = cs.getConcept(OpenmrsConstants.CIVIL_STATUS_CONCEPT_ID);
			if (civilStatus == null) {
				log.error("OpenmrsConstants.CIVIL_STATUS_CONCEPT_ID is defined incorrectly.");
			} else {
				records = civilStatus.getAnswers(false).iterator();
				
				Map<String, String> opts = new HashMap<String, String>();
				for (ConceptAnswer a : civilStatus.getAnswers(false)) {
					opts.put(a.getAnswerConcept().getConceptId().toString(), a.getAnswerConcept().getBestName(locale)
					        .getName());
				}
				records = opts.entrySet().iterator();
				if (select != null) {
					select = select.toString() + "=" + opts.get(select);
				}
			}
		} else if (name.equals("gender")) {
			Map<String, String> opts = OpenmrsConstants.GENDER();
			records = opts.entrySet().iterator();
			if (select != null) {
				select = select.toString() + "=" + opts.get(select);
			}
		} else if (name.equals("workflowStatus")) {
			List<ProgramWorkflowState> ret = Context.getProgramWorkflowService().getStates();
			records = ret.iterator();
		} else if (name.equals("workflowProgram")) {
			List<org.openmrs.Program> ret = Context.getProgramWorkflowService().getAllPrograms();
			records = ret.iterator();
		} else if (name.equals("role")) {
			List<Role> ret = Context.getUserService().getAllRoles();
			records = ret.iterator();
		} else if (name.equals("conceptSet")) {
			if (conceptSet == null) {
				throw new IllegalArgumentException("Must specify conceptSet");
			}
			Concept c = OpenmrsUtil.getConceptByIdOrName(conceptSet);
			if (c == null) {
				throw new IllegalArgumentException("Can't find conceptSet " + conceptSet);
			}
			List<Concept> list = Context.getConceptService().getConceptsByConceptSet(c);
			records = list.iterator();
		} else if (name.equals("answer")) {
			if (concept == null) {
				throw new IllegalArgumentException("Must specify concept");
			}
			Concept c = OpenmrsUtil.getConceptByIdOrName(concept);
			if (c == null) {
				log.error("Can't find concept with name or id of: " + concept + " and so no answers will be returned");
				records = null;
			} else if (c.getAnswers(false) != null) {
				records = c.getAnswers(false).iterator();
			} else {
				records = new ArrayList<Concept>().iterator();
			}
		} else {
			try {
				Class<?> cls = Context.loadClass(name);
				Constructor<?> ct = cls.getConstructor();
				Iterable<?> iterable = (Iterable<?>) ct.newInstance();
				records = iterable.iterator();
				
			}
			catch (Exception e) {
				log.error(name + " not found in ForEachRecord list " + e);
			}
		}
		
		if (records == null || !records.hasNext()) {
			records = null;
			return SKIP_BODY;
		} else {
			return EVAL_BODY_BUFFERED;
		}
		
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.BodyTag#doInitBody()
	 */
	public void doInitBody() throws JspException {
		if (records.hasNext()) {
			Object obj = records.next();
			iterate(obj);
		}
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
	 */
	public int doAfterBody() throws JspException {
		if (records.hasNext()) {
			Object obj = records.next();
			iterate(obj);
			return EVAL_BODY_BUFFERED;
		} else {
			return SKIP_BODY;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void iterate(Object obj) {
		if (obj != null) {
			if (name.equals("gender")) {
				Map.Entry<String, String> e = (Map.Entry<String, String>) obj;
				e.setValue(e.getValue().toLowerCase());
				obj = e;
			}
			pageContext.setAttribute("record", obj);
			pageContext.setAttribute("selected", obj.equals(select) ? "selected" : "");
			if (name.equals("civilStatus")) { //Kludge until this in the db and not a HashMap
				String str = obj.toString();
				pageContext.setAttribute("selected", str.equals(select) ? "selected" : "");
			}
		} else {
			pageContext.removeAttribute("record");
			pageContext.removeAttribute("selected");
		}
	}
	
	/**
	 * @param locationAndDepths
	 * @param locations
	 * @param i counter
	 */
	private void populateLocationAndDepthList(List<LocationAndDepth> locationAndDepths, Collection<Location> locations,
	        int depth) {
		for (Location location : locations) {
			locationAndDepths.add(new LocationAndDepth(depth, location));
			if (location.getChildLocations() != null && location.getChildLocations().size() > 0) {
				populateLocationAndDepthList(locationAndDepths, location.getChildLocations(), depth + 1);
			}
			
		}
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		try {
			if (getBodyContent() != null && records != null) {
				getBodyContent().writeOut(getBodyContent().getEnclosingWriter());
			}
		}
		catch (java.io.IOException e) {
			throw new JspTagException("IO Error: " + e.getMessage());
		}
		return EVAL_PAGE;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return Returns the select.
	 */
	public Object getSelect() {
		return select;
	}
	
	/**
	 * @param select The select to set.
	 */
	public void setSelect(Object select) {
		this.select = select;
	}
	
	public String getReportObjectType() {
		return reportObjectType;
	}
	
	public void setReportObjectType(String reportObjectType) {
		this.reportObjectType = reportObjectType;
	}
	
	public String getConcept() {
		return concept;
	}
	
	public void setConcept(String concept) {
		this.concept = concept;
	}
	
	public String getConceptSet() {
		return conceptSet;
	}
	
	public void setConceptSet(String conceptSet) {
		this.conceptSet = conceptSet;
	}
	
}
