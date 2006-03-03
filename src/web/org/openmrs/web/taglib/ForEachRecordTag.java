package org.openmrs.web.taglib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;


public class ForEachRecordTag extends BodyTagSupport {

	public static final long serialVersionUID = 1232300L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String name;
	private Object select;
	private Iterator records;

	public int doStartTag() {
		
		records = null;
		
		Context context = (Context)pageContext.getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Locale locale = context.getLocale();
		
		if (name.equals("patientIdentifierType")) {
			PatientService ps = context.getPatientService();
			records = ps.getPatientIdentifierTypes().iterator();
		}
		else if (name.equals("location")) {
			EncounterService es = context.getEncounterService();
			records = es.getLocations().iterator();
		}
		else if (name.equals("tribe")) {
			PatientService ps = context.getPatientService();
			records = ps.getTribes().iterator();
		}
		else if (name.equals("civilStatus")) {
			ConceptService cs = context.getConceptService();
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
		else {
			log.error(name + " not found in ForEachRecord list");
		}
		
		
		if (records == null)
			return SKIP_BODY;
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
	
	private void iterate(Object obj) {
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

	/**
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		try
        {
            if(bodyContent != null)
            	bodyContent.writeOut(bodyContent.getEnclosingWriter());
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
}
