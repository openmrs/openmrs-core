<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.EncounterType" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Encounters" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	pageContext.setAttribute("encounterService", context.getEncounterService());

	//deleting encounter types
	String[] encounterTypes = request.getParameterValues("encounterTypeId");
	if (encounterTypes != null) {
		EncounterType tmpEncounterType = null;
		for(int x = 0; x < encounterTypes.length; x++) {
				tmpEncounterType = context.getEncounterService().getEncounterType(Integer.valueOf(encounterTypes[x]));
				try {
					adminService.deleteEncounterType(tmpEncounterType);
				}
				catch (APIException e)
				{
					session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Encounter type cannot be deleted - " + e.getMessage());
				}
		}
		if (encounterTypes.length == 1)
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "EncounterType '" + tmpEncounterType.getName() + "' deleted");
		else
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, encounterTypes.length + " encounter types deleted");
	}
	
	//adding an encounter type
	String encounterTypeName = request.getParameter("name");
	String description = request.getParameter("description");
	if (encounterTypeName != "" && encounterTypeName != null) {
		EncounterType t = new EncounterType();
		t.setName(encounterTypeName);
		t.setDescription(description);
		try {
			adminService.createEncounterType(t);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Encounter type added");
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to add encounter type " + e.getMessage());
		}
	}
%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Encounter Type Management</h2>	
<br />

<b class="boxHeader">Add a New Encounter Type</b>
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
	<input type="submit" value="Add Encounter Type">
</form>

<br />

<b class="boxHeader">
	Current Encounter Types
</b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> Name </th>
			<th> Description </th>
		</tr>
		<c:forEach var="encounterType" items="${encounterService.encounterTypes}">
			<jsp:useBean id="encounterType" type="org.openmrs.EncounterType" scope="page"/>
			<tr>
				<td valign="top"><input type="checkbox" name="encounterTypeId" value="${encounterType.encounterTypeId}"></td>
				<td valign="top"><a href="editEncounterType.jsp?encounterTypeId=${encounterType.encounterTypeId}">
					   ${encounterType.name}
					</a>
				</td>
				<td valign="top">${encounterType.description}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="Delete Selected Encounter Types" name="action">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>