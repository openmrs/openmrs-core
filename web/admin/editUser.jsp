<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.UserService" %>
<%@ page import="org.openmrs.User" %>

<openmrs:require privilege="Manage Users" otherwise="/openmrs/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<br>

<h2>User Management</h2>	
<br>

<%
	Context context = (Context)session.getAttribute("__openmrs_context");
	UserService userService = context.getUserService();
	String id = request.getParameter("id");
	Integer i = Integer.valueOf(id);
	User u = userService.getUser(i);
	pageContext.setAttribute("user", u);
%>

<form method="post" action="editUserServlet">
<table>
	<tr>
		<td>Username</td>
		<td><input type="text" name="username" value="<c:out value="${user.username}"/>"></td>
	</tr>
	<tr>
		<td>First Name</td>
		<td><input type="text" name="username" value="<c:out value="${user.username}"/>"></td>
	</tr>
	<tr>
		<td>Middle Name</td>
		<td><input type="text" name="username" value="<c:out value="${user.username}"/>"></td>
	</tr>
	<tr>
		<td>Last Name</td>
		<td><input type="text" name="username" value="<c:out value="${user.username}"/>"></td>
	</tr>
	<tr>
		<td>Roles</td>
		<td>
			<select name="roles" multiple size="<c:out value="${user.roles}"/>">
				<c:forEach var="role" items="${user.roles}">
					<jsp:useBean id="role" type="org.openmrs.Role" scope="page"/>
					<option value="<c:out value="${role.role}"/>">
						<c:out value="${role}"/>
					</option>
				</c:forEach>
			</select>
		</td>
	</tr>
</table>
<input type="submit" value="Save User">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>