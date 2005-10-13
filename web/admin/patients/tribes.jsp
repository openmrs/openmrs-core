<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.Tribe" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Tribes" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	pageContext.setAttribute("patientService", context.getPatientService());

	//retiring tribes
	String[] tribes = request.getParameterValues("tribeId");
	String action  = request.getParameter("action");
	if (tribes != null) {
		for(int x = 0; x < tribes.length; x++) {
				Tribe tmpTribe = context.getPatientService().getTribe(Integer.valueOf(tribes[x]));
				try {
					if (action.startsWith("Retire"))
						adminService.retireTribe(tmpTribe);
					else
						adminService.unretireTribe(tmpTribe);
				}
				catch (APIException e)
				{
					session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Tribe cannot be retired - " + e.getMessage());
				}
		}
		if (tribes.length == 1)
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Tribe '" + tribes[0] + "' retired");
		else
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, tribes.length + " tribes retired");
	}
	
	//adding a tribe
	String tribeName = request.getParameter("tribe");
	if (tribeName != "" && tribeName != null) {
		Tribe t = new Tribe();
		t.setName(tribeName);
		try {
			adminService.createTribe(t);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Tribe added");
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to add tribe " + e.getMessage());
		}
	}
%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Tribe Management</h2>	
<br />

<b class="boxHeader">Add a New Tribe</b>
<form method="post" class="box">
	<table>
		<tr>
			<td>Name</td>
			<td><input type="text" name="tribe"></td>
		</tr>
	</table>
	<input type="submit" value="Add Tribe">
</form>

<br /><br />

<b class="boxHeader">Current Tribes</b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> Tribe </th>
			<th> Retired </th>
		</tr>
		<c:forEach var="tribe" items="${patientService.tribes}">
			<jsp:useBean id="tribe" type="org.openmrs.Tribe" scope="page"/>
			<tr>
				<td><input type="checkbox" name="tribeId" value="${tribe.tribeId}"></td>
				<td><a href="editTribe.jsp?id=${tribe.tribeId}">${tribe.name}</a></td>
				<td><%= tribe.isRetired() %></td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="Retire Selected Tribes" name="action">
	<input type="submit" value="Unretire Selected Tribes" name="action">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>