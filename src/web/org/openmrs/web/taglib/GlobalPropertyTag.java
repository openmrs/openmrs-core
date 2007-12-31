package org.openmrs.web.taglib;

import java.util.Arrays;
import java.util.Collections;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class GlobalPropertyTag extends TagSupport {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Log log = LogFactory.getLog(getClass());
	
	private String key="";
	private String defaultValue="";
	private String var=null;
	private String listSeparator=null;
	
	public int doStartTag() {

		Object value;
		if (StringUtils.hasText(listSeparator))
			value = Collections.singletonList(defaultValue);
		else
			value = defaultValue;
		
		// If user is logged in
		if ( Context.isAuthenticated() ) { 
			if (StringUtils.hasText(listSeparator)) {
				String stringVal = (String) Context.getAdministrationService().getGlobalProperty(key, defaultValue);
				value = Arrays.asList(stringVal.split(listSeparator));
			} else
				value = (String) Context.getAdministrationService().getGlobalProperty(key, defaultValue);
		}
		
		try {
			if (var != null)
				pageContext.setAttribute(var, value);
			else
				pageContext.getOut().write(value.toString());

		} catch (Exception e) { 
			log.error("error getting global property", e);
		}
		return SKIP_BODY;
	}

	
	public String getKey() { 
		return this.key;
	}
	
	public void setKey(String key) { 
		this.key = key;
	}
	
	public String getDefaultValue() { 
		return this.defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) { 
		this.defaultValue = defaultValue;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}
	
}
