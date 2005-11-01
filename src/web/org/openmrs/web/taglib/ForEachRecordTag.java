package org.openmrs.web.taglib;

import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.context.Context;
import org.openmrs.web.Constants;

public class ForEachRecordTag extends BodyTagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String name;
	private Object select;
	private Iterator records;

	public int doStartTag() {
		
		records = null;
		
		Context context = (Context)pageContext.getSession().getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
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
			HashMap<String, String> opts = new HashMap<String, String>();
			opts.put("1", "Single");
			opts.put("2", "Married");
			opts.put("3", "Divorced");
			opts.put("4", "Widowed");
			records = opts.entrySet().iterator();
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
			pageContext.setAttribute("record", obj);
			pageContext.setAttribute("selected", obj.equals(select) ? "selected" : "");
			if (name.equals("civilStatus")) {
				String str = obj.toString();
				pageContext.setAttribute("selected", str.equals(select) ? "selected" : "");
			}
		}
	}

	/**
	 * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
	 */
	public int doAfterBody() throws JspException {
        if(records.hasNext()) {
			Object obj = records.next();
			pageContext.setAttribute("record", obj);
			pageContext.setAttribute("selected", obj.equals(select) ? "selected" : "");
			if (name.equals("civilStatus")) { //Kludge until this in the db and not a HashMap
				String str = obj.toString();
				pageContext.setAttribute("selected", str.equals(select) ? "selected" : "");
			}
            return EVAL_BODY_BUFFERED;
        }
        else
            return SKIP_BODY;
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
