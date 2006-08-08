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

public class HtmlIncludeTag extends TagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());

	private static final String POSSIBLE_TYPES_JS = ".js,javascript,jscript";
	private static final String POSSIBLE_TYPES_CSS = ".css,style,stylesheet";
	private static final String OPENMRS_HTML_INCLUDE_KEY = "org.openmrs.htmlInclude.includeMap";
		
	private String type;
	private String file;
	private HashMap<String,String> hmIncludeMap;
	
	public PageContext getPageContext() {
		return this.pageContext;
	}
	
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
					HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
					prefix = request.getContextPath();
				} catch (ClassCastException cce) {
					log.debug("Could not cast request to HttpServletRequest in HtmlIncludeTag");
				}
					
				if ( isJs ) {
					output = "<script src=\"" + prefix + file + "\" ></script>";
				} else if ( isCss ) {
					output = "<link rel=\"" + prefix + file + "\" />";
				}
				
				try {
					pageContext.getOut().print(output);
				} catch (IOException e) {
					log.equals("Could not produce output in HtmlIncludeTag.java");
				}
			}
		}
		
		resetValues();
		
		return SKIP_BODY;
	}

	private boolean isAlreadyUsed(String fileName) {
		boolean isUsed = false;
		
		if ( fileName != null ) {
			if ( this.hmIncludeMap == null ) {
				hmIncludeMap = (HashMap<String, String>) pageContext.getAttribute("bobby");
				if ( hmIncludeMap == null ) {
					hmIncludeMap = new HashMap<String,String>();
				}
			}
			
			if ( hmIncludeMap.containsKey(fileName) ) {
				isUsed = true;
			} else {
				hmIncludeMap.put(fileName,"true");
				pageContext.setAttribute("bobby", hmIncludeMap);
			}
			
		}

		return isUsed;
	}

	private void resetValues() {
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
	}

}
