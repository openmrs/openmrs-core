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

	public static final long serialVersionUID = 13472382822L;
	
	private final Log log = LogFactory.getLog(getClass());

	private static final String POSSIBLE_TYPES_JS = ".js,javascript,jscript";
	private static final String POSSIBLE_TYPES_CSS = ".css,style,stylesheet";
	public static final String OPENMRS_HTML_INCLUDE_PAGE_NAME_KEY = "org.openmrs.htmlInclude.pageName";
	public static final String OPENMRS_HTML_INCLUDE_KEY = "org.openmrs.htmlInclude.includeMap";
		
	private String type;
	private String file;
	
	public int doStartTag() throws JspException {
		log.debug("\n\n");
		
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
			HttpServletRequest initialRequest = getInitialRequest();
			
			log.debug("initialRequest uri: [" + initialRequest.getRequestURI() + "]");
			log.debug("initialRequest(): [" + initialRequest + "]");
			log.debug("Object at " + HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY + " is " + 
					initialRequest.getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY) + "");
			
			if ( !isAlreadyUsed(file, initialRequest) ) {
				String output = "";
				String prefix = "";
				try {
					prefix = initialRequest.getContextPath();
					if ( file.startsWith(prefix + "/") ) prefix = "";
				} catch (ClassCastException cce) {
					log.debug("Could not cast request to HttpServletRequest in HtmlIncludeTag");
				}
					
				if ( isJs ) {
					output = "<script src=\"" + prefix + file + "\" ></script>";
				} else if ( isCss ) {
					output = "<link href=\"" + prefix + file + "\" type=\"text/css\" rel=\"stylesheet\" />";
				}

				log.debug("isAlreadyUsed() is FALSE - printing " + this.file + " to output.");

				try {
					pageContext.getOut().print(output);
				} catch (IOException e) {
					log.debug("Could not produce output in HtmlIncludeTag.java");
				}
			} else {
				log.debug("isAlreadyUsed() is TRUE - suppressing file print for " + this.file + "");
			}
		}
		
		resetValues();
		
		return SKIP_BODY;
	}
	
	private HttpServletRequest getInitialRequest() {
		HttpServletRequest pageRequest = (HttpServletRequest)this.pageContext.getRequest();
		if ( pageRequest.getAttribute(OpenmrsFilter.INIT_REQ_ATTR_NAME) != null ) {
			HttpServletRequest initRequest = (HttpServletRequest)pageRequest.getAttribute(OpenmrsFilter.INIT_REQ_ATTR_NAME);
			log.debug("Returning initial request: " + initRequest.toString());
			return initRequest;
		} else {
			log.debug("Using pageContext request of " + pageRequest.toString());
			return pageRequest;
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean isAlreadyUsed(String fileName, HttpServletRequest initialRequest) {
		boolean isUsed = false;

		if ( fileName != null ) {
			
			// retrieve the current page name from the initial request
			String initialPageName = initialRequest.getRequestURI();
			
			// retrieve the page name that the last mapping was added for
			String lastPageUsed = (String)pageContext.getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_PAGE_NAME_KEY, PageContext.SESSION_SCOPE);
			
			// retrieve the htmlinclude map from the page request
			//HashMap<String,String> hmIncludeMap = (HashMap<String, String>) initialRequest.getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY);
			HashMap<String,String> hmIncludeMap = (HashMap<String, String>) pageContext.getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY, PageContext.SESSION_SCOPE);

			// reset the hmIncludeMap if not found or if not on the initial request anymore
			if ( hmIncludeMap == null || !initialPageName.equals(lastPageUsed)) {
				log.debug("Creating new hmIncludeMap");
				hmIncludeMap = new HashMap<String,String>();
			} 
			else 
				log.debug("Using hmIncludeMap from object");
			
			if ( hmIncludeMap.containsKey(fileName) ) {
				log.debug("HTMLINCLUDETAG HAS ALREADY INCLUDED FILE " + fileName);
				isUsed = true;
			} else {
				log.debug("HTMLINCLUDETAG IS WRITING HTML TO INCLUDE FILE " + fileName);
				log.debug("HashCode for file is " + fileName.hashCode());
				
				hmIncludeMap.put(fileName,"true");
				
				// save the hmIncludeMap to the  
				pageContext.setAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY, hmIncludeMap, PageContext.SESSION_SCOPE);
				
				// save the name of the initial page 
				pageContext.setAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_PAGE_NAME_KEY, initialPageName, PageContext.SESSION_SCOPE);
			}
		}
		
		return isUsed;
	}

	private void resetValues() {
		log.debug("resetting values");
		this.type = null;
		this.file = null;
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

}
