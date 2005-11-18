package org.openmrs.web;

import java.util.HashMap;
import java.util.Map;

public class Constants {

	// TODO put this in database ?
	private static HashMap<String, String> civilStatus = new HashMap<String, String>();
	
	static {
		civilStatus.put("1", "Single");
		civilStatus.put("2", "Married");
		civilStatus.put("3", "Divorced");
		civilStatus.put("4", "Widowed");
	}
	
	public static final String OPENMRS_CONTEXT_HTTPSESSION_ATTR = "__openmrs_context";
	public static final String OPENMRS_CLIENT_IP_HTTPSESSION_ATTR = "__openmrs_client_ip";
	public static final String OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR = "__openmrs_login_redirect"; // TODO change login.jsp to use this constant
	
	public static final String OPENMRS_MSG_ATTR = "openmrs_msg";
	public static final String OPENMRS_ERROR_ATTR = "openmrs_error";

	public static final Map<String, String> OPENMRS_CIVIL_STATUS = civilStatus;
}
