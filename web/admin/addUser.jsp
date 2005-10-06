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
	UserService userService = context.getUserService();
	pageContext.setAttribute("userService", userService);



	//adding roles
	String roleString = request.getParameter("username");
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
<h2>Add a New User</h2>	
<br>

<form method="post">
	<table>
		<tr>
			<td>Username</td>
			<td><input type="text" name="username"></td>
		</tr>
		<tr>
			<td>Password</td>
			<td><input type="password" name="password"></td>
		</tr>
		<tr>
			<td>First Name</td>
			<td><input type="text" name="firstName"></td>
		</tr>
		<tr>
			<td>Middle Name</td>
			<td><input type="text" name="middleName"></td>
		</tr>
		<tr>
			<td>Last Name</td>
			<td><input type="text" name="lastName"></td>
		</tr>
		<tr>
			<td valign="top">Roles</td>
			<td>
				<select name="roles" multiple size="5">
					<c:forEach var="role" items="${userService.roles}">
						<jsp:useBean id="role" type="org.openmrs.Role" scope="page"/>
						<option value="<c:out value="${role.role}"/>">
							<c:out value="${role}"/>
						</option>
					</c:forEach>
				</select>
			</td>
		</tr>
	</table>
	<input type="submit" value="Add User">
<form>
<%@ include file="/WEB-INF/template/footer.jsp" %>