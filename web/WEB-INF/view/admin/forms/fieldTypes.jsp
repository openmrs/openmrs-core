<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.api.context.Context" %>
<%@ page import="org.openmrs.api.db.AdministrationService" %>
<%@ page import="org.openmrs.FieldType" %>
<%@ page import="org.openmrs.api.db.APIException" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Forms" otherwise="/login.htm" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	pageContext.setAttribute("formService", context.getFormService());

	//deleting field types
	String[] fieldTypes = request.getParameterValues("fieldTypeId");
	if (fieldTypes != null) {
		FieldType tmpFieldType = null;
		for(int x = 0; x < fieldTypes.length; x++) {
				tmpFieldType = context.getFormService().getFieldType(Integer.valueOf(fieldTypes[x]));
				try {
					adminService.deleteFieldType(tmpFieldType);
				}
				catch (APIException e)
				{
					session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Field type cannot be deleted - " + e.getMessage());
				}
		}
		if (fieldTypes.length == 1)
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Field type '" + tmpFieldType.getName() + "' deleted");
		else
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, fieldTypes.length + " field types deleted");
	}
	
	//adding an field type
	String fieldTypeStr = request.getParameter("name");
	String description = request.getParameter("description");
	String isSet       = request.getParameter("isSet");
	if (fieldTypeStr != "" && fieldTypeStr != null) {
		FieldType t = new FieldType();
		t.setName(fieldTypeStr);
		t.setDescription(description);
		t.setIsSet(Boolean.valueOf(isSet));
		try {
			adminService.createFieldType(t);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Field type added");
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to add field type " + e.getMessage());
		}
	}
%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Field Type Management</h2>	
<br />

<b class="boxHeader">Add a New Field Type</b>
<form method="post" class="box">
	<table>
		<tr>
			<td>Name</td>
			<td><input type="text" name="name"></td>
		</tr>
		<tr>
			<td>Is a set</td>
			<td><input type="checkbox" name="isSet" value="true"></td>
		</tr>
		<tr>
			<td>Description</td>
			<td><textarea name="description" rows="2" cols="50"></textarea></td>
		</tr>
	</table>
	<input type="submit" value="Add Field Type">
</form>

<br />

<b class="boxHeader">
	Current Field Types
</b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> Name </th>
			<th> Is a Set   </th>
			<th> Description</th>
		</tr>
		<c:forEach var="fieldType" items="${formService.fieldTypes}">
			<jsp:useBean id="fieldType" type="org.openmrs.FieldType" scope="page"/>
			<tr>
				<td valign="top"><input type="checkbox" name="fieldTypeId" value="${fieldType.fieldTypeId}"></td>
				<td valign="top"><a href="editFieldType.jsp?fieldTypeId=${fieldType.fieldTypeId}">
					   ${fieldType.name}
					</a>
				</td>
				<td valign="top" align="center"><%= fieldType.getIsSet().booleanValue() ? "yes" : "" %></td>
				<td valign="top">${fieldType.description}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="Delete Selected Field Types" fieldType="action">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>