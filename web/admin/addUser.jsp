<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.UserService" %>
<%@ page import="org.openmrs.User" %>
<%@ page import="org.openmrs.Role" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.openmrs.api.APIException" %>

<openmrs:require privilege="Manage Users" otherwise="/openmrs/login.jsp" />

<%
	Context context = (Context)session.getAttribute("__openmrs_context");
	UserService userService = context.getUserService();
	pageContext.setAttribute("userService", userService);


	//adding roles 

	String username = request.getParameter("uname");
	String password = request.getParameter("pword");
	String confirm  = request.getParameter("confirm");
	if (username != null && !password.equals(confirm)) {
		session.setAttribute("openmrs_error", "Passwords do not match");
	}
	else if (username != "" && username != null) {
		User user = new User();
		user.setUsername(username);
		user.setFirstName(request.getParameter("firstName"));
		user.setMiddleName(request.getParameter("middleName"));
		user.setLastName(request.getParameter("lastName"));

		//make the role set to add to the user
		String[] roles = request.getParameterValues("roles");
		Set roleObjs = new HashSet();
		if (roles != null) {
			for(int x = 0; x < roles.length; x++) {
				roleObjs.add(new Role(roles[x]));
			}
		}
		user.setRoles(roleObjs);
		
		try {
			userService.createUser(user, password);
			session.setAttribute("openmrs_msg", "User created");
		}
		catch (APIException e) {
			session.setAttribute("openmrs_error", "Unable to add user : " + e.getMessage());
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
			<td><input type="text" name="uname"></td>
		</tr>
		<tr>
			<td>Password</td>
			<td><input type="password" name="pword"></td>
		</tr>
		<tr>
			<td>Password (again)</td>
			<td><input type="password" name="confirm"></td>
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