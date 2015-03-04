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

import java.util.HashMap;
import java.util.Map;

/**
 * Constants used by the module system in openmrs
 */
public class ModuleConstants {
	
	/**
	 * A map from "moduleid" to "required version" for all core modules that are required by
	 * openmrs. A module with at least the given version is required to start before openmrs will
	 * fully start.
	 */
	public static final Map<String, String> CORE_MODULES = new HashMap<String, String>();
	
	/**
	 * Used to determine if the {@link #CORE_MODULES} list should be used or not. For unit testing
	 * this is set to true to allow for faster runs with less dependencies.
	 */
	public static final String IGNORE_CORE_MODULES_PROPERTY = "module.ignore_core_status";
	
	/**
	 * Name of the file in the module repository to ping to question for updates to a module
	 */
	public static final String UPDATE_FILE_NAME = "update.rdf";
	
	/**
	 * Name of the global property that will tell the system where to look for modules to load. Can
	 * be either relative or absolute
	 */
	public static final String REPOSITORY_FOLDER_PROPERTY = "module_repository_folder";
	
	/**
	 * Default name of the folder that holds the currently loaded modules for the system
	 * 
	 * @see #REPOSITORY_FOLDER_PROPERTY
	 */
	public static final String REPOSITORY_FOLDER_PROPERTY_DEFAULT = "modules";
	
	/**
	 * Setting either of these properties to false will block web administration of modules
	 * 
	 * @see #RUNTIMEPROPERTY_ALLOW_ADMIN
	 */
	public static final String RUNTIMEPROPERTY_ALLOW_UPLOAD = "module.allow_upload";
	
	/**
	 * Setting either of these properties to "false" will block web administration of modules
	 * 
	 * @see #RUNTIMEPROPERTY_ALLOW_UPLOAD
	 */
	public static final String RUNTIMEPROPERTY_ALLOW_ADMIN = "module.allow_web_admin";
	
	/**
	 * Intended to be used by the testing framework in order to set a list of specific files that
	 * need to be loaded by the Context. Should be space separated. Should be either absolute file
	 * paths or classpath loadable file paths.
	 */
	public static final String RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD = "module.list_to_load";
	
	/**
	 * Name of the runtime property that will tell the system where to look for modules to load. Can
	 * be either relative or absolute
	 */
	public static final String REPOSITORY_FOLDER_RUNTIME_PROPERTY = "module.repository_folder";
	
	/**
	 * A module message.properties file containing this key mapped to "true" will be allowed to define messages outside of the module's namespace.
	 *
	 * @since 1.9.3
	 */
	public static final String MESSAGE_PROPERTY_ALLOW_KEYS_OUTSIDE_OF_MODULE_NAMESPACE = "openmrs.property.MESSAGE_PROPERTY_ALLOW_KEYS_OUTSIDE_OF_MODULE_NAMESPACE";
	
}
