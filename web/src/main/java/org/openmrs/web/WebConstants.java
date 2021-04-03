/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

public class WebConstants {
	
	/**
	 * Private constructor to prevent accidental instantiation of this utility class
	 */
	private WebConstants() {
	}
	
	public static final String INIT_REQ_UNIQUE_ID = "__INIT_REQ_UNIQUE_ID__";
	
	public static final String OPENMRS_CONTEXT_HTTPSESSION_ATTR = "__openmrs_context";
	
	public static final String OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR = "__openmrs_user_context";
	
	public static final String OPENMRS_CLIENT_IP_HTTPSESSION_ATTR = "__openmrs_client_ip";
	
	public static final String OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR = "__openmrs_login_redirect";
	
	public static final String OPENMRS_MSG_ATTR = "openmrs_msg";
	
	public static final String OPENMRS_MSG_ARGS = "openmrs_msg_arguments";
	
	public static final String OPENMRS_ERROR_ATTR = "openmrs_error";
	
	public static final String OPENMRS_ERROR_ARGS = "openmrs_error_arguments";
	
	public static final String OPENMRS_ADDR_TMPL = "openmrs_address_template";
	
	public static final String OPENMRS_LANGUAGE_COOKIE_NAME = "__openmrs_language";
	
	public static final String OPENMRS_USER_OVERRIDE_PARAM = "__openmrs_user_over_id";
	
	public static final String OPENMRS_ANALYSIS_IN_PROGRESS_ATTR = "__openmrs_analysis_in_progress";
	
	public static final String OPENMRS_DYNAMIC_FORM_IN_PROGRESS_ATTR = "__openmrs_dynamic_form_in_progress";
	
	public static final String OPENMRS_PATIENT_SET_ATTR = "__openmrs_patient_set";
	
	public static final Integer OPENMRS_PATIENTSET_PAGE_SIZE = 25;
	
	public static final String OPENMRS_DYNAMIC_FORM_KEEPALIVE = "__openmrs_dynamic_form_keepalive";
	
	public static final String OPENMRS_HEADER_USE_MINIMAL = "__openmrs_use_minimal_header";
	
	public static final String OPENMRS_PORTLET_MODEL_NAME = "model";
	
	public static final String OPENMRS_PORTLET_LAST_REQ_ID = "__openmrs_portlet_last_req_id";
	
	public static final String OPENMRS_PORTLET_CACHED_MODEL = "__openmrs_portlet_cached_model";
	
	// these vars filled in by org.openmrs.web.Listener at webapp start time
	public static String BUILD_TIMESTAMP = "";
	
	public static String WEBAPP_NAME = "openmrs";
	
	/**
	 * Page in the webapp used for initial setup of the database connection if no valid one exists
	 */
	public static final String SETUP_PAGE_URL = "initialsetup";
	
	/**
	 * The url of the module repository. This is filled in at startup by the value in web.xml
	 */
	public static String MODULE_REPOSITORY_URL = "";
	
	/**
	 * Global property name for the number of times one IP can fail at logging in before being
	 * locked out. A value of 0 for this property means no IP lockout checks.
	 * 
	 * @see org.openmrs.web.servlet.LoginServlet
	 */
	public static final String GP_ALLOWED_LOGIN_ATTEMPTS_PER_IP = "security.loginAttemptsAllowedPerIP";
	
	/**
	 * User names of the logged-in users are stored in this map (session id -&gt; user name) in the
	 * ServletContext under this key
	 */
	public static final String CURRENT_USERS = "CURRENT_USERS";
	
	/**
	 * Session attribute name that specifies if there are any privilege checks the currently
	 * authenticated user failed
	 */
	public static final String INSUFFICIENT_PRIVILEGES = "insufficient_privileges";
	
	/**
	 * Session attribute name for the url of the page the user was trying to access when they failed
	 * a privilege check
	 */
	public static final String DENIED_PAGE = "denied_page";
	
	/**
	 * Session attribute name for the privileges the user didn't have
	 */
	public static final String REQUIRED_PRIVILEGES = "required_privileges";
	
	/**
	 * Session attribute name for the uncaught exception message
	 */
	public static final String UNCAUGHT_EXCEPTION_MESSAGE = "uncaught_exception_message";
	
	/**
	 * Session attribute name for the referer url
	 */
	public static final String REFERER_URL = "referer_url";
}
