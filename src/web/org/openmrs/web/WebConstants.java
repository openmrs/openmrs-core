package org.openmrs.web;

public class WebConstants {
	
	public static final String INIT_REQ_UNIQUE_ID = "__INIT_REQ_UNIQUE_ID__";

	public static final String OPENMRS_CONTEXT_HTTPSESSION_ATTR = "__openmrs_context";
	
	public static final String OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR = "__openmrs_user_context";

	public static final String OPENMRS_CLIENT_IP_HTTPSESSION_ATTR = "__openmrs_client_ip";

	public static final String OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR = "__openmrs_login_redirect";

	public static final String OPENMRS_MSG_ATTR = "openmrs_msg";
	public static final String OPENMRS_MSG_ARGS = "openmrs_msg_arguments";

	public static final String OPENMRS_ERROR_ATTR = "openmrs_error";
	public static final String OPENMRS_ERROR_ARGS = "openmrs_error_arguments";

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
}