<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.Privilege" %>
<%@ page import="org.openmrs.api.APIException" %>

<openmrs:require privilege="Manage Users" otherwise="/openmrs/login.jsp" />

<%
	Context context = (Context)session.getAttribute("__openmrs_context");
	AdministrationService adminService = context.getAdministrationService();
	pageContext.setAttribute("userService", context.getUserService());

	//deleting privileges
	String[] privileges = request.getParameterValues("privilegeToDelete");
	if (privileges != null) {
		for(int x = 0; x < privileges.length; x++) {
				Privilege tmpPrivilege = context.getUserService().getPrivilege(privileges[x]);
				try {
					adminService.deletePrivilege(tmpPrivilege);
				}
				catch (APIException e)
				{
					session.setAttribute("openmrs_error", "Privilege cannot be deleted");
				}
		}
		if (privileges.length == 1)
			session.setAttribute("openmrs_msg", "Privilege '" + privileges[0] + "' deleted");
		else
			session.setAttribute("openmrs_msg", privileges.length + " privileges deleted");
	}
	
	//adding privileges
	String privilegeString = request.getParameter("privilege");
	if (privilegeString != "" && privilegeString != null) {
		Privilege privilege = new Privilege(privilegeString);
		privilege.setDescription(request.getParameter("description"));
		try {
			adminService.createPrivilege(privilege);
			session.setAttribute("openmrs_msg", "Privilege added");
		}
		catch (APIException e) {
			session.setAttribute("openmrs_error", "Unable to add privilege " + e.getMessage());
		}
	}
%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br>
<h2>Privilege Management</h2>	
<br>

<h3>Add a New Privilege</h3>
<form method="post" class="box">
	<table>
		<tr>
			<td>Privilege</td>
			<td><input type="text" name="privilege"></td>
		</tr>
		<tr>
			<td>Description</td>
			<td><input type="text" name="description"></td>
		</tr>
	</table>
	<input type="submit" value="Add Privilege">
<form>

<br><br>

<h3>Update a privilege</h3>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> Privilege </th>
			<th> Description </th>
		</tr>
	<c:forEach var="privilege" items="${userService.privileges}">
		<jsp:useBean id="privilege" type="org.openmrs.Privilege" scope="page"/>
		<tr>
			<td><input type="checkbox" name="privilegeToDelete" value="<c:out value="${privilege.privilege}"/>"></td>
			<td>
				<a href="editPrivilege.jsp?privilege=<c:out value="${privilege.privilege}"/>">
					<c:out value="${privilege.privilege}"/>
				</a>
			</td>
			<td><c:out value="${privilege.description}"/></td>
		</tr>
	</c:forEach>
	</table>
	<input type="submit" value="Delete Selected Privileges">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>