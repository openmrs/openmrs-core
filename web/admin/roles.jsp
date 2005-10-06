<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.Role" %>
<%@ page import="org.openmrs.Privilege" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.openmrs.api.APIException" %>

<openmrs:require privilege="Manage Roles" otherwise="/openmrs/login.jsp" />

<%
	Context context = (Context)session.getAttribute("__openmrs_context");
	AdministrationService adminService = context.getAdministrationService();
	pageContext.setAttribute("userService", context.getUserService());

	//deleting roles
	String[] roles = request.getParameterValues("roleToDelete");
	if (roles != null) {
		for(int x = 0; x < roles.length; x++) {
				Role tmpRole = context.getUserService().getRole(roles[x]);
				try {
					adminService.deleteRole(tmpRole);
				}
				catch (APIException e)
				{
					session.setAttribute("openmrs_error", "Role cannot be deleted");
				}
		}
		if (roles.length == 1)
			session.setAttribute("openmrs_msg", "Role '" + roles[0] + "' deleted");
		else
			session.setAttribute("openmrs_msg", roles.length + " roles deleted");
	}
	
	//adding roles
	String roleString = request.getParameter("role");
	if (roleString != "" && roleString != null) {
		Role role = new Role(roleString);
		role.setDescription(request.getParameter("description"));
		String[] privs = request.getParameterValues("privileges");
		Set privObjs = new HashSet();
		if (privs != null) {
			for(int x = 0; x < privs.length; x++) {
				privObjs.add(new Privilege(privs[x]));
			}
		}
		role.setPrivileges(privObjs);
		try {
			adminService.createRole(role);
			session.setAttribute("openmrs_msg", "Role added");
		}
		catch (APIException e) {
			session.setAttribute("openmrs_error", "Unable to add role " + e.getMessage());
		}
	}
%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br>
<h2>Role Management</h2>	
<br>

<form method="post">
	<table>
		<tr>
			<td>Role</td>
			<td><input type="text" name="role"></td>
		</tr>
		<tr>
			<td>Description</td>
			<td><input type="text" name="description"></td>
		</tr>
		<tr>
			<td valign="top">Privileges</td>
			<td>
				<select name="privileges" multiple size="5">
					<c:forEach var="privilege" items="${userService.privileges}">
						<jsp:useBean id="privilege" type="org.openmrs.Privilege" scope="page"/>
						<option value="<c:out value="${privilege.privilege}"/>">
							<c:out value="${privilege}"/>
						</option>
					</c:forEach>
				</select>
			</td>
		</tr>
	</table>
	<input type="submit" value="Add Role">
<form>

<br><br>

<form method="post">
	<table>
		<tr>
			<th> </th>
			<th> Role </th>
			<th> Description </th>
			<th> Privileges </th>
		</tr>
	<c:forEach var="role" items="${userService.roles}">
		<jsp:useBean id="role" type="org.openmrs.Role" scope="page"/>
		<tr>
			<td><input type="checkbox" name="roleToDelete" value="<c:out value="${role.role}"/>"></td>
			<td>
				<a href="editRole.jsp?role=<c:out value="${role.role}"/>">
					<c:out value="${role.role}"/>
				</a>
			</td>
			<td><c:out value="${role.description}"/></td>
			<td><c:out value="${role.privileges}"/></td>
		</tr>
	</c:forEach>
	</table>
	<input type="submit" value="Delete Selected Roles">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>