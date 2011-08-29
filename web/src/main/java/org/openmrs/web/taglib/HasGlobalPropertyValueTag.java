package org.openmrs.web.taglib;

import javax.servlet.jsp.tagext.TagSupport;

import org.openmrs.api.context.Context;

/**
 * Controller for the <openmrs:hasGlobalPropertyValue> taglib used on jsp pages. This taglib checks
 * for the value of a given global property and includes the body of this tag if the saved value is
 * the same as that passed in the tag's value attribute. <br/>
 * <br/>
 * Example use case:
 * 
 * <pre>
 * &lt;openmrs:hasGlobalPropertyValue name="dashboard.enableVisits" value="true" defaultValue="true" &gt;
 * </pre>
 */
public class HasGlobalPropertyValueTag extends TagSupport {
	
	public static final long serialVersionUID = 1L;
	
	private String name;
	
	private String value;
	
	private String defaultValue;
	
	public int doStartTag() {
		
		if (value.equalsIgnoreCase(Context.getAdministrationService().getGlobalPropertyValue(name, defaultValue))) {
			return EVAL_BODY_INCLUDE;
		} else {
			return SKIP_BODY;
		}
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * @return Returns the default value.
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * @param defaultValue The default value to set.
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
