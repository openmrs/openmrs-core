<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.UserService" %>
<%@ page import="org.openmrs.web.Constants" %>
<%@ page import="org.openmrs.User" %>

<openmrs:require privilege="Manage Users" otherwise="/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br>
<h2>User Management</h2>	
<br>

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	UserService userService = context.getUserService();
	String id = request.getParameter("id");
	Integer i = Integer.valueOf(id);
	User user = userService.getUser(i);
	pageContext.setAttribute("user", user);
	pageContext.setAttribute("userService", userService);
%>

<form method="post" action="editUserServlet">
<table>
	<tr>
		<td>Username</td>
		<td><input type="text" name="username" value="<c:out value="${user.username}"/>"></td>
	</tr>
	<tr>
		<td>First Name</td>
		<td><input type="text" name="firstName" value="<c:out value="${user.firstName}"/>"></td>
	</tr>
	<tr>
		<td>Middle Name</td>
		<td><input type="text" name="middleName" value="<c:out value="${user.middleName}"/>"></td>
	</tr>
	<tr>
		<td>Last Name</td>
		<td><input type="text" name="lastName" value="<c:out value="${user.lastName}"/>"></td>
	</tr>
	<tr>
		<td valign="top">Roles</td>
		<td>
			<select name="roles" multiple size="5">
				<c:forEach var="role" items="${userService.roles}">
					<jsp:useBean id="role" type="org.openmrs.Role" scope="page"/>
					<option value="<c:out value="${role.role}"/>"
							<% if (user.getRoles().contains(role)) {%>selected <%}%>>
						<c:out value="${role}"/>
					</option>
				</c:forEach>
			</select>
		</td>
	</tr>
</table>
<input type="hidden" name="userId" value="<c:out value="${user.userId}"/>">
<input type="submit" value="Save User">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>