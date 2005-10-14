package org.openmrs.web.taglib;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IncludeIfExistsTag extends TagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String page;
	public int doStartTag() throws JspTagException{
        try{
            JspWriter out = pageContext.getOut();

            ServletContext context = pageContext.getServletContext();
            HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
            ServletResponse response = pageContext.getResponse();

            String sp = request.getServletPath();
            String resourcePath =  sp.substring(0, sp.lastIndexOf("/")) + "/" + page;
            RequestDispatcher rd = request.getRequestDispatcher(resourcePath);
            rd.include(request, response);

        }catch(Exception e){
            throw new JspTagException("IncludeTag ("+page+"):"+e.toString());
        }
        return SKIP_BODY; 
	}
	
	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

}
