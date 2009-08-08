/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module;

/**
 * Constants used by the module system in openmrs
 */
public class ModuleConstants {
	
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
	
}
