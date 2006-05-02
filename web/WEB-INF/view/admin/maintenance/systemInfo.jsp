<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Audit" otherwise="/login.htm" redirect="/admin/maintenance/systemInfo.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<style>
</style>

<br />
<h2><spring:message code="SystemInfo.title"/></h2>
<br />

<table cellpadding="4" cellspacing="0">
	<tr>
		<th><spring:message code="SystemInfo.name"/></th>
		<th><spring:message code="SystemInfo.value"/></th>
	</tr>
	<%@ page import="org.openmrs.api.context.Context" %>
	<%
		Context context = (Context)session.getAttribute(org.openmrs.web.WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		pageContext.setAttribute("vars", context.getAdministrationService().getSystemVariables());
	%>
	<c:forEach items="${vars}" var="var" varStatus="status">
		<tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
			<td>${var.key}</td>
			<td>${var.value}</td>
		</tr>
	</c:forEach>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>