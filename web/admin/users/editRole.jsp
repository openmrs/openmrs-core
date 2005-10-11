<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.api.UserService" %>
<%@ page import="org.openmrs.Role" %>
<%@ page import="org.openmrs.Privilege" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Users" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	UserService userService = context.getUserService();
	Role role = userService.getRole(request.getParameter("role"));

	if (request.getParameter("description") != null) {
		role.setDescription(request.getParameter("description"));
		String[] privs = request.getParameterValues("privileges");
		Set privObjs = new HashSet();
		if (privs != null) {
			for(int x = 0; x < privs.length; x++) {
				privObjs.add(new Privilege(privs[x]));
			}
		}
		role.setPrivileges(privObjs);
		adminService.updateRole(role);
		session.setAttribute("openmrs_msg", "Role updated");
	}
	pageContext.setAttribute("role", role);
	pageContext.setAttribute("userService", userService);
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
		<td><c:out value="${role.role}"/></td>
	</tr>
	<tr>
		<td>Description</td>
		<td><input type="text" name="description" value="<c:out value="${role.description}"/>"></td>
	</tr>
	<tr>
		<td valign="top">Privileges</td>
		<td>
			<select name="privileges" multiple size="5">
				<c:forEach var="privilege" items="${userService.privileges}">
					<jsp:useBean id="privilege" type="org.openmrs.Privilege" scope="page"/>
					<option value="<c:out value="${privilege.privilege}"/>"
							<% if (role.getPrivileges().contains(privilege)) {%>selected <%}%>>
						<c:out value="${privilege}"/>
					</option>
				</c:forEach>
			</select>
		</td>
	</tr>
</table>
<input type="hidden" name="role" value="<c:out value="${role.role}"/>">
<input type="submit" value="Save Role">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>