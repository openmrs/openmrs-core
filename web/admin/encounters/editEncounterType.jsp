<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.api.EncounterService" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.EncounterType" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Encounters" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	EncounterService encounterService = context.getEncounterService();
	EncounterType encounterType = encounterService.getEncounterType(Integer.valueOf(request.getParameter("encounterTypeId")));

	if (request.getParameter("name") != null) {
		encounterType.setName(request.getParameter("name"));
		encounterType.setDescription(request.getParameter("description"));
		try {
			adminService.updateEncounterType(encounterType);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Encounter Encounter Type updated");
			response.sendRedirect("encounterTypes.jsp");
			return;
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to update encounter type " + e.getMessage());
		}
		
	}
	pageContext.setAttribute("encounterType", encounterType);
%>	

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Editing Encounter Encounter Type</h2>

<form method="post">
<table>
	<tr>
		<td>Name</td>
		<td><input type="text" name="name" value="${encounterType.name}" size="35" /></td>
	</tr>
	<tr>
		<td valign="top">Description</td>
		<td><textarea name="description" rows="3" cols="40">${encounterType.description}</textarea></td>
	</tr>
	<tr>
		<td>Creator</td>
		<td>${encounterType.creator}</td>
	</tr>
	<tr>
		<td>Date Created</td>
		<td>${encounterType.dateCreated}</td>
	</tr>
</table>
<input type="hidden" name="encounterTypeId" value="<c:out value="${encounterType.encounterTypeId}"/>">
<br />
<input type="submit" value="Save Encounter Type">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>