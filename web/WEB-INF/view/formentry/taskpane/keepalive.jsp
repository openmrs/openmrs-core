<%@ page import="java.util.Date" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="org.openmrs.api.context.Context" %>
<%@ page import="org.openmrs.web.WebConstants" %>
<%
	String attName = WebConstants.OPENMRS_DYNAMIC_FORM_KEEPALIVE;
	Date firstVisit = (Date)session.getAttribute(attName);
	
	String keepaliveMinutes = Context.getAdministrationService().getGlobalProperty("formentry.infopath_taskpane_keepalive_min");
	int keepalive = 60;  //default
	
	try {
		keepalive = Integer.parseInt(keepaliveMinutes);
	} catch ( Exception e ) {
		keepalive = 60;
	}
	
	Calendar c = Calendar.getInstance();
	c.setTime(firstVisit);
	c.add(Calendar.MINUTE, keepalive);
	Date finalVisit = c.getTime();
	Date rightNow = new Date();

	if ( rightNow.before(finalVisit) ) {
%>
		<openmrs:globalProperty key="formentry.infopath_taskpane_refresh_sec" defaultValue="" var="refreshSec" />
		<c:if test="${refreshSec != ''}">
			<meta http-equiv="refresh" content="<openmrs:globalProperty key="formentry.infopath_taskpane_refresh_sec" />;">
		</c:if>
<%
	}
%>
