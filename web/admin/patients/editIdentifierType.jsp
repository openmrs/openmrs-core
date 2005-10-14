<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.api.PatientService" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.PatientIdentifierType" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Patients" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	PatientService patientService = context.getPatientService();
	PatientIdentifierType identifierType = patientService.getPatientIdentifierType(Integer.valueOf(request.getParameter("identifierTypeId")));

	if (request.getParameter("name") != null) {
		identifierType.setName(request.getParameter("name"));
		identifierType.setDescription(request.getParameter("description"));
		try {
			adminService.updatePatientIdentifierType(identifierType);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Patient Identifier Type updated");
			response.sendRedirect("identifierTypes.jsp");
			return;
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to update identifier type " + e.getMessage());
		}
		
	}
	pageContext.setAttribute("identifierType", identifierType);
%>	

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Editing Patient Identifier Type</h2>

<form method="post">
<table>
	<tr>
		<td>Name</td>
		<td><input type="text" name="name" value="${identifierType.name}" size="35" /></td>
	</tr>
	<tr>
		<td valign="top">Description</td>
		<td><textarea name="description" rows="3" cols="40">${identifierType.description}</textarea></td>
	</tr>
	<tr>
		<td>Creator</td>
		<td>${identifierType.creator}</td>
	</tr>
	<tr>
		<td>Date Created</td>
		<td>${identifierType.dateCreated}</td>
	</tr>
</table>
<input type="hidden" name="identifierTypeId" value="<c:out value="${identifierType.patientIdentifierTypeId}"/>">
<br />
<input type="submit" value="Save Identifier Type">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>