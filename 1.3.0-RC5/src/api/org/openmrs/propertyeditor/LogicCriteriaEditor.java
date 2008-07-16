package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.util.LogicCriteriaBuilder;
import org.springframework.util.StringUtils;


/**
 * Property editor for Logic Criteria.  
 *
 */
public class LogicCriteriaEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Public constructor
	 *
	 */
	public LogicCriteriaEditor() {	}
	
	
	/**
	 * Sets the value object to the Logic Criteria object represented by the given text string.
	 * 
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		

		if (StringUtils.hasText(text)) {
			try {
				LogicCriteria criteria = LogicCriteriaBuilder.serialize(text);
				setValue(criteria);
			}
			catch (Exception ex) {
				log.error("Error setting value for Logic Criteria: " + text, ex);
				throw new IllegalArgumentException("LogicCriteria could not be instantiated: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	/**
	 * Gets a text string version of a Logic Criteria object.
	 * 
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	public String getAsText() {
		return LogicCriteriaBuilder.deserialize((LogicCriteria) getValue());
	}

}
