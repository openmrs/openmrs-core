/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.taglib.fieldgen.FieldGenHandler;
import org.openmrs.web.taglib.fieldgen.FieldGenHandlerFactory;
import org.openmrs.web.taglib.fieldgen.LocationHandler;

public class FieldGenTag extends TagSupport {
	
	public static final long serialVersionUID = 21132L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	public static final String DEFAULT_INPUT_TEXT_LENGTH = "20";
	
	public static final String DEFAULT_INPUT_INT_LENGTH = "8";
	
	public static final String DEFAULT_INPUT_FLOAT_LENGTH = "12";
	
	public static final String DEFAULT_INPUT_CHAR_LENGTH = "2";
	
	private String type;
	
	private String formFieldName;
	
	private Object val;
	
	private String url;
	
	private String parameters = "";
	
	private Map<String, Object> parameterMap = null;
	
	private Boolean allowUserDefault = Boolean.FALSE;
	
	// should not be reset each time
	private FieldGenHandlerFactory factory = null;
	
	private Map<String, FieldGenHandler> fieldGenHandlerCache = new HashMap<String, FieldGenHandler>();
	
	//private String fieldLength;
	//private String forceInputType;
	//private String isNullable;
	//private String hasLabelBefore;
	//private String trueLabel;
	//private String falseLabel;
	//private String unknownLabel;
	//private String emptySelectMessage;
	//private String additionalArgs;
	//private Map<String,String> args;
	
	public PageContext getPageContext() {
		return this.pageContext;
	}
	
