package org.openmrs.module;

/**
 * Utility methods for working and manipulating modules
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class ModuleConstants {
	//private static Log log = LogFactory.getLog(ModuleConstants.class);
	
	public static final String UPDATE_FILE_NAME = "update.rdf";
	
	public static final String REPOSITORY_FOLDER_PROPERTY = "module_repository_folder";
	public static final String REPOSITORY_FOLDER_PROPERTY_DEFAULT = "modules";
	
	// either of these properties will block web administration of modules
	public static final String RUNTIMEPROPERTY_ALLOW_UPLOAD = "module.allow_upload";
	public static final String RUNTIMEPROPERTY_ALLOW_ADMIN = "module.allow_web_admin";
	
}
