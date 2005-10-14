<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.api.PatientService" %>
<%@ page import="org.openmrs.Tribe" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Patients" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	PatientService patientService = context.getPatientService();
	Tribe tribe = patientService.getTribe(Integer.valueOf(request.getParameter("tribeId")));

	if (request.getParameter("name") != null) {
		tribe.setName(request.getParameter("name"));
		tribe.setRetired(Boolean.valueOf(request.getParameter("retired")));
		try {
			adminService.updateTribe(tribe);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Tribe updated");
			response.sendRedirect("tribes.jsp");
			return;
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to update tribe " + e.getMessage());
		}
		
	}
	pageContext.setAttribute("tribe", tribe);
%>	

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Editing Tribe</h2>

<form method="post">
<table>
	<tr>
		<td>Name</td>
		<td><input type="text" name="name" value="${tribe.name}" /></td>
	</tr>
	<tr>
		<td>Retired</td>
		<td><input type="checkbox" name="retired" value="true" <%= tribe.isRetired().booleanValue() ? "checked" : "" %> /></td>
	</tr>
</table>
<input type="hidden" name="tribeId" value="<c:out value="${tribe.tribeId}"/>">
<br />
<input type="submit" value="Save Tribe">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>