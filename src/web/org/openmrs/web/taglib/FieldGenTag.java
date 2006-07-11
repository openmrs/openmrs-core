package org.openmrs.web.taglib;

import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.web.taglib.fieldgen.FieldGenHandler;
import org.openmrs.web.taglib.fieldgen.FieldGenHandlerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class FieldGenTag extends TagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());

	public static final String DEFAULT_INPUT_TEXT_LENGTH = "20";
	public static final String DEFAULT_INPUT_INT_LENGTH = "8";
	public static final String DEFAULT_INPUT_FLOAT_LENGTH = "12";
	public static final String DEFAULT_INPUT_CHAR_LENGTH = "2";
	public static final String DEFAULT_CONCEPT_NAME_LENGTH = "40";
	public static final String DEFAULT_INPUT_DATE_LENGTH = "10";
		
	private String type;
	private String formFieldName;
	private String fieldLength;
	private String startVal;
	private String forceInputType;
	private String isNullable;
	private String hasLabelBefore;
	private String trueLabel;
	private String falseLabel;
	private String unknownLabel;

	public PageContext getPageContext() {
		return this.pageContext;
	}
	
	public int doStartTag() {
		if (type == null) type = "";
		if (formFieldName == null) formFieldName = "";
		
		String output = "Cannot handle type [" + type + "]. Please add a module to handle this type.";
		
		if ( formFieldName.length() > 0 ) {
			FieldGenHandler handler = getHandlerByClassName(type);
			if ( handler != null ) {
				handler.setFieldGenTag(this);
				output = handler.getOutput(output);
			} else {
				if ( type.indexOf("java.lang.String") >= 0 ) {
					startVal = (startVal == null) ? "" : startVal;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_TEXT_LENGTH : fieldLength;
					output = "<input type=\"text\" name=\"" + formFieldName + "\" value=\"" + startVal + "\" size=\"" + fieldLength + "\" />";
				} else if ( type.equals("char") || type.indexOf("java.lang.Character") >= 0 ) {
					startVal = (startVal == null) ? "" : startVal;
					if ( startVal.length() > 1 ) startVal = startVal.substring(0, 1);
					
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_CHAR_LENGTH : fieldLength;
					output = "<input type=\"text\" name=\"" + formFieldName + "\" value=\"" + startVal + "\" size=\"" + fieldLength + "\" maxlength=\"1\" />";
				} else if ( type.indexOf("java.util.Date") >= 0 ) {
					startVal = startVal == null ? "" : startVal;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_DATE_LENGTH : fieldLength;
					output = "<input id=\"" + formFieldName + "\" type=\"text\" name=\"" + formFieldName + "\" value=\"" + startVal + "\" onClick=\"javascript:showCalendar(this);\" /> ";
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
					startVal = (startVal == null) ? "" : startVal;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_INT_LENGTH : fieldLength;
					output = "<input type=\"text\" name=\"" + formFieldName + "\" value=\"" + startVal + "\" size=\"" + fieldLength + "\" />";
				} else if ( type.equals("float") || type.indexOf("java.lang.Float") >= 0
						|| type.equals("double") || type.indexOf("java.lang.Double") >= 0
						|| type.indexOf("java.lang.Number") >= 0 ) {
					startVal = (startVal == null) ? "" : startVal;
					fieldLength = (fieldLength == null) ? DEFAULT_INPUT_FLOAT_LENGTH : fieldLength;
					output = "<input type=\"text\" name=\"" + formFieldName + "\" value=\"" + startVal + "\" size=\"" + fieldLength + "\" />";				
				} else if ( type.equals("boolean") || type.indexOf("java.lang.Boolean") >= 0 ) {
					startVal = (startVal == null) ? "" : startVal.toLowerCase();
					if ("false".equals(startVal) || "0".equals(startVal)) startVal = "f";
					if ("true".equals(startVal) || "1".equals(startVal)) startVal = "t";
					if ("unknown".equals(startVal) || "?".equals(startVal)) startVal = "u";
					
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
				}
			}
		}
		
		try {
			pageContext.getOut().write(output);
		}
		catch (IOException e) {
			log.error(e);
		}
		
		resetValues();
		
		return SKIP_BODY;
	}
	
	private void resetValues() {
		type = null;
		formFieldName = null;
		fieldLength = null;
		startVal = null;
		forceInputType = null;
		isNullable = null;
		hasLabelBefore = null;
		trueLabel = null;
		falseLabel = null;
		unknownLabel = null;
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
	 * @return Returns the fieldLength.
	 */
	public String getFieldLength() {
		return fieldLength;
	}

	/**
	 * @param fieldLength The fieldLength to set.
	 */
	public void setFieldLength(String fieldLength) {
		this.fieldLength = fieldLength;
	}

	/**
	 * @return Returns the forceInputType.
	 */
	public String getForceInputType() {
		return forceInputType;
	}

	/**
	 * @param forceInputType The forceInputType to set.
	 */
	public void setForceInputType(String forceInputType) {
		this.forceInputType = forceInputType;
	}

	/**
	 * @return Returns the startVal.
	 */
	public String getStartVal() {
		return startVal;
	}

	/**
	 * @param startVal The startVal to set.
	 */
	public void setStartVal(String startVal) {
		this.startVal = startVal;
	}

	/**
	 * @return Returns the isNullable.
	 */
	public String getIsNullable() {
		return isNullable;
	}

	/**
	 * @param isNullable The isNullable to set.
	 */
	public void setIsNullable(String isNullable) {
		this.isNullable = isNullable;
	}

	/**
	 * @return Returns the hasLabelBefore.
	 */
	public String getHasLabelBefore() {
		return hasLabelBefore;
	}

	/**
	 * @param hasLabelBefore The hasLabelBefore to set.
	 */
	public void setHasLabelBefore(String hasLabelBefore) {
		this.hasLabelBefore = hasLabelBefore;
	}

	/**
	 * @return Returns the falseLabel.
	 */
	public String getFalseLabel() {
		return falseLabel;
	}

	/**
	 * @param falseLabel The falseLabel to set.
	 */
	public void setFalseLabel(String falseLabel) {
		this.falseLabel = falseLabel;
	}

	/**
	 * @return Returns the trueLabel.
	 */
	public String getTrueLabel() {
		return trueLabel;
	}

	/**
	 * @param trueLabel The trueLabel to set.
	 */
	public void setTrueLabel(String trueLabel) {
		this.trueLabel = trueLabel;
	}

	/**
	 * @return Returns the unknownLabel.
	 */
	public String getUnknownLabel() {
		return unknownLabel;
	}

	/**
	 * @param unknownLabel The unknownLabel to set.
	 */
	public void setUnknownLabel(String unknownLabel) {
		this.unknownLabel = unknownLabel;
	}

	public FieldGenHandler getHandlerByClassName(String className) {
		String handlerClassName = null;
		FieldGenHandlerFactory factory = null;
		
		try {
			//Resource beanDefinition = new ClassPathResource("/web/WEB-INF/openmrs-servlet.xml");
			//XmlBeanFactory beanFactory = new XmlBeanFactory( beanDefinition );
			//factory = (FieldGenHandlerFactory)beanFactory.getBean("fieldGenHandlerFactory");
			ApplicationContext context = new FileSystemXmlApplicationContext("file:/**/WEB-INF/openmrs-servlet.xml");
			factory = (FieldGenHandlerFactory)context.getBean("fieldGenHandlerFactory");
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
