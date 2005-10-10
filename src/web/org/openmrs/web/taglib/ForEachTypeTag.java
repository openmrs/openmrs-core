package org.openmrs.web.taglib;

import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.PatientService;
import org.openmrs.context.Context;
import org.openmrs.web.Constants;

public class ForEachTypeTag extends BodyTagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String name;
	private Iterator types;

	public int doStartTag() {
		
		types = null;
		
		Context context = (Context)pageContext.getSession().getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (name.equals("PatientIdentifierType")) {
			PatientService ps = context.getPatientService();
			types = ps.getPatientIdentifierTypes().iterator();
		}
		
		if (types == null)
			return SKIP_BODY;
		else
			return EVAL_BODY_BUFFERED;
		
	}

	/**
	 * @see javax.servlet.jsp.tagext.BodyTag#doInitBody()
	 */
	public void doInitBody() throws JspException {
		//pageContext.setAttribute("type", types.next());
	}

	/**
	 * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
	 */
	public int doAfterBody() throws JspException {
        if(types.hasNext()) {
        	pageContext.setAttribute("type", types.next());
            return EVAL_BODY_AGAIN;
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
}
