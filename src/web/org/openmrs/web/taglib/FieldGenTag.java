
package org.openmrs.web.taglib;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.taglibs.standard.tag.common.core.ImportSupport;
import org.openmrs.util.Helper;
import org.openmrs.web.taglib.fieldgen.FieldGenHandler;
import org.openmrs.web.taglib.fieldgen.FieldGenHandlerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class FieldGenTag extends ImportSupport {

	public static final long serialVersionUID = 2L;
	
	private final Log log = LogFactory.getLog(getClass());

	public static final String DEFAULT_INPUT_TEXT_LENGTH = "20";
	public static final String DEFAULT_INPUT_INT_LENGTH = "8";
	public static final String DEFAULT_INPUT_FLOAT_LENGTH = "12";
	public static final String DEFAULT_INPUT_CHAR_LENGTH = "2";
		
	private String type;
	private String formFieldName;
	private Object val;
	//private String startVal;
	private String parameters = "";
	private Map<String, Object> parameterMap = null;
	
	// should not be reset each time
	private ApplicationContext context = null;
	private FieldGenHandlerFactory factory = null;

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
	
	public int doStartTag() throws JspException {
		if (type == null) type = "";
		if (formFieldName == null) formFieldName = "";
		if (val == null) {
			//System.out.println("VAL IS NULL!!");
		} else {
			Class valClass = val.getClass();
			if ( valClass == null ) {
				//System.out.println("VALCLASS IS NULL!!");
			} else {
				//System.out.println("VAL IS " + val.getClass().getName());
			}
		}
				
		if ( formFieldName.length() > 0 ) {
			FieldGenHandler handler = getHandlerByClassName(type);
			if ( handler != null ) {
				handler.setFieldGenTag(this);
				handler.run();
			} else {
				String output = "Cannot handle type [" + type + "]. Please add a module to handle this type.";

				
				if ( type.indexOf("java.lang.String") >= 0 ) {
					String startVal = "";
					if ( val != null ) {
						startVal = val.toString();
					}
					startVal = (startVal == null) ? "" : startVal;
					String fieldLength = this.parameterMap != null ? (String)this.parameterMap.get("fieldLength") : null;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_TEXT_LENGTH : fieldLength;
					output = "<input type=\"text\" name=\"" + formFieldName + "\" value=\"" + startVal + "\" size=\"" + fieldLength + "\" />";
				} else if ( type.equals("char") || type.indexOf("java.lang.Character") >= 0 ) {
					String startVal = "";
					if ( val != null ) {
						startVal = val.toString();
					}
					startVal = (startVal == null) ? "" : startVal;
					if ( startVal.length() > 1 ) startVal = startVal.substring(0, 1);
					String fieldLength = this.parameterMap != null ? (String)this.parameterMap.get("fieldLength") : null;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_CHAR_LENGTH : fieldLength;
					output = "<input type=\"text\" name=\"" + formFieldName + "\" value=\"" + startVal + "\" size=\"" + fieldLength + "\" maxlength=\"1\" />";
				/*
				} else if ( type.indexOf("java.util.Date") >= 0 ) {

					startVal = startVal == null ? "" : startVal;
					String fieldLength = this.parameterMap != null ? (String)this.parameterMap.get("fieldLength") : null;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_DATE_LENGTH : fieldLength;
					output = "<input id=\"" + formFieldName + "\" type=\"text\" name=\"" + formFieldName + "\" value=\"" + startVal + "\" onClick=\"javascript:showCalendar(this);\" /> ";
					output += " (need a better widget than this - for now input Date as mm/dd/yyyy)";
					//output += "<a href=\"javascript:showCalendar(document.getElementById('" + formFieldName + "'));\"><img src=\"/openmrs/images/lookup.gif\" border=\"0\" /></a>";
					/*
					String startDay = "";
					String startMonth = "";
					String startYear = "";
					if ( startVal != null ) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					}
					output = "<select name=\"" + formFieldName + "_day\">";
					for (int i = 1; i <= 31; i++ ) {
						output += "<option value=\"" + i + "\"" + (startDay.equals("" + i) ? " selected" : "") + ">";
						output += "" + i;
						output += "</option>";
					}
					output += "</select> ";
					
					output += "<select name=\"" + formFieldName + "_month\">";
					for (int i = 1; i <= 12; i++ ) {
						output += "<option value=\"" + i + "\"" + (startMonth.equals("" + i) ? " selected" : "") + ">";
						output += "month_" + i;
						output += "</option>";
						//MessageTag mTag = new MessageTag();
						//mTag.setCode("general.month." + i);
	
					}
					output += "</select> " ;
					
					output += "<input type=\"text\" name=\"" + formFieldName + "_year\" value=\"" + startYear + "\" size=\"4\" />";
					*/
				} else if ( type.equals("int") || type.indexOf("java.lang.Integer") >= 0
						|| type.equals("long") || type.indexOf("java.lang.Long") >= 0 ) {
					String startVal = "";
					if ( val != null ) {
						startVal = val.toString();
						System.out.println("\n\n\nval seems to be " + val + "\n\n\n");
					} else {
						System.out.println("\n\n\nval seems to be none!!\n\n\n");
					}
					startVal = (startVal == null) ? "" : startVal;
					String fieldLength = this.parameterMap != null ? (String)this.parameterMap.get("fieldLength") : null;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_INT_LENGTH : fieldLength;
					output = "<input type=\"text\" name=\"" + formFieldName + "\" value=\"" + startVal + "\" size=\"" + fieldLength + "\" />";
				} else if ( type.equals("float") || type.indexOf("java.lang.Float") >= 0
						|| type.equals("double") || type.indexOf("java.lang.Double") >= 0
						|| type.indexOf("java.lang.Number") >= 0 ) {
					String startVal = "";
					if ( val != null ) {
						startVal = val.toString();
					}
					startVal = (startVal == null) ? "" : startVal;
					String fieldLength = this.parameterMap != null ? (String)this.parameterMap.get("fieldLength") : null;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_FLOAT_LENGTH : fieldLength;
					output = "<input type=\"text\" name=\"" + formFieldName + "\" value=\"" + startVal + "\" size=\"" + fieldLength + "\" />";				
				} else if ( type.equals("boolean") || type.indexOf("java.lang.Boolean") >= 0 ) {
					String startVal = "";
					if ( val != null ) {
						startVal = val.toString();
					}
					startVal = (startVal == null) ? "" : startVal.toLowerCase();
					if ("false".equals(startVal) || "0".equals(startVal)) startVal = "f";
					if ("true".equals(startVal) || "1".equals(startVal)) startVal = "t";
					if ("unknown".equals(startVal) || "?".equals(startVal)) startVal = "u";
					
					String forceInputType = this.parameterMap != null ? (String)this.parameterMap.get("forceInputType") : null;
					String isNullable = this.parameterMap != null ? (String)this.parameterMap.get("isNullable") : null;
					String trueLabel = this.parameterMap != null ? (String)this.parameterMap.get("trueLabel") : null;
					String falseLabel = this.parameterMap != null ? (String)this.parameterMap.get("falseLabel") : null;
					String unknownLabel = this.parameterMap != null ? (String)this.parameterMap.get("unknownLabel") : null;
					
					if ( forceInputType == null ) forceInputType = "";
					
					if ( "checkbox".equals(forceInputType) ) {
						output = "<input type=\"checkbox\" name=\"" + formFieldName + "\" value=\"t\"" + ("t".equals(startVal) ? " checked" : "") + "/> ";				
					} else {
						if ( isNullable == null ) isNullable = "";
						if ( trueLabel == null ) trueLabel = "true";
						if ( falseLabel == null ) falseLabel = "false";
						if ( unknownLabel == null ) unknownLabel = "unknown";
						
						if ( "false".equalsIgnoreCase(isNullable) || "f".equalsIgnoreCase(isNullable) || "0".equals(isNullable) ) {
							output = "<input type=\"radio\" name=\"" + formFieldName + "\" value=\"f\"" + ("f".equals(startVal) ? " checked" : "") + "/> ";
							output += falseLabel;
							output += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
							output += "<input type=\"radio\" name=\"" + formFieldName + "\" value=\"t\"" + ("t".equals(startVal) ? " checked" : "") + "/> ";
							output += trueLabel;
						} else {
							output = "<input type=\"radio\" name=\"" + formFieldName + "\" value=\"f\"" + ("f".equals(startVal) ? " checked" : "") + "/> ";
							output += falseLabel;
							output += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
							output += "<input type=\"radio\" name=\"" + formFieldName + "\" value=\"t\"" + ("t".equals(startVal) ? " checked" : "") + "/> ";
							output += trueLabel;
							output += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
							output += "<input type=\"radio\" name=\"" + formFieldName + "\" value=\"u\"" + ("u".equals(startVal) ? " checked" : "") + "/> ";
							output += unknownLabel;
						}
					}
				} else if (type.indexOf("$") >= 0) {
					// this could be an enum - if so, let's display it
					//System.out.println("\n\n\nenum is " + startVal + "\n\n\n");
					String className = type;
					try {
						Class cls = Class.forName(className);
						if (cls.isEnum()) {
							Object[] enumConstants = cls.getEnumConstants();
							
							if ( enumConstants.length > 0 ) {
								String startVal = val.toString();
								if ( startVal == null ) startVal = "";
								output = "<select name=\"" + formFieldName + "\">";
								for ( int i = 0; i < enumConstants.length; i++ ) {
									output += "<option value=\"" + enumConstants[i].toString() + "\"" + (startVal.equals(enumConstants[i].toString()) ? " selected" : "") + ">";
									output += enumConstants[i].toString();
									output += "</option>";
								}
								output += "</select> ";
							}
						}
					} catch ( Throwable t ) {
						//System.out.println(t.getStackTrace());
					}
				} // end checking different built-in types
				
				try {
					pageContext.getOut().write(output);
				}
				catch (IOException e) {
					log.error(e);
				}
			}
		}
		
		if (url == null) url = "default.field";
		
		// all fieldGens are contained in the /WEB-INF/view/fieldGen/ folder and end with .field
		if (!url.endsWith("field"))
			url += ".field";
		url = "/fieldGen/" + url;

		/*
		try {
			this.typeClass = Class.forName(this.type);
		} catch (ClassNotFoundException e) {
			this.typeClass = null;
		}
		*/
		
		// add attrs to request so that the controller (and field jsp) can see/use them
		pageContext.getRequest().setAttribute("org.openmrs.fieldGen.type", type);
		pageContext.getRequest().setAttribute("org.openmrs.fieldGen.formFieldName", formFieldName);
		pageContext.getRequest().setAttribute("org.openmrs.fieldGen.parameters", Helper.parseParameterList(parameters));
		HashMap<String,Object> hmParamMap = (HashMap<String, Object>) pageContext.getRequest().getAttribute("org.openmrs.fieldGen.parameterMap");
		if ( hmParamMap == null ) hmParamMap = new HashMap<String,Object>();
		if ( this.parameterMap != null ) hmParamMap.putAll(this.parameterMap);
		pageContext.getRequest().setAttribute("org.openmrs.fieldGen.parameterMap", hmParamMap);
		
		/*
		if ( pageContext.getRequest().getAttribute("org.openmrs.fieldGen.object") == null ) {
			if ( typeClass == null ) {
				pageContext.getRequest().setAttribute("org.openmrs.fieldGen.object", (String)val);
			} else {
				pageContext.getRequest().setAttribute("org.openmrs.fieldGen.object", typeClass.cast(val));
			}
		}
		*/
		pageContext.getRequest().setAttribute("org.openmrs.fieldGen.object", val);
		
		//System.out.println("\n\n\nURL is " + url + ", " + type + " \n\n\n");
		
		int doSuper = super.doStartTag();
		
		//System.out.println("\n\n\nURL is now " + url + ", " + type + " \n\n\n");

		//resetValues();
		
		return doSuper;
	}

	public int doEndTag() throws JspException {
		
		int i = super.doEndTag();

		resetValues();
		
		return i;
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
		if ( type.startsWith("class ")) {
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
		String delimiter = ";";
		if ( parameters.indexOf(delimiter) < 0 ) {
			delimiter = ",";
		}
		String[] nvPairs = parameters.split(delimiter);
		for ( String nvPair : nvPairs ) {
			String[] nameValue = nvPair.split("=");
			String name = nameValue[0];
			String val = nameValue[1];
			
			if ( this.parameterMap == null ) this.parameterMap = new HashMap<String,Object>();
			this.parameterMap.put(name, val);
		}
	}

	public FieldGenHandler getHandlerByClassName(String className) {
		String handlerClassName = null;
		
		try {
			//Resource beanDefinition = new ClassPathResource("/web/WEB-INF/openmrs-servlet.xml");
			//XmlBeanFactory beanFactory = new XmlBeanFactory( beanDefinition );
			//factory = (FieldGenHandlerFactory)beanFactory.getBean("fieldGenHandlerFactory");

			//ApplicationContext context = new FileSystemXmlApplicationContext("file:/**/WEB-INF/openmrs-servlet.xml");
			//if ( context == null ) context = WebApplicationContextUtils.getWebApplicationContext(this.pageContext.getServletContext());
			if ( context == null ) context = new FileSystemXmlApplicationContext("file:/**/WEB-INF/openmrs-servlet.xml");
			if ( context != null ) {
				if ( factory == null ) factory = (FieldGenHandlerFactory)context.getBean("fieldGenHandlerFactory");
			} else log.error("Could not get handle on BeanFactory from FieldGen module");
		} catch (Exception e) {
			factory = null;
			e.printStackTrace(); 
		} 
		
		if ( factory != null ) {
			handlerClassName = factory.getHandlerByClassName(className);
			
			if ( handlerClassName != null ) {
				try {
					Class cls = Class.forName(handlerClassName);
					Constructor ct = cls.getConstructor();
					FieldGenHandler handler = (FieldGenHandler)ct.newInstance();
					return handler;
				} catch (Exception e) {
					//System.out.println("Unable to handle type [" + className + "] with handler [" + handlerClassName + "].");
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