	@SuppressWarnings("unchecked")
	public int doStartTag() throws JspException {
		
		if (type == null) {
			type = "";
		}
		if (formFieldName == null) {
			formFieldName = "";
		}
		
		if (formFieldName.length() > 0) {
			FieldGenHandler handler = getHandlerByClassName(type);
			if (handler != null) {
				handler.setFieldGenTag(this);
				handler.run();
			} else {
				StringBuilder output = new StringBuilder("Cannot handle type [" + type
				        + "]. Please add a module to handle this type.");
				
				if (type.equals("char") || type.indexOf("java.lang.Character") >= 0) {
					String startVal = "";
					if (val != null) {
						startVal = val.toString();
					}
					if (startVal.length() > 1) {
						startVal = startVal.substring(0, 1);
					}
					String fieldLength = this.parameterMap != null ? (String) this.parameterMap.get("fieldLength") : null;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_CHAR_LENGTH : fieldLength;
					output.setLength(0);
					output.append("<input type=\"text\" name=\"").append(formFieldName).append("\" id=\"").append(
					    formFieldName).append("\" value=\"");
					output.append(startVal).append("\" size=\"").append(fieldLength).append("\" maxlength=\"1\" />");
				} else if (type.equals("int") || type.indexOf("java.lang.Integer") >= 0 || type.equals("long")
				        || type.indexOf("java.lang.Long") >= 0) {
					String startVal = "";
					if (val != null) {
						startVal = val.toString();
					}
					String fieldLength = this.parameterMap != null ? (String) this.parameterMap.get("fieldLength") : null;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_INT_LENGTH : fieldLength;
					output.setLength(0);
					output.append("<input type=\"text\" name=\"").append(formFieldName).append("\" id=\"").append(
					    formFieldName).append("\" value=\"");
					output.append(startVal).append("\" size=\"").append(fieldLength).append("\" />");
				} else if (type.equals("float") || type.indexOf("java.lang.Float") >= 0 || type.equals("double")
				        || type.indexOf("java.lang.Double") >= 0 || type.indexOf("java.lang.Number") >= 0) {
					String startVal = "";
					if (val != null) {
						startVal = val.toString();
					}
					
					String fieldLength = this.parameterMap != null ? (String) this.parameterMap.get("fieldLength") : null;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_FLOAT_LENGTH : fieldLength;
					output.setLength(0);
					output.append("<input type=\"text\" name=\"").append(formFieldName).append("\" id=\"").append(
					    formFieldName).append("\" value=\"");
					output.append(startVal).append("\" size=\"").append(fieldLength).append("\" />");
				} else if (type.equals("boolean") || type.indexOf("java.lang.Boolean") >= 0) {
					String startVal = "";
					if (val != null) {
						startVal = val.toString();
					}
					startVal = (startVal == null) ? "" : startVal.toLowerCase();
					if ("false".equals(startVal) || "0".equals(startVal)) {
						startVal = "false";
					}
					if ("true".equals(startVal) || "1".equals(startVal)) {
						startVal = "true";
					}
					if ("unknown".equals(startVal) || "?".equals(startVal)) {
						startVal = "unknown";
					}
					
					String forceInputType = this.parameterMap != null ? (String) this.parameterMap.get("forceInputType")
					        : null;
					String isNullable = this.parameterMap != null ? (String) this.parameterMap.get("isNullable") : null;
					String trueLabel = this.parameterMap != null ? (String) this.parameterMap.get("trueLabel") : null;
					String falseLabel = this.parameterMap != null ? (String) this.parameterMap.get("falseLabel") : null;
					String unknownLabel = this.parameterMap != null ? (String) this.parameterMap.get("unknownLabel") : null;
					
					if (forceInputType == null) {
						forceInputType = "";
					}
					
					if ("checkbox".equals(forceInputType)) {
						output.setLength(0);
						output.append("<input type=\"checkbox\" name=\"").append(formFieldName).append("\" id=\"").append(
						    formFieldName);
						output.append("\" value=\"true\"").append(("true".equals(startVal) ? " checked" : "")).append("/> ");
					} else {
						if (isNullable == null) {
							isNullable = "";
						}
						if (trueLabel == null) {
							trueLabel = Context.getMessageSourceService().getMessage("general.yes");
						}
						if (falseLabel == null) {
							falseLabel = Context.getMessageSourceService().getMessage("general.no");
						}
						if (unknownLabel == null) {
							unknownLabel = Context.getMessageSourceService().getMessage("general.unknown");
						}
						
						if ("false".equalsIgnoreCase(isNullable) || "f".equalsIgnoreCase(isNullable)
						        || "0".equals(isNullable)) {
							output.setLength(0);
							output.append("<input type=\"radio\" name=\"").append(formFieldName).append("\" id=\"").append(
							    formFieldName);
							output.append("_f\" value=\"false\"").append(("false".equals(startVal) ? " checked" : ""))
							        .append("/> ");
							output.append(falseLabel);
							output.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
							output.append("<input type=\"radio\" name=\"").append(formFieldName).append("\" id=\"").append(
							    formFieldName);
							output.append("_t\" value=\"true\"").append(("true".equals(startVal) ? " checked" : "")).append(
							    "/> ");
							output.append(trueLabel);
						} else {
							output.setLength(0);
							output.append("<input type=\"radio\" name=\"").append(formFieldName).append("\" id=\"").append(
							    formFieldName);
							output.append("_f\" value=\"false\"").append(("false".equals(startVal) ? " checked" : ""))
							        .append("/> ");
							output.append(falseLabel);
							output.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
							output.append("<input type=\"radio\" name=\"").append(formFieldName).append("\" id=\"").append(
							    formFieldName);
							output.append("_t\" value=\"true\"").append(("true".equals(startVal) ? " checked" : "")).append(
							    "/> ");
							output.append(trueLabel);
							output.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
							output.append("<input type=\"radio\" name=\"").append(formFieldName).append("\" id=\"").append(
							    formFieldName);
							output.append("_u\" value=\"unknown\"").append(("unknown".equals(startVal) ? " checked" : ""))
							        .append("/> ");
							output.append(unknownLabel);
						}
					}
				} else if (type.indexOf("$") >= 0) {
					// this could be an enum - if so, let's display it
					String className = type;
					
					Class cls = null;
					try {
						cls = Class.forName(className);
					}
					catch (Exception e) {
						cls = null;
						log.error("Could not instantiate class for this enum of class name [" + className
						        + "] in FieldGenTag");
					}
					
					if (cls != null && cls.isEnum()) {
						Object[] enumConstants = cls.getEnumConstants();
						
						if (enumConstants != null && enumConstants.length > 0) {
							String startVal = "";
							if (val != null) {
								startVal = val.toString();
							}
							log.debug("val is " + val);
							log.debug("val.toString is " + startVal);
							if (startVal == null) {
								startVal = "";
							}
							output.setLength(0);
							output.append("<select name=\"").append(formFieldName).append("\" id=\"").append(formFieldName)
							        .append("\">");
							StringBuilder options = new StringBuilder();
							for (int i = 0; i < enumConstants.length; i++) {
								options.append("<option value=\"").append(enumConstants[i].toString()).append("\"").append(
								    startVal.equals(enumConstants[i].toString()) ? " selected" : "").append(">").append(
								    enumConstants[i].toString()).append("</option>");
							}
							output.append(options.toString());
							output.append("</select> ");
						}
					}
				} else if (type.equals("dropDownList")) {
					
					String startVal = "";
					if (val != null) {
						startVal = StringEscapeUtils.escapeHtml(val.toString());
					}
					
					String items = this.parameterMap != null ? (String) this.parameterMap.get("items") : null;
					
					output.setLength(0);
					output.append("<select name=\"").append(formFieldName).append("\" id=\"").append(formFieldName).append(
					    "\">");
					
					if (items != null && !items.isEmpty()) {
						StringBuilder options = new StringBuilder();
						for (String item : items.split(",")) {
							String escapedItem = StringEscapeUtils.escapeHtml(item);
							escapedItem = StringEscapeUtils.escapeJavaScript(escapedItem);
							options.append("<option value=\"").append(escapedItem).append("\"").append(
							    startVal.equals(escapedItem) ? " selected" : "").append(">").append(escapedItem).append(
							    "</option>");
						}
						output.append(options.toString());
					}
					
					output.append("</select> ");
				} // end checking different built-in types
				
				try {
					pageContext.getOut().write(output.toString());
				}
				catch (IOException e) {
					log.error(e);
				}
			}
		}
		
		if (url == null) {
			url = "default.field";
		}
		
		// all fieldGens are contained in the /WEB-INF/view/fieldGen/ folder and end with .field
		if (!url.endsWith("field")) {
			url += ".field";
		}
		url = "/fieldGen/" + url;
		
		// add attrs to request so that the controller (and field jsp) can see/use them
		pageContext.getRequest().setAttribute("org.openmrs.fieldGen.type", type);
		pageContext.getRequest().setAttribute("org.openmrs.fieldGen.formFieldName", formFieldName);
		pageContext.getRequest().setAttribute("org.openmrs.fieldGen.parameters", OpenmrsUtil.parseParameterList(parameters));
		HashMap<String, Object> hmParamMap = (HashMap<String, Object>) pageContext.getRequest().getAttribute(
		    "org.openmrs.fieldGen.parameterMap");
		if (hmParamMap == null) {
			hmParamMap = new HashMap<String, Object>();
		}
		if (this.parameterMap != null) {
			hmParamMap.putAll(this.parameterMap);
		}
		pageContext.getRequest().setAttribute("org.openmrs.fieldGen.parameterMap", hmParamMap);
		
		pageContext.getRequest().setAttribute("org.openmrs.fieldGen.object", val);
		pageContext.getRequest().setAttribute("org.openmrs.fieldGen.request", pageContext.getRequest());
		
		try {
			pageContext.include(this.url);
		}
		catch (ServletException e) {
			log.error("ServletException while trying to include a file in FieldGenTag", e);
		}
		catch (IOException e) {
			log.error("IOException while trying to include a file in FieldGenTag", e);
		}
		
		/*
		log.debug("FieldGenTag has reqest of " + pageContext.getRequest().toString());
		pageContext.getRequest().setAttribute("javax.servlet.include.servlet_path.fieldGen", url);
		FieldGenController fgc = new FieldGenController();
		try {
			fgc.handleRequest((HttpServletRequest)pageContext.getRequest(), (HttpServletResponse)pageContext.getResponse());
		} catch (ServletException e) {
			log.error("ServletException while attempting to pass control to FieldGenController in FieldGenTag");
		} catch (IOException e) {
			log.error("IOException while attempting to pass control to FieldGenController in FieldGenTag");
		}
		*/

		resetValues();
		
		return SKIP_BODY;
	}
	
