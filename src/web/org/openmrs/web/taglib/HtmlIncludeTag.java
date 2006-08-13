// TODO: This class is not fully working right now...  stupid bug...
package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HtmlIncludeTag extends TagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());

	private static final String POSSIBLE_TYPES_JS = ".js,javascript,jscript";
	private static final String POSSIBLE_TYPES_CSS = ".css,style,stylesheet";
	public static final String OPENMRS_HTML_INCLUDE_KEY = "org.openmrs.htmlInclude.includeMap";
		
	private String type;
	private String file;
	private HttpServletRequest request;
	private HashMap<String,String> hmIncludeMap;
	
	public int doStartTag() throws JspException {

		// see if this is a JS or CSS file
		boolean isJs = false;
		boolean isCss = false;
		
		String fileExt = file.substring(file.lastIndexOf("."));
		
		if ( this.type != null ) {
			if ( this.type.length() > 0 ) {
				if ( this.POSSIBLE_TYPES_CSS.indexOf(type) >= 0 ) isCss = true;
				else if ( this.POSSIBLE_TYPES_JS.indexOf(type) >= 0 ) isJs = true;
			}
		}

		if ( !isCss && !isJs && fileExt.length() > 0 ) {
			if ( this.POSSIBLE_TYPES_CSS.indexOf(fileExt) >= 0 ) isCss = true;
			else if ( this.POSSIBLE_TYPES_JS.indexOf(fileExt) >= 0 ) isJs = true;
		}

		if ( isJs || isCss ) {
			if ( !isAlreadyUsed(file) ) {
				String output = "";
				String prefix = "";
				try {
					prefix = getRequest().getContextPath();
					if ( file.startsWith(prefix) ) prefix = "";
				} catch (ClassCastException cce) {
					log.debug("Could not cast request to HttpServletRequest in HtmlIncludeTag");
				}
					
				if ( isJs ) {
					output = "<script src=\"" + prefix + file + "\" ></script>";
				} else if ( isCss ) {
					output = "<link href=\"" + prefix + file + "\" type=\"text/css\" rel=\"stylesheet\" />";
				}
				
				try {
					pageContext.getOut().print(output);
				} catch (IOException e) {
					log.error("Could not produce output in HtmlIncludeTag.java");
				}
			}
		}
		
		resetValues();
		
		return SKIP_BODY;
	}
	
	private HttpServletRequest getRequest() {
		if ( this.request == null ) {
			//log.debug("Using pageContext request of " + this.pageContext.getRequest().toString());
			return (HttpServletRequest)this.pageContext.getRequest();
		} else {
			//log.debug("Using passed-in request of " + this.request.toString());
			return this.request;
		}
	}
	
	private boolean isAlreadyUsed(String fileName) {
		boolean isUsed = false;

		if ( fileName != null ) {
			if ( this.hmIncludeMap == null ) {
				
				
				hmIncludeMap = (HashMap<String, String>) getRequest().getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY);
				if ( hmIncludeMap == null ) {
					hmIncludeMap = (HashMap<String, String>)pageContext.getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY, PageContext.REQUEST_SCOPE);
					if ( hmIncludeMap == null ) {
						hmIncludeMap = new HashMap<String,String>();
						//log.debug("Using blank Map");
					} //else log.debug("Using map from pageContext");
				} //else log.debug("Using map from getRequest()");
			} //else log.debug("Using map from object");
			
			
			//log.debug("HtmlIncludeTag has request of " + getRequest().toString());
			//log.debug("HtmlIncludeTag has default request of " + pageContext.getRequest().toString());
			
			if ( hmIncludeMap.containsKey(fileName) ) {
				//log.debug("HTMLINCLUDETAG HAS ALREADY INCLUDED FILE " + fileName);
				isUsed = true;
			} else {
				//log.debug("HTMLINCLUDETAG IS WRITING HTML TO INCLUDE FILE " + fileName);
				//log.debug("HashCode for file is " + fileName.hashCode());
				hmIncludeMap.put(fileName,"true");
				getRequest().setAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY, hmIncludeMap.clone());
				pageContext.setAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY, hmIncludeMap.clone(), PageContext.REQUEST_SCOPE);
			}
			
		}
		
		return isUsed;
	}

	private void resetValues() {
		this.type = null;
		this.file = null;
		this.hmIncludeMap = null;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return Returns the file.
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param file The file to set.
	 */
	public void setFile(String file) {
		this.file = file;
		if ( file != null ) this.file = file.trim();
	}

	/**
	 * @param request The request to set.
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

}
