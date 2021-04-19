/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension is a small snippet of code that is run at certain "extension points" throughout the
 * user interface
 * <p>
 * An extension is not necessarily tied to only one certain point. If all of the need return values
 * are defined it can be used to extend any point. A module can contain many extensions for many
 * different points.
 */

public abstract class Extension {
	
	private static final Logger log = LoggerFactory.getLogger(Extension.class);
	
	// point which this extension is extending
	private String pointId;
	
	// id of the module implementing this point
	private String moduleId;
	
	// parameters given at the extension point
	private Map<String, String> parameterMap;
	
	/**
	 * String separating the pointId and media type in an extension id
	 *
	 * @see #toExtensionId(String, MEDIA_TYPE)
	 */
	public static final String EXTENSION_ID_SEPARATOR = "|";
	
	/**
	 * All media types allowed by the module extension system. If an extension specifies 'html' as
	 * its media type, it is assumed to mainly work just within html rendering environments. If an
	 * extension has a null media type, it should work for any visual/text rendering environment
	 */
	public enum MEDIA_TYPE {
		html
	}
	
	/**
	 * default constructor
	 */
	public Extension() {
	}
	
	/**
	 * Called before being displayed each time
	 *
	 * @param parameterMap
	 */
	public void initialize(Map<String, String> parameterMap) {
		log.debug("Initializing extension for point: " + pointId);
		this.setPointId(pointId);
		this.setParameterMap(parameterMap);
	}
	
	/**
	 * Get the point id
	 *
	 * @return the <code>String</code> Point Id
	 */
	public String getPointId() {
		return pointId;
	}
	
	/**
	 * Set the point id
	 *
	 * @param pointId
	 */
	public void setPointId(String pointId) {
		this.pointId = pointId;
	}
	
	/**
	 * Get all of the parameters given to this extension point
	 *
	 * @return key-value parameter map
	 */
	public Map<String, String> getParameterMap() {
		return parameterMap;
	}
	
	/**
	 * Parameters given at the extension point This method is usually called only during extension
	 * initialization
	 *
	 * @param parameterMap key-value parameter map
	 */
	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}
	
	/**
	 * Sets the content type of this extension. If null is returned this extension should work
	 * across all medium types
	 *
	 * @return type of the medium that this extension works for
	 */
	public abstract Extension.MEDIA_TYPE getMediaType();
	
	/**
	 * Get the extension point id
	 *
	 * @return the <code>String</code> Extension Id
	 */
	public String getExtensionId() {
		return toExtensionId(getPointId(), getMediaType());
	}
	
	/**
	 * If this method returns a non-null value then the return value will be used as the default
	 * content for this extension at this extension point
	 *
	 * @return override content
	 */
	public String getOverrideContent(String bodyContent) {
		return null;
	}
	
	/**
	 * Get this extension's module id
	 *
	 * @return the <code>String</code> Module Id
	 */
	public final String getModuleId() {
		return moduleId;
	}
	
	/**
	 * Set the module id of this extension
	 *
	 * @param moduleId
	 */
	public final void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	/**
	 * If multiple extensions are added to the same extension point, set the order
	 * of those extensions by overriding this property.  Lower order numbers will generally 
	 * appear first within the extension point.
	 *
	 * @return 0
	 */
	public int getOrder() {
		return 0;
	}
	
	/**
	 * Get the string representation of this extension
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return "Extension: " + this.getExtensionId();
	}
	
	/**
	 * Convert the given pointId and mediaType to an extensionId. The extension id is usually
	 * pointid|mediaType if mediatype is null, extension id is just point id
	 *
	 * @param pointId
	 * @param mediaType
	 * @return string extension id
	 */
	public static final String toExtensionId(String pointId, MEDIA_TYPE mediaType) {
		if (mediaType != null) {
			return pointId + Extension.EXTENSION_ID_SEPARATOR + mediaType;
		} else {
			return pointId;
		}
	}
}
