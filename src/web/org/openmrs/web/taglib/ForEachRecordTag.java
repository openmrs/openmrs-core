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
package org.openmrs.web.taglib;

import java.util.ArrayList;
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
import org.openmrs.Form;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Role;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
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
	private Iterator records;

	public int doStartTag() {
		
		records = null;
		
		Locale locale = Context.getLocale();
		
		if (name.equals("patientIdentifierType")) {
			PatientService ps = Context.getPatientService();
			records = ps.getPatientIdentifierTypes().iterator();
		}
		else if (name.equals("relationshipType")) {
			PersonService ps = Context.getPersonService();
			records = ps.getRelationshipTypes().iterator();
		}
		else if (name.equals("encounterType")) {
			EncounterService es = Context.getEncounterService();
			records = es.getEncounterTypes().iterator();
		}
		else if (name.equals("location")) {
			EncounterService es = Context.getEncounterService();
			records = es.getLocations().iterator();
		}
		else if (name.equals("tribe")) {
			PatientService ps = Context.getPatientService();
			records = ps.getTribes().iterator();
		}
		else if (name.equals("cohort")) {
			List<Cohort> cohorts = Context.getCohortService().getCohorts();
			records = cohorts.iterator();
		}
		else if (name.equals("form")) {
			List<Form> forms = Context.getFormService().getForms();
			records = forms.iterator();
		}
		else if (name.equals("reportObject")) {
			List ret = null;
			if (reportObjectType != null)
				ret = Context.getReportObjectService().getReportObjectsByType(reportObjectType); 
			else
				ret = Context.getReportObjectService().getAllReportObjects();
			records = ret.iterator();
		}
		else if (name.equals("civilStatus")) {
			ConceptService cs = Context.getConceptService();
			Concept civilStatus = cs.getConcept(OpenmrsConstants.CIVIL_STATUS_CONCEPT_ID);
			if (civilStatus == null)
				log.error("OpenmrsConstants.CIVIL_STATUS_CONCEPT_ID is defined incorrectly.");
			
			records = civilStatus.getAnswers().iterator();
			
			Map<String, String> opts = new HashMap<String, String>();
			for (ConceptAnswer a : civilStatus.getAnswers()) {
				opts.put(a.getAnswerConcept().getConceptId().toString(), a.getAnswerConcept().getName(locale, false).getName());
			}
			records = opts.entrySet().iterator();
			if (select != null)
				select = select.toString() + "=" + opts.get(select);
		}
		else if (name.equals("gender")) {
			Map<String, String> opts = OpenmrsConstants.GENDER();
			records = opts.entrySet().iterator();
			if (select != null)
				select = select.toString() + "=" + opts.get(select);
		}
		else if (name.equals("workflowStatus")) {
			List<ProgramWorkflowState> ret = Context.getProgramWorkflowService().getStates();
			records = ret.iterator();
		}
		else if (name.equals("workflowProgram")) {
			List<org.openmrs.Program> ret = Context.getProgramWorkflowService().getPrograms();
			records = ret.iterator();
		}
		else if (name.equals("role")) {
			List<Role> ret = Context.getUserService().getRoles();
			records = ret.iterator();
		}
		else if (name.equals("conceptSet")) {
			if (conceptSet == null)
				throw new IllegalArgumentException("Must specify conceptSet");
			Concept c = OpenmrsUtil.getConceptByIdOrName(conceptSet);
			if (c == null)
				throw new IllegalArgumentException("Can't find conceptSet " + conceptSet);
			List<Concept> list = Context.getConceptService().getConceptsInSet(c);
			records = list.iterator();
		}
		else if (name.equals("answer")) {
			if (concept == null)
				throw new IllegalArgumentException("Must specify concept");
			Concept c = OpenmrsUtil.getConceptByIdOrName(concept);
			if (c == null)
				throw new IllegalArgumentException("Can't find concept " + concept);
			if (c.getAnswers() != null)
				records = c.getAnswers().iterator();
			else
				records = new ArrayList<Concept>().iterator();
		}
		else {
			log.error(name + " not found in ForEachRecord list");
		}
		
		if (records == null || records.hasNext() == false) {
			records = null;
			return SKIP_BODY;
		}
		else
			return EVAL_BODY_BUFFERED;
		
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
        if(records.hasNext()) {
        	Object obj = records.next();
			iterate(obj);
            return EVAL_BODY_BUFFERED;
        }
        else
            return SKIP_BODY;
	}
	
	@SuppressWarnings("unchecked")
	private void iterate(Object obj) {
		if (obj != null) {
			if (name.equals("gender")) {
				Map.Entry<String, String> e = (Map.Entry<String, String>)obj;
				e.setValue(e.getValue().toLowerCase());
				obj = e;
			}
			pageContext.setAttribute("record", obj);
			pageContext.setAttribute("selected", obj.equals(select) ? "selected" : "");
			if (name.equals("civilStatus")) { //Kludge until this in the db and not a HashMap
				String str = obj.toString();
				pageContext.setAttribute("selected", str.equals(select) ? "selected" : "");
			}
		}
		else {
			pageContext.removeAttribute("record");
			pageContext.removeAttribute("selected");
		}
	}

	/**
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		try
        {
			if(getBodyContent() != null && records != null)
            	getBodyContent().writeOut(getBodyContent().getEnclosingWriter());
        }
        catch(java.io.IOException e)
        {
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
