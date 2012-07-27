<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Audit" otherwise="/login.htm" redirect="/admin/maintenance/auditPatientIdentifiers.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<br />
<h2><openmrs:message code="AuditPatientIdentifiers.manage"/></h2>
<br />

<a href="${pageContext.request.contextPath}/auditServlet?audit=patientIdentifiers">
	<openmrs:message code="AuditPatientIdentifiers.link"/>
</a>
<openmrs:message code="AuditPatientIdentifiers.description"/>

<%@ include file="/WEB-INF/template/footer.jsp" %>