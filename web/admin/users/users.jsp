<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.UserService" %>
<%@ page import="org.openmrs.User" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Users" otherwise="/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br>
<h2>User Management</h2>	
<br>

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	pageContext.setAttribute("userService", context.getUserService());
%>

<table>
	<tr>
		<th> Username </th>
		<th> First    </th>
		<th> Last     </th>
		<th> Roles    </th>
	</tr>
<c:forEach var="user" items="${userService.users}">
	<jsp:useBean id="user" type="org.openmrs.User" scope="page"/>
	<tr>
		<td>
			<a href="editUser.jsp?id=<c:out value="${user.userId}"/>">
				<c:out value="${user.username}"/>
			</a>
		</td>
		<td><c:out value="${user.firstName}"/></td>
		<td><c:out value="${user.lastName}"/></td>
		<td><c:out value="${user.roles}"/></td>
	</tr>
</c:forEach>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>