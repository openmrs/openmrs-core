<%@page isErrorPage="true" %>
<%@ include file="/WEB-INF/template/include.jsp" %>

<spring:message var="pageTitle" code="missing.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<h2>Error 404</h2>

<br /><br />

<spring:message code="Missing.start"/> "<b><%= request.getAttribute("javax.servlet.error.request_uri") %></b>"
<spring:message code="Missing.end"/>

<br/><br/>

<openmrs:extensionPoint pointId="org.openmrs.missing" type="html" />


<%@ include file="/WEB-INF/template/footer.jsp" %> 