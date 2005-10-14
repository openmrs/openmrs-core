<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.UserService" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.User" %>
<%@ page import="org.openmrs.Role" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Users" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	UserService userService = context.getUserService();
	pageContext.setAttribute("userService", userService);

	//adding user
	String action = request.getParameter("action");
	if (action != null && action.equals("Add User")) {
		String username = request.getParameter("uname");
		if (username != null && !username.equals("")) {
			String password = request.getParameter("pword");
			String confirm  = request.getParameter("confirm");
			if (username != null && !password.equals(confirm)) {
				session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Passwords do not match");
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
					session.setAttribute(Constants.OPENMRS_MSG_ATTR, "User created");
					response.sendRedirect("users.jsp");
					return;
				}
				catch (APIException e) {
					session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to add user : " + e.getMessage());
				}
			}
		}
	}
	else {
		//retiring users
		String[] users = request.getParameterValues("userId");
		if (users != null && users.length > 0) {
			action = action.startsWith("Void") ? "voided" : "unvoided";
			User tmpUser = null;
			for(int x = 0; x < users.length; x++) {
					tmpUser = context.getUserService().getUser(Integer.valueOf(users[x]));
					try {
						if (action.equals("voided"))
							userService.voidUser(tmpUser, "TO IMPLEMENT"); //TODO void reason
						else
							userService.unvoidUser(tmpUser);
					}
					catch (APIException e)
					{
						session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "User cannot be " + action + " - " + e.getMessage());
					}
			}
			if (users.length == 1)
				session.setAttribute(Constants.OPENMRS_MSG_ATTR, "User '" + tmpUser + "' " + action);
			else
				session.setAttribute(Constants.OPENMRS_MSG_ATTR, users.length + " users " + action);
		}
	}
	
%>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br>
<h2>User Management</h2>

<b class="boxHeader">Add a New User</b>
<form method="post" class="box">
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
	<input type="hidden" name="action" value="Add User" />
	<input type="submit" value="Add User" />
</form>

<b class="boxHeader">Current Users</b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> Username </th>
			<th> First    </th>
			<th> Last     </th>
			<th> Roles    </th>
		</tr>
		<c:forEach var="user" items="${userService.users}">
			<jsp:useBean id="user" type="org.openmrs.User" scope="page"/>
			<tr>
				<td><input type="checkbox" name="userId" value="${user.userId}" /></td>
				<td>
					<a href="editUser.jsp?id=<c:out value="${user.userId}"/>"
					   class='<%= (user.isVoided() != null && user.isVoided().booleanValue()) ? "retired" : "" %>'>
						<c:out value="${user.username}"/>
					</a>
				</td>
				<td><c:out value="${user.firstName}"/></td>
				<td><c:out value="${user.lastName}"/></td>
				<td><c:out value="${user.roles}"/></td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" name="action" value="Void Selected Users">
	<input type="submit" name="action" value="Unvoid Selected Users">
</form>
<%@ include file="/WEB-INF/template/footer.jsp" %>