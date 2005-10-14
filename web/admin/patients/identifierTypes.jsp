<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.PatientIdentifierType" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Patients" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	pageContext.setAttribute("patientService", context.getPatientService());

	//deleting identifierTypes
	String[] identifierTypes = request.getParameterValues("identifierTypeId");
	if (identifierTypes != null) {
		PatientIdentifierType tmpIdentifierType = null;
		for(int x = 0; x < identifierTypes.length; x++) {
				tmpIdentifierType = context.getPatientService().getPatientIdentifierType(Integer.valueOf(identifierTypes[x]));
				try {
					adminService.deletePatientIdentifierType(tmpIdentifierType);
				}
				catch (APIException e)
				{
					session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "IdentifierType cannot be deleted - " + e.getMessage());
				}
		}
		if (identifierTypes.length == 1)
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "IdentifierType '" + tmpIdentifierType.getName() + "' deleted");
		else
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, identifierTypes.length + " identifierTypes deleted");
	}
	
	//adding a identifierType
	String identifierTypeName = request.getParameter("name");
	String description = request.getParameter("description");
	if (identifierTypeName != "" && identifierTypeName != null) {
		PatientIdentifierType t = new PatientIdentifierType();
		t.setName(identifierTypeName);
		t.setDescription(description);
		try {
			adminService.createPatientIdentifierType(t);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Identifier type added");
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to add identifier type " + e.getMessage());
		}
	}
%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Patient Identifier Type Management</h2>	
<br />

<b class="boxHeader">Add a New IdentifierType</b>
<form method="post" class="box">
	<table>
		<tr>
			<td>Name</td>
			<td><input type="text" name="name"></td>
		</tr>
		<tr>
			<td>Description</td>
			<td><textarea name="description" rows="2" cols="50"></textarea></td>
			
		</tr>
	</table>
	<input type="submit" value="Add Identifier Type">
</form>

<br />

<b class="boxHeader">
	Current Identifier Types
</b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> Name </th>
			<th> Description </th>
		</tr>
		<c:forEach var="identifierType" items="${patientService.patientIdentifierTypes}">
			<jsp:useBean id="identifierType" type="org.openmrs.PatientIdentifierType" scope="page"/>
			<tr>
				<td valign="top"><input type="checkbox" name="identifierTypeId" value="${identifierType.patientIdentifierTypeId}"></td>
				<td valign="top"><a href="editIdentifierType.jsp?identifierTypeId=${identifierType.patientIdentifierTypeId}">
					   ${identifierType.name}
					</a>
				</td>
				<td valign="top">${identifierType.description}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="Delete Selected Identifier Types" name="action">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>