<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.api.UserService" %>
<%@ page import="org.openmrs.Privilege" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Users" otherwise="/openmrs/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	Privilege privilege = context.getUserService().getPrivilege(request.getParameter("privilege"));

	if (request.getParameter("description") != null) {
		privilege.setDescription(request.getParameter("description"));
		context.getAdministrationService().updatePrivilege(privilege);
		session.setAttribute("openmrs_msg", "Privilege updated");
	}
	pageContext.setAttribute("privilege", privilege);
%>	

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br>
<h2>Privilege Management</h2>	
<br>

<form method="post">
<table>
	<tr>
		<td>Privilege</td>
		<td><c:out value="${privilege.privilege}"/></td>
	</tr>
	<tr>
		<td>Description</td>
		<td><input type="text" name="description" value="<c:out value="${privilege.description}"/>"></td>
	</tr>
</table>
<input type="hidden" name="privilege" value="<c:out value="${privilege.privilege}"/>">
<input type="submit" value="Save Privilege">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>