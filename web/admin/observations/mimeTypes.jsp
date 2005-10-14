<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.MimeType" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Observations" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	pageContext.setAttribute("obsService", context.getObsService());

	//deleting mime types
	String[] mimeTypes = request.getParameterValues("mimeTypeId");
	if (mimeTypes != null) {
		MimeType tmpMimeType = null;
		for(int x = 0; x < mimeTypes.length; x++) {
				tmpMimeType = context.getObsService().getMimeType(Integer.valueOf(mimeTypes[x]));
				try {
					adminService.deleteMimeType(tmpMimeType);
				}
				catch (APIException e)
				{
					session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Mime type cannot be deleted - " + e.getMessage());
				}
		}
		if (mimeTypes.length == 1)
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "MimeType '" + tmpMimeType.getMimeType() + "' deleted");
		else
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, mimeTypes.length + " mime types deleted");
	}
	
	//adding an mime type
	String mimeTypeStr = request.getParameter("mimeType");
	String description = request.getParameter("description");
	if (mimeTypeStr != "" && mimeTypeStr != null) {
		MimeType t = new MimeType();
		t.setMimeType(mimeTypeStr);
		t.setDescription(description);
		try {
			adminService.createMimeType(t);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Mime type added");
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to add mime type " + e.getMessage());
		}
	}
%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Mime Type Management</h2>	
<br />

<b class="boxHeader">Add a New Mime Type</b>
<form method="post" class="box">
	<table>
		<tr>
			<td>Mime Type</td>
			<td><input type="text" name="mimeType"></td>
		</tr>
		<tr>
			<td>Description</td>
			<td><textarea name="description" rows="2" cols="50"></textarea></td>
			
		</tr>
	</table>
	<input type="submit" value="Add Mime Type">
</form>

<br />

<b class="boxHeader">
	Current Mime Types
</b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> Mime Type </th>
			<th> Description </th>
		</tr>
		<c:forEach var="mimeType" items="${obsService.mimeTypes}">
			<jsp:useBean id="mimeType" type="org.openmrs.MimeType" scope="page"/>
			<tr>
				<td valign="top"><input type="checkbox" name="mimeTypeId" value="${mimeType.mimeTypeId}"></td>
				<td valign="top"><a href="editMimeType.jsp?mimeTypeId=${mimeType.mimeTypeId}">
					   ${mimeType.mimeType}
					</a>
				</td>
				<td valign="top">${mimeType.description}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="Delete Selected Mime Types" mimeType="action">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>