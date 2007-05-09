package org.openmrs.module;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An extension is a small snippet of code that is run at certain
 * "extension points" throughout the user interface
 * 
 * An extension is not necessarily tied to only one certain point.  If 
 * all of the need return values are defined it can be used to extend 
 * any point.
 * 
 * A module can contain many extensions for many different points.
 * 
 * @author Ben Wolfe
 */

public abstract class Extension {

	private Log log = LogFactory.getLog(this.getClass());
	
	// point which this extension is extending 
	private String pointId;
	
	// id of the module implementing this point
	private String moduleId;
	
	// parameters given at the extension point
	private Map<String, String> parameterMap;
	
	public enum MEDIA_TYPE { html }
	
	/**
	 * default constructor
	 */
	public Extension() { }
	
	/**
	 * Called before being displayed each time 
	 * @param parameterMap
	 */
	public void initialize(Map<String, String> parameterMap) {
		log.debug("Initializing extension for point: " + pointId);
		this.setPointId(pointId);
		this.setParameterMap(parameterMap);
	}

	public String getPointId() {
		return pointId;
	}
	
	public void setPointId(String pointId) {
		this.pointId = pointId;
	}

	public Map<String, String> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}
	
	/**
	 * Sets the content type of this extension.  If null is returned 
	 * this extension should work across all medium types
	 * 
	 * @return type of the medium that this extension works for
	 */
	public abstract Extension.MEDIA_TYPE getMediaType();
	
	public String getExtensionId() {
		if (getMediaType() != null && getMediaType() != null)
			return getPointId() + "|" + getMediaType();
		else
			return getPointId();
	}
	
	/**
	 * If this method returns a non-null value then the return value
	 * will be used as the default content for this extension at this 
	 * extension point 
	 * 
	 * @return override content
	 */
	public String getOverrideContent(String bodyContent) {
		return null;
	}
	
	/**
	 * Get this extension's module id
	 * @return
	 */
	public final String getModuleId() {
		return moduleId;
	}
	
	/**
	 * Set the module id of this extension
	 * @param moduleId
	 */
	public final void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	
	public final String toString() {
		return "Extension: " + this.getExtensionId();
	}
}