	private void resetValues() {
		this.type = null;
		this.formFieldName = null;
		this.val = null;
		this.url = null;
		this.parameters = null;
		this.parameterMap = null;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		if (type.startsWith("class ")) {
			this.type = type.substring("class ".length());
		} else {
			this.type = type;
		}
	}
	
	/**
	 * @return Returns the formFieldName.
	 */
	public String getFormFieldName() {
		return formFieldName;
	}
	
	/**
	 * @param formFieldName The formFieldName to set.
	 */
	public void setFormFieldName(String formFieldName) {
		this.formFieldName = formFieldName;
	}
	
	/**
	 * This is the initial value or the stored value for this tag.
	 *
	 * @return Returns the startVal.
	 */
	public Object getVal() {
		return val;
	}
	
	/**
	 * @param startVal The startVal to set.
	 */
	public void setVal(Object startVal) {
		this.val = startVal;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * @return Returns the parameterMap.
	 */
	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}
	
	/**
	 * @param parameterMap The parameterMap to set.
	 */
	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}
	
	/**
	 * @return Returns the parameters.
	 */
	public String getParameters() {
		return parameters;
	}
	
	/**
	 * @param parameters The parameters to set.
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
		String delimiter = "\\|"; // pipe is a special char in regex, so need to escape it...	
		/*
		if ( parameters.indexOf(delimiter) < 0 ) {
			delimiter = ";";
		}
		*/
		String[] nvPairs = parameters.split(delimiter);
		try {
			for (String nvPair : nvPairs) {
				String[] nameValue = nvPair.split("=");
				String name = "";
				if (nameValue.length > 0) {
					name = nameValue[0];
				}
				String val = "";
				if (nameValue.length > 1) {
					val = nameValue[1];
				}
				
				if (this.parameterMap == null) {
					this.parameterMap = new HashMap<String, Object>();
				}
				this.parameterMap.put(name, val);
			}
		}
		catch (ArrayIndexOutOfBoundsException ae) {
			log.error("Out of bounds while trying to parse " + parameters + " with delimiter " + delimiter);
		}
	}
	
	/**
	 * @return the allowUserDefault
	 */
	public Boolean getAllowUserDefault() {
		return allowUserDefault;
	}
	
	/**
	 * If this is set to true, the user's stored default value for this value will be used if the
	 * {@link #getVal()} is null. <br/>
	 * <br/>
	 * Usage of this is up to the individual handlers. See {@link LocationHandler} for an example. <br/>
	 * <br/>
	 * An example of when the dev doesn't want a default value is if location is set to null by a
	 * previous user and the current user is only editing. Therefore, the
	 * FieldGenTag.java#setAllowUserDefault() should only be set to true if creating an object for
	 * the first time)
	 *
	 * @param allowUserDefault the allowUserDefault to set
	 */
	public void setAllowUserDefault(Boolean allowUserDefault) {
		this.allowUserDefault = allowUserDefault;
	}
	
	public FieldGenHandler getHandlerByClassName(String className) {
		FieldGenHandler cacheHit = fieldGenHandlerCache.get(className);
		if (cacheHit != null) {
			return cacheHit;
		}
		
		String handlerClassName = null;
		
		try {
			//Resource beanDefinition = new ClassPathResource("/web/WEB-INF/openmrs-servlet.xml");
			//XmlBeanFactory beanFactory = new XmlBeanFactory( beanDefinition );
			//factory = (FieldGenHandlerFactory)beanFactory.getBean("fieldGenHandlerFactory");
			
			//ApplicationContext context = new FileSystemXmlApplicationContext("file:/**/WEB-INF/openmrs-servlet.xml");
			//if ( context == null ) context = WebApplicationContextUtils.getWebApplicationContext(this.pageContext.getServletContext());
			//if ( context == null ) context = new FileSystemXmlApplicationContext("file:/**/WEB-INF/openmrs-servlet.xml");
			/*
			if ( context != null ) {
				if ( factory == null ) factory = (FieldGenHandlerFactory)Context.getBean("fieldGenHandlerFactory");
			} else log.error("Could not get handle on BeanFactory from FieldGen module");
			*/
			factory = FieldGenHandlerFactory.getSingletonInstance();
			
		}
		catch (Exception e) {
			factory = null;
			log.error(e);
		}
		
		if (factory != null) {
			handlerClassName = factory.getHandlerByClassName(className);
			
			if (handlerClassName != null) {
				try {
					Class<?> cls = Context.loadClass(handlerClassName);
					Constructor<?> ct = cls.getConstructor();
					FieldGenHandler handler = (FieldGenHandler) ct.newInstance();
					fieldGenHandlerCache.put(className, handler);
					return handler;
				}
				catch (Exception e) {
					log.error("Unable to handle type [" + className + "] with handler [" + handlerClassName + "]. " + e);
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
