<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.api.FormService" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.FieldType" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Forms" otherwise="/login.htm" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	FormService fieldService = context.getFormService();
	FieldType fieldType = fieldService.getFieldType(Integer.valueOf(request.getParameter("fieldTypeId")));

	if (request.getParameter("name") != null) {
		fieldType.setName(request.getParameter("name"));
		fieldType.setDescription(request.getParameter("description"));
		fieldType.setIsSet(Boolean.valueOf(request.getParameter("isSet")));
		try {
			adminService.updateFieldType(fieldType);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Field Type updated");
			response.sendRedirect("fieldTypes.jsp");
			return;
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to update field type " + e.getMessage());
		}
		
	}
	pageContext.setAttribute("fieldType", fieldType);
%>	

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Editing Field Type</h2>

<form method="post">
<table>
	<tr>
		<td>Name</td>
		<td><input type="text" name="name" value="${fieldType.name}" size="35" /></td>
	</tr>
	<tr>
		<td>Is a set</td>
		<td><input type="checkbox" name="isSet" value="true" <%= fieldType.getIsSet().booleanValue() ? "checked" : "" %> /></td>
	</tr>
	<tr>
		<td valign="top">Description</td>
		<td><textarea name="description" rows="3" cols="40">${fieldType.description}</textarea></td>
	</tr>
	<tr>
		<td>Creator</td>
		<td>${fieldType.creator}</td>
	</tr>
	<tr>
		<td>Date Created</td>
		<td>${fieldType.dateCreated}</td>
	</tr>
</table>
<input type="hidden" name="fieldTypeId" value="<c:out value="${fieldType.fieldTypeId}"/>">
<br />
<input type="submit" value="Save Field Type">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>