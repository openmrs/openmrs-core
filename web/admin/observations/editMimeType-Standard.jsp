<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.api.ObsService" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.MimeType" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Observations" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	ObsService obsService = context.getObsService();
	MimeType mimeType = obsService.getMimeType(Integer.valueOf(request.getParameter("mimeTypeId")));

	if (request.getParameter("mimeType") != null) {
		mimeType.setMimeType(request.getParameter("mimeType"));
		mimeType.setDescription(request.getParameter("description"));
		try {
			adminService.updateMimeType(mimeType);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Mime Type updated");
			response.sendRedirect("mimeTypes.jsp");
			return;
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to update mime type " + e.getMessage());
		}
		
	}
	pageContext.setAttribute("mimeType", mimeType);
%>	

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Editing Mime Type</h2>

<form method="post">
<table>
	<tr>
		<td>Mime Type</td>
		<td><input type="text" name="mimeType" value="${mimeType.mimeType}" size="35" /></td>
	</tr>
	<tr>
		<td valign="top">Description</td>
		<td><textarea name="description" rows="3" cols="40">${mimeType.description}</textarea></td>
	</tr>
</table>
<input type="hidden" name="mimeTypeId" value="<c:out value="${mimeType.mimeTypeId}"/>">
<br />
<input type="submit" value="Save Mime Type">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>