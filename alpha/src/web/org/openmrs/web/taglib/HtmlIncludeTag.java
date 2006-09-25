// TODO: This class is not fully working right now...  stupid bug...
package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.web.OpenmrsFilter;

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
				if ( HtmlIncludeTag.POSSIBLE_TYPES_CSS.indexOf(type) >= 0 ) isCss = true;
				else if ( HtmlIncludeTag.POSSIBLE_TYPES_JS.indexOf(type) >= 0 ) isJs = true;
			}
		}

		if ( !isCss && !isJs && fileExt.length() > 0 ) {
			if ( HtmlIncludeTag.POSSIBLE_TYPES_CSS.indexOf(fileExt) >= 0 ) isCss = true;
			else if ( HtmlIncludeTag.POSSIBLE_TYPES_JS.indexOf(fileExt) >= 0 ) isJs = true;
		}

		if ( isJs || isCss ) {
			HttpServletRequest request = getRequest();
			HttpServletRequest pageRequest = (HttpServletRequest) pageContext.getRequest();
			
			//log.debug("\n\n[" + request.getRequestURI() + "/" + request.getRequestURL() + "] [" + pageRequest.getRequestURI() + "/" + pageRequest.getRequestURL() + "] [" + request + "], Object at " + HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY + " is " + request.getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY) + "");

			if ( !isAlreadyUsed(file) ) {
				String output = "";
				String prefix = "";
				try {
					prefix = getRequest().getContextPath();
					if ( file.startsWith(prefix + "/") ) prefix = "";
				} catch (ClassCastException cce) {
					log.debug("Could not cast request to HttpServletRequest in HtmlIncludeTag");
				}
					
				if ( isJs ) {
					output = "<script src=\"" + prefix + file + "\" ></script>";
				} else if ( isCss ) {
					output = "<link href=\"" + prefix + file + "\" type=\"text/css\" rel=\"stylesheet\" />";
				}

				//log.debug("isAlreadyUsed() is FALSE - printing " + this.file + " to output.");

				try {
					pageContext.getOut().print(output);
				} catch (IOException e) {
					log.error("Could not produce output in HtmlIncludeTag.java");
				}
			} else {
				//log.debug("isAlreadyUsed() is TRUE - suppressing file print for " + this.file + "");
			}
		}
		
		resetValues();
		
		return SKIP_BODY;
	}
	
	private HttpServletRequest getRequest() {
		HttpServletRequest pageRequest = (HttpServletRequest)this.pageContext.getRequest();
		if ( this.pageContext.getRequest().getAttribute(OpenmrsFilter.INIT_REQ_ATTR_NAME) != null ) {
			HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest().getAttribute(OpenmrsFilter.INIT_REQ_ATTR_NAME);
			return (HttpServletRequest)this.pageContext.getRequest().getAttribute(OpenmrsFilter.INIT_REQ_ATTR_NAME);
		} else {
			if ( this.request == null ) {
				//log.debug("Using pageContext request of " + this.pageContext.getRequest().toString());
				return (HttpServletRequest)this.pageContext.getRequest();
			} else {
				//log.debug("Using passed-in request of " + this.request.toString());
				return this.request;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private synchronized boolean isAlreadyUsed(String fileName) {
		boolean isUsed = false;

		if ( fileName != null ) {
			
			HashMap<String,String> hmIncludeMap = (HashMap<String, String>) getRequest().getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY);

			if ( hmIncludeMap == null ) {
				hmIncludeMap = new HashMap<String,String>();
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
				getRequest().setAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY, hmIncludeMap);
				//pageContext.setAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY, hmIncludeMap.clone(), PageContext.REQUEST_SCOPE);
				//this.hmIncludeMap = hmIncludeMap;
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
